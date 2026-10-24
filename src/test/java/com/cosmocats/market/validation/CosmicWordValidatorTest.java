
package com.cosmocats.market.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CosmicWordValidatorTest {

    private final CosmicWordValidator validator = new CosmicWordValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "Space Pizza",
            "Moon Cake",
            "Star Lamp",
            "Galaxy Phone",
            "Cosmic Cat"
    })
    void testValidNames(String name) {
        assertThat(validator.isValid(name, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Red Chair",
            "Milk Bottle",
            "Green Apple"
    })
    void testInvalidNames(String name) {
        assertThat(validator.isValid(name, null)).isFalse();
    }

    @Test
    void testNullName() {
        // перевіряємо чи null не викликає помилку
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
