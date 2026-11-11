package com.cosmocats.market.dto;

public record TopSoldProductDto(
        Long productId,
        String productName,
        Long totalQuantity
) {
}