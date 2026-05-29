package com.danielabirzan.storemanagement.product.service;

import com.danielabirzan.storemanagement.product.dto.ProductRequest;
import com.danielabirzan.storemanagement.product.dto.ProductResponse;
import com.danielabirzan.storemanagement.product.model.Product;
import com.danielabirzan.storemanagement.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.quantity()
        );
        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
        return toResponse(product);
    }

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public ProductResponse changePrice(Long id, BigDecimal newPrice) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
        product.setPrice(newPrice);
        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public ProductResponse changeQuantity(Long id, Integer newQuantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
        product.setQuantity(newQuantity);
        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NoSuchElementException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity()
        );
    }
}