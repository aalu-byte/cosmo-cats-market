package com.cosmocats.market.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private List<CartItem> items;
    private Double totalPrice;
    private String status;
    private LocalDateTime createdAt;
}