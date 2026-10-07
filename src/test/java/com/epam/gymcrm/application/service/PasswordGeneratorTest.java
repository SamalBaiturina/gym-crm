package com.epam.gymcrm.application.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorTest {

    private final PasswordGenerator generator = new PasswordGenerator();

    @Test
    void generate_returnsTenCharacters() {
        assertEquals(10, generator.generate().length());
    }

    @Test
    void generate_containsOnlyLettersAndDigits() {
        assertTrue(generator.generate().matches("[A-Za-z0-9]{10}"));
    }
}