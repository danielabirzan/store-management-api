package com.danielabirzan.storemanagement.auth.controller;

import com.danielabirzan.storemanagement.auth.dto.LoginRequest;
import com.danielabirzan.storemanagement.auth.dto.LoginResponse;
import com.danielabirzan.storemanagement.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()));

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.replace("ROLE_", ""))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user has no role"));

        String token = jwtUtil.generateToken(request.username(), role);

        return ResponseEntity.ok(new LoginResponse(token));
    }
}