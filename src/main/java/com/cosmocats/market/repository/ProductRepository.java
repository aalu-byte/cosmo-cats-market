package com.cosmocats.market.repository;

import com.cosmocats.market.repository.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cosmocats.market.domain.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    @Query("""
            select i.product.id as productId,
                   i.product.name as productName,
                   sum(i.quantity) as totalQuantity
            from OrderItemEntity i
            where i.order.status <> :excludedStatus
            group by i.product.id, i.product.name
            order by sum(i.quantity) desc, i.product.name asc
            """)
    List<TopProductProjection> findTopSoldProducts(@Param("excludedStatus") OrderStatus excludedStatus, Pageable pageable);
}