package com.cosmocats.market.service;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final Map<Long, Product> products = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public ProductService() {
        create(Product.builder().name("Anti-gravity Star Yarn").description("Floats in zero-g").price(19.99).categoryId(1L).build());
        create(Product.builder().name("Galaxy Milk").description("Milk from the Milky Way").price(4.50).categoryId(2L).build());
        create(Product.builder().name("Comet Laser Pointer").description("Faster than light chase").price(12.00).categoryId(1L).build());
    }

    public Product create(Product product) {
        Long id = idGenerator.incrementAndGet();
        product.setId(id);
        products.put(id, product);
        return product;
    }

    public List<Product> getAll(int page, int size) {
        List<Product> all = new ArrayList<>(products.values());
        int from = page * size;
        int to = Math.min(from + size, all.size());
        if (from >= all.size()) {
            return List.of();
        }
        return all.subList(from, to);
    }

    public long count() {
        return products.size();
    }

    public Product getById(Long id) {
        Product product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    public Product update(Long id, Product updated) {
        Product existing = getById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setCategoryId(updated.getCategoryId());
        return existing;
    }

    public void delete(Long id) {
        if(products.remove(id)==null) {
            throw new ProductNotFoundException(id);

        }

    }
}