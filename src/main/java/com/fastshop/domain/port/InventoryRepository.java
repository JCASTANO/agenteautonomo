package com.fastshop.domain.port;

import com.fastshop.domain.model.Product;
import java.util.Optional;

public interface InventoryRepository {
    Optional<Product> findByProductId(Integer productId);
    void updateProduct(Product product);
}