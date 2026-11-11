
package com.cosmocats.market.service;

import com.cosmocats.market.domain.OrderStatus;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.TopSoldProductDto;
import com.cosmocats.market.exception.CategoryNotFoundException;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.repository.CategoryRepository;
import com.cosmocats.market.repository.ProductRepository;
import com.cosmocats.market.repository.TopProductProjection;
import com.cosmocats.market.repository.entity.CategoryEntity;
import com.cosmocats.market.repository.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
    class ProductServiceTest {

        @Mock
        private ProductRepository productRepository;

        @Mock
        private CategoryRepository categoryRepository;

        @InjectMocks
        private ProductService productService;

        private CategoryEntity category(long id) {
            CategoryEntity category = new CategoryEntity();
            category.setId(id);
            category.setName("Cosmic Toys");
            return category;
        }

        private ProductEntity entity(long id, String name) {
            ProductEntity entity = new ProductEntity();
            entity.setId(id);
            entity.setName(name);
            entity.setDescription("Test description");
            entity.setPrice(new BigDecimal("9.99"));
            entity.setCategory(category(1L));
            return entity;
        }

    private Product newProduct(String name, String price, long categoryId) {
        return Product.builder()
                .name(name)
                .description("Test description")
                .price(new BigDecimal(price))
                .categoryId(categoryId)
                .build();
    }



    @Test
    void testCreate() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L)));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(call -> {
            ProductEntity saved = call.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Product created = productService.create(newProduct("Star Snack", "1.50", 1L));
        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getName()).isEqualTo("Star Snack");
        assertThat(created.getPrice()).isEqualByComparingTo("1.50");
        assertThat(created.getCategoryId()).isEqualTo(1L);
    }
    @Test
    void testCreateCategoryNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(newProduct("Star Snack", "1.50", 99L)))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("99");

        verify(productRepository, never()).save(any());
    }

    @Test
    void testGetAll() {
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity(1L, "Star Yarn"), entity(2L, "Moon Cake"))));

        List<Product> products = productService.getAll(0, 2);

        assertThat(products).extracting(Product::getId).containsExactly(1L, 2L);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(2);
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by("id"));
    }


    @Test
    void testCount() {
        when(productRepository.count()).thenReturn(5L);

        assertThat(productService.count()).isEqualTo(5L);
    }



    @Test
    void testGetById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "Star Yarn")));

        Product product = productService.getById(1L);

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Star Yarn");
        assertThat(product.getCategoryId()).isEqualTo(1L);
    }

    @Test
    void testGetByIdNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }


    @Test
    void testUpdate() {
        ProductEntity existing = entity(2L, "Galaxy Milk");
        when(productRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category(2L)));

        Product updated = productService.update(2L, newProduct("Galaxy Milk Deluxe", "6.75", 2L));

        assertThat(updated.getId()).isEqualTo(2L);
        assertThat(updated.getName()).isEqualTo("Galaxy Milk Deluxe");
        assertThat(updated.getPrice()).isEqualByComparingTo("6.75");
        assertThat(updated.getCategoryId()).isEqualTo(2L);
        // сутність змінилась напряму, save не потрібен (dirty checking)
        assertThat(existing.getName()).isEqualTo("Galaxy Milk Deluxe");
        verify(productRepository, never()).save(any());
    }

    @Test
    void testUpdateNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(999L, newProduct("Star", "1.00", 1L)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void testDelete() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void testDeleteNotFound() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(999L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }
    @Test
    void testGetTopSold() {
        TopProductProjection projection = mock(TopProductProjection.class);
        when(projection.getProductId()).thenReturn(1L);
        when(projection.getProductName()).thenReturn("Star Yarn");
        when(projection.getTotalQuantity()).thenReturn(7L);
        when(productRepository.findTopSoldProducts(OrderStatus.CANCELLED, PageRequest.of(0, 3)))
                .thenReturn(List.of(projection));

        List<TopSoldProductDto> top = productService.getTopSold(3);

        assertThat(top).containsExactly(new TopSoldProductDto(1L, "Star Yarn", 7L));
    }
}
