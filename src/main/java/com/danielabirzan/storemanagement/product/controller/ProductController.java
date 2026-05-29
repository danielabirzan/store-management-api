package com.danielabirzan.storemanagement.product.controller;

import com.danielabirzan.storemanagement.product.dto.ProductRequest;
import com.danielabirzan.storemanagement.product.dto.ProductResponse;
import com.danielabirzan.storemanagement.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping
    public Page<ProductResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.findAll(PageRequest.of(page, size))
                .map(ProductResponse::from);
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<ProductResponse> changePrice(@PathVariable Long id,
                                                       @RequestParam BigDecimal newPrice) {
        return ResponseEntity.ok(productService.changePrice(id, newPrice));
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<ProductResponse> changeQuantity(@PathVariable Long id,
                                                          @RequestParam Integer newQuantity) {
        return ResponseEntity.ok(productService.changeQuantity(id, newQuantity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}