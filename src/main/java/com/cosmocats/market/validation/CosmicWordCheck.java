package com.cosmocats.market.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CosmicWordValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CosmicWordCheck {

    String message() default "Product name must contain a cosmic word (star, galaxy, comet, ...)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}