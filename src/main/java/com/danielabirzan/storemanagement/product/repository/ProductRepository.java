package com.danielabirzan.storemanagement.product.repository;

import com.danielabirzan.storemanagement.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}