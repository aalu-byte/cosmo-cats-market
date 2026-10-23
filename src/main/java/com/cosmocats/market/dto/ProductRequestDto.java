package com.cosmocats.market.dto;

import jakarta.validation.constraints.*;
import com.cosmocats.market.validation.CosmicWordCheck;

import java.math.BigDecimal;

public record ProductRequestDto(
        @NotBlank(message = "Name must not be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        @NotBlank(message = "Name must not be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        @CosmicWordCheck
        String name,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2,message = "Price must have at most 2 decimals places")
        BigDecimal price,

        @NotNull(message = "Category id is required")
        @Positive(message = "Category id must be positive")
        Long categoryId
) {
}