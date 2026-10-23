package com.cosmocats.market.dto;

public record ProductResponseDto(
        Long id,
        String name,
        String description,
        Double price,
        Long categoryId
) {
}