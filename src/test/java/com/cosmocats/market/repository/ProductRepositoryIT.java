package com.cosmocats.market.repository;

import com.cosmocats.market.TestcontainersConfiguration;
import com.cosmocats.market.domain.OrderStatus;
import com.cosmocats.market.repository.entity.OrderEntity;
import com.cosmocats.market.repository.entity.OrderItemEntity;
import com.cosmocats.market.repository.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProductRepositoryIT {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private ProductEntity saveProduct(String name) {
        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setPrice(new BigDecimal("10.00"));
        product.setCategory(categoryRepository.getReferenceById(1L));
        return productRepository.save(product);
    }

    private void saveOrder(OrderStatus status, ProductEntity product, int quantity) {
        OrderEntity order = new OrderEntity();
        order.setOrderNumber(UUID.randomUUID());
        order.setStatus(status);
        order.setTotalPrice(new BigDecimal("10.00").multiply(BigDecimal.valueOf(quantity)));
        order.setCreatedAt(LocalDateTime.now());

        OrderItemEntity item = new OrderItemEntity();
        item.setProduct(product);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setQuantity(quantity);
        order.addItem(item);

        orderRepository.save(order);
    }

    @Test
    void testTopSoldProductsSortedByQuantity() {
        ProductEntity yarn = saveProduct("Star Yarn");
        ProductEntity milk = saveProduct("Galaxy Milk");
        ProductEntity lamp = saveProduct("Moon Lamp");
        saveOrder(OrderStatus.PAID, yarn, 2);
        saveOrder(OrderStatus.NEW, yarn, 3);   // Star Yarn разом 5
        saveOrder(OrderStatus.PAID, milk, 7);  // Galaxy Milk 7
        saveOrder(OrderStatus.PAID, lamp, 1);  // Moon Lamp 1
        orderRepository.flush();

        List<TopProductProjection> top =
                productRepository.findTopSoldProducts(OrderStatus.CANCELLED, PageRequest.of(0, 2));

        assertThat(top).extracting(TopProductProjection::getProductName)
                .containsExactly("Galaxy Milk", "Star Yarn");
        assertThat(top).extracting(TopProductProjection::getTotalQuantity)
                .containsExactly(7L, 5L);
    }

    @Test
    void testCancelledOrdersAreIgnored() {
        ProductEntity yarn = saveProduct("Star Yarn");
        ProductEntity milk = saveProduct("Galaxy Milk");
        saveOrder(OrderStatus.PAID, yarn, 2);
        saveOrder(OrderStatus.CANCELLED, milk, 10);
        orderRepository.flush();

        List<TopProductProjection> top =
                productRepository.findTopSoldProducts(OrderStatus.CANCELLED, PageRequest.of(0, 5));

        assertThat(top).extracting(TopProductProjection::getProductName)
                .containsExactly("Star Yarn");
    }
}