package com.danielabirzan.storemanagement.product.controller;

import com.danielabirzan.storemanagement.product.dto.ProductResponse;
import com.danielabirzan.storemanagement.product.exception.ProductNotFoundException;
import com.danielabirzan.storemanagement.product.service.ProductService;
import com.danielabirzan.storemanagement.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void getProduct_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProduct_asUser_returns200() throws Exception {
        when(productService.findById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProduct_asAdmin_returns200() throws Exception {
        when(productService.findById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProduct_notFound_returns404() throws Exception {
        when(productService.findById(99L))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProduct_invalidIdType_returns400() throws Exception {
        mockMvc.perform(get("/api/products/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProduct_unexpectedError_returns500() throws Exception {
        when(productService.findById(1L)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void listProducts_asUser_returns200() throws Exception {
        when(productService.findAll(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listProducts_asAdmin_returns200() throws Exception {
        when(productService.findAll(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void listProducts_negativePage_returns400() throws Exception {
        mockMvc.perform(get("/api/products").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void listProducts_negativeSize_returns400() throws Exception {
        mockMvc.perform(get("/api/products").param("size", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_asUser_returns403() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_asAdmin_returns201() throws Exception {
        when(productService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatePrice_asUser_returns403() throws Exception {
        mockMvc.perform(patch("/api/products/1/price").param("newPrice", "19.50"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePrice_asAdmin_returns200() throws Exception {
        when(productService.changePrice(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(patch("/api/products/1/price").param("newPrice", "19.50"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateQuantity_asUser_returns403() throws Exception {
        mockMvc.perform(patch("/api/products/1/quantity").param("newQuantity", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuantity_asAdmin_returns200() throws Exception {
        when(productService.changeQuantity(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(patch("/api/products/1/quantity").param("newQuantity", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteProduct_asUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_blankName_returns400() throws Exception {
        String invalid = """
                {"name":"","description":"x","price":9.50,"quantity":10}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_priceWithTooManyDecimals_returns400() throws Exception {
        String invalid = """
                {"name":"Milk","description":"1L, 3.5% fat","price":9.99999,"quantity":10}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_negativeQuantity_returns400() throws Exception {
        String invalid = """
                {"name":"Milk","description":"1L, 3.5% fat","price":9.50,"quantity":-1}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_decimalQuantity_returns400() throws Exception {
        String invalid = """
                {"name":"Milk","description":"1L, 3.5% fat","price":9.50,"quantity":9.10}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePrice_negativePrice_returns400() throws Exception {
        mockMvc.perform(patch("/api/products/1/price").param("newPrice", "-1.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePrice_tooManyDecimals_returns400() throws Exception {
        mockMvc.perform(patch("/api/products/1/price").param("newPrice", "9.999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuantity_negativeQuantity_returns400() throws Exception {
        mockMvc.perform(patch("/api/products/1/quantity").param("newQuantity", "-1"))
                .andExpect(status().isBadRequest());
    }

    private String validBody() {
        return """
                {"name":"Milk","description":"1L, 3.5% fat","price":9.50,"quantity":10}
                """;
    }

    private ProductResponse sampleResponse() {
        return new ProductResponse(1L, "Milk", "1L, 3.5% fat", new BigDecimal("9.50"), 10);
    }
}
