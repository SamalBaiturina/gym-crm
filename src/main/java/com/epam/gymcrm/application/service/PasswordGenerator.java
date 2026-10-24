package com.epam.gymcrm.application.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Generates a random 10-character password with SecureRandom. */
@Component
public class PasswordGenerator {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    /** Returns a new random password made of letters and digits. */
    public String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
