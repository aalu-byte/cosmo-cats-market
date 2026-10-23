package com.cosmocats.market.mapper;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toDomain(ProductRequestDto dto) {
        return Product.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .categoryId(dto.categoryId())
                .build();
    }

    public ProductResponseDto toDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategoryId()
        );
    }
}