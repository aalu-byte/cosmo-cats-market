package com.cosmocats.market.service;

import com.cosmocats.market.TestcontainersConfiguration;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.CategoryNotFoundException;
import com.cosmocats.market.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, ProductService.class})
class ProductServiceIT {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product newProduct(String name, long categoryId) {
        return Product.builder()
                .name(name)
                .description("Test description")
                .price(new BigDecimal("19.99"))
                .categoryId(categoryId)
                .build();
    }

    @Test
    void testCreateAndGetById() {
        Product created = productService.create(newProduct("Star Yarn", 1L));

        Product found = productService.getById(created.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Star Yarn");
        assertThat(found.getPrice()).isEqualByComparingTo("19.99");
        assertThat(found.getCategoryId()).isEqualTo(1L);
    }

    @Test
    void testGetAllWithPagination() {
        Product first = productService.create(newProduct("Star Yarn", 1L));
        Product second = productService.create(newProduct("Moon Cake", 2L));
        Product third = productService.create(newProduct("Comet Toy", 1L));

        List<Product> page0 = productService.getAll(0, 2);
        List<Product> page1 = productService.getAll(1, 2);

        assertThat(page0).extracting(Product::getId).containsExactly(first.getId(), second.getId());
        assertThat(page1).extracting(Product::getId).containsExactly(third.getId());
        assertThat(productService.count()).isEqualTo(3);
    }

    @Test
    void testUpdate() {
        Product created = productService.create(newProduct("Galaxy Milk", 2L));

        productService.update(created.getId(), newProduct("Galaxy Milk Deluxe", 2L));
        productRepository.flush();

        assertThat(productService.getById(created.getId()).getName()).isEqualTo("Galaxy Milk Deluxe");
    }

    @Test
    void testDelete() {
        Product created = productService.create(newProduct("Rocket Fish", 2L));

        productService.delete(created.getId());

        assertThat(productRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void testCreateWithUnknownCategory() {
        assertThatThrownBy(() -> productService.create(newProduct("Star Lamp", 99L)))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void testDuplicateNameInSameCategoryIsRejected() {
        productService.create(newProduct("Star Yarn", 1L));
        productRepository.flush();

        assertThatThrownBy(() -> {
            productService.create(newProduct("Star Yarn", 1L));
            productRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testSameNameInDifferentCategoryIsAllowed() {
        productService.create(newProduct("Star Yarn", 1L));
        productService.create(newProduct("Star Yarn", 2L));
        productRepository.flush();

        assertThat(productService.count()).isEqualTo(2);
    }
}