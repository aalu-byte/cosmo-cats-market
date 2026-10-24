
package com.cosmocats.market.mapper;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private ProductMapper mapper = new ProductMapper();

    @Test
    void testToDomain() {
        ProductRequestDto dto = new ProductRequestDto(
                "Star Yarn",
                "Floats in zero-g",
                new BigDecimal("19.99"),
                1L
        );

        Product product = mapper.toDomain(dto);

        assertThat(product.getId()).isNull();
        assertThat(product.getName()).isEqualTo("Star Yarn");
        assertThat(product.getDescription()).isEqualTo("Floats in zero-g");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(product.getCategoryId()).isEqualTo(1L);
    }

    @Test
    void testToDto() {
        Product product = Product.builder()
                .id(7L)
                .name("Galaxy Milk")
                .description("Milk from the Milky Way")
                .price(new BigDecimal("4.50"))
                .categoryId(2L)
                .build();

        ProductResponseDto dto = mapper.toDto(product);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.name()).isEqualTo("Galaxy Milk");
        assertThat(dto.description()).isEqualTo("Milk from the Milky Way");
        assertThat(dto.price()).isEqualTo(new BigDecimal("4.50"));
        assertThat(dto.categoryId()).isEqualTo(2L);
    }
}
