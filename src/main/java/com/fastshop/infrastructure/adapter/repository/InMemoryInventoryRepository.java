package com.fastshop.infrastructure.adapter.repository;

import com.fastshop.domain.model.Product;
import com.fastshop.domain.port.InventoryRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<Integer, Product> inventory = new HashMap<>();

    public InMemoryInventoryRepository() {
        // Initialize with sample data as per requirements
        inventory.put(456, new Product(456, 5, new BigDecimal("100.00")));
        inventory.put(789, new Product(789, 2, new BigDecimal("250.00")));
        inventory.put(321, new Product(321, 10, new BigDecimal("50.00")));
    }

    @Override
    public Optional<Product> findByProductId(Integer productId) {
        return Optional.ofNullable(inventory.get(productId));
    }

    @Override
    public void updateProduct(Product product) {
        inventory.put(product.getProductId(), product);
    }
}