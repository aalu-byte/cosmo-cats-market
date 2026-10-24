
package com.cosmocats.market.service;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        // створюємо сервіс перед тестом
        productService = new ProductService();
    }

    private Product newProduct(String name, String price) {
        return Product.builder()
                .name(name)
                .description("Test description")
                .price(new BigDecimal(price))
                .categoryId(1L)
                .build();
    }

    @Test
    void testCount() {
        assertThat(productService.count()).isEqualTo(3);
    }

    @Test
    void testCreate() {
        Product product = productService.create(
                newProduct("Star Snack", "1.50")
        );

        assertThat(product.getId()).isEqualTo(4L);
        assertThat(productService.count()).isEqualTo(4);
        assertThat(productService.getById(4L)).isSameAs(product);
    }

    @Test
    void testCreateIds() {
        Product first = productService.create(
                newProduct("Comet Toy", "2.00")
        );
        Product second = productService.create(
                newProduct("Moon Ball", "3.00")
        );

        assertThat(second.getId()).isEqualTo(first.getId() + 1);
    }

    @Test
    void testGetAll() {
        List<Product> products = productService.getAll(0, 2);

        assertThat(products).extracting(Product::getId)
                .containsExactly(1L, 2L);
    }

    @Test
    void testGetAllSecondPage() {
        List<Product> products = productService.getAll(1, 2);

        assertThat(products).extracting(Product::getId)
                .containsExactly(3L);
    }

    @Test
    void testGetAllBigSize() {
        assertThat(productService.getAll(0, 100)).hasSize(3);
    }

    @Test
    void testGetAllEmpty() {
        assertThat(productService.getAll(5, 10)).isEmpty();
    }

    @Test
    void testGetById() {
        Product product = productService.getById(1L);

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isNotBlank();
    }

    @Test
    void testGetByIdNotFound() {
        assertThatThrownBy(() -> productService.getById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void testUpdate() {
        Product changes = Product.builder()
                .name("Galaxy Milk Deluxe")
                .description("Now with stardust")
                .price(new BigDecimal("6.75"))
                .categoryId(5L)
                .build();

        Product updated = productService.update(2L, changes);

        assertThat(updated.getId()).isEqualTo(2L);
        assertThat(updated.getName()).isEqualTo("Galaxy Milk Deluxe");
        assertThat(updated.getDescription()).isEqualTo("Now with stardust");
        assertThat(updated.getPrice())
                .isEqualByComparingTo(new BigDecimal("6.75"));
        assertThat(updated.getCategoryId()).isEqualTo(5L);
    }

    @Test
    void testUpdateNotFound() {
        assertThatThrownBy(() ->
                productService.update(999L, newProduct("Star", "1.00"))
        ).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void testDelete() {
        productService.delete(1L);

        assertThat(productService.count()).isEqualTo(2);

        assertThatThrownBy(() -> productService.getById(1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> productService.delete(999L))
                .isInstanceOf(ProductNotFoundException.class);

        assertThat(productService.count()).isEqualTo(3);
    }
}
