package com.cosmocats.market.service;

import com.cosmocats.market.domain.OrderStatus;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.TopSoldProductDto;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.exception.CategoryNotFoundException;
import com.cosmocats.market.repository.CategoryRepository;
import com.cosmocats.market.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.cosmocats.market.repository.entity.CategoryEntity;
import com.cosmocats.market.repository.entity.ProductEntity;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;


    @Transactional
    public Product create(Product product) {
        ProductEntity entity = new ProductEntity();
        fillEntity(entity, product);
        return toDomain(productRepository.save(entity));
    }

    public List<Product> getAll(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size, Sort.by("id")))
                .map(this::toDomain)
                .getContent();

    }

    public long count() {
        return productRepository.count();
    }

    public Product getById(Long id) {
        return toDomain(findEntity(id));
    }
    @Transactional
    public Product update(Long id, Product updated) {
        ProductEntity entity = findEntity(id);
        fillEntity(entity, updated);
        return toDomain(entity);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);

    }
    private ProductEntity findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void fillEntity(ProductEntity entity, Product product) {
        CategoryEntity category = categoryRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(product.getCategoryId()));
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPrice(product.getPrice());
        entity.setCategory(category);
    }

    private Product toDomain(ProductEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .categoryId(entity.getCategory().getId())
                .build();
    }
    public List<TopSoldProductDto> getTopSold(int limit) {
        return productRepository.findTopSoldProducts(OrderStatus.CANCELLED, PageRequest.of(0, limit))
                .stream()
                .map(p -> new TopSoldProductDto(p.getProductId(), p.getProductName(), p.getTotalQuantity()))
                .toList();
    }
}