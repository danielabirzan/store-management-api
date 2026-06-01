package com.danielabirzan.storemanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter(objectMapper, 3, 1);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    }

    @Test
    void shouldAllowRequestsWithinLimit() throws Exception {
        for (int i = 0; i < 3; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(3)).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void shouldReturn429WhenLimitExceeded() throws Exception {
        when(request.getRequestURI()).thenReturn("/products");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        for (int i = 0; i < 3; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(3)).doFilter(request, response);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void shouldTrackDifferentIPsSeparately() throws Exception {
        when(request.getRequestURI()).thenReturn("/products");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        HttpServletRequest requestFromDifferentIP = mock(HttpServletRequest.class);
        when(requestFromDifferentIP.getRemoteAddr()).thenReturn("10.0.0.1");

        for (int i = 0; i < 3; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        filter.doFilterInternal(requestFromDifferentIP, response, filterChain);

        verify(filterChain, times(4)).doFilter(any(HttpServletRequest.class), eq(response));
        verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }
}