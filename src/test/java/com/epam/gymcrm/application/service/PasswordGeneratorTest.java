package com.epam.gymcrm.application.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorTest {

    private final PasswordGenerator generator = new PasswordGenerator();

    @Test
    void generatesTenAlphanumericCharacters() {
        String password = generator.generate();

        assertEquals(10, password.length());
        assertTrue(password.matches("[A-Za-z0-9]{10}"));
    }

    @Test
    void generatesDifferentPasswords() {
        assertNotEquals(generator.generate(), generator.generate());
    }
}
