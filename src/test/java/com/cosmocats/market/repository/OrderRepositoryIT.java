package com.cosmocats.market.repository;

import com.cosmocats.market.TestcontainersConfiguration;
import com.cosmocats.market.domain.OrderStatus;
import com.cosmocats.market.repository.entity.CategoryEntity;
import com.cosmocats.market.repository.entity.OrderEntity;
import com.cosmocats.market.repository.entity.OrderItemEntity;
import com.cosmocats.market.repository.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class OrderRepositoryIT {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ProductEntity saveProduct(String name) {
        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setPrice(new BigDecimal("12.50"));
        product.setCategory(categoryRepository.getReferenceById(1L));
        return productRepository.save(product);
    }

    private OrderEntity newOrder(UUID orderNumber) {
        OrderEntity order = new OrderEntity();
        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.NEW);
        order.setTotalPrice(new BigDecimal("25.00"));
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }



    @Test
    void testSaveOrderWithItemsAndFindByNaturalId() {
        ProductEntity product = saveProduct("Comet Toy");
        OrderEntity order = newOrder(UUID.randomUUID());

        OrderItemEntity item = new OrderItemEntity();
        item.setProduct(product);
        item.setUnitPrice(new BigDecimal("12.50"));
        item.setQuantity(2);
        order.addItem(item);

        orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        OrderEntity found = orderRepository.findByOrderNumber(order.getOrderNumber()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(found.getItems().get(0).getUnitPrice()).isEqualByComparingTo("12.50");
        assertThat(found.getItems().get(0).getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    void testUpdateOrderStatus() {
        OrderEntity order = orderRepository.save(newOrder(UUID.randomUUID()));
        entityManager.flush();

        order.setStatus(OrderStatus.PAID);
        entityManager.flush();
        entityManager.clear();

        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void testDeleteOrderRemovesItems() {
        OrderEntity order = newOrder(UUID.randomUUID());
        OrderItemEntity item = new OrderItemEntity();
        item.setProduct(saveProduct("Star Lamp"));
        item.setUnitPrice(new BigDecimal("12.50"));
        item.setQuantity(1);
        order.addItem(item);
        orderRepository.save(order);
        entityManager.flush();

        orderRepository.delete(order);
        entityManager.flush();

        Long items = entityManager.getEntityManager()
                .createQuery("select count(i) from OrderItemEntity i", Long.class)
                .getSingleResult();
        assertThat(orderRepository.count()).isZero();
        assertThat(items).isZero();
    }

    @Test
    void testDuplicateOrderNumberIsRejected() {
        UUID orderNumber = UUID.randomUUID();
        orderRepository.saveAndFlush(newOrder(orderNumber));

        assertThatThrownBy(() -> orderRepository.saveAndFlush(newOrder(orderNumber)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}