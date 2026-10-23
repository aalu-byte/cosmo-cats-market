package com.cosmocats.market.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class CosmicWordValidator implements ConstraintValidator<CosmicWordCheck, String> {

    private static final List<String> COSMIC_WORDS = List.of(
            "star", "galaxy", "comet", "planet", "moon", "nebula",
            "cosmic", "space", "orbit", "asteroid", "meteor", "rocket"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null перевіряє @NotBlank
        }
        String lower = value.toLowerCase();
        return COSMIC_WORDS.stream().anyMatch(lower::contains);
    }
}