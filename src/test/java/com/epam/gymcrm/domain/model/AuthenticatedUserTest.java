package com.epam.gymcrm.domain.model;

import com.epam.gymcrm.domain.exception.ForbiddenOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticatedUserTest {

    private final AuthenticatedUser trainee =
            new AuthenticatedUser(new Credentials("John.Smith", "secret"), Role.TRAINEE, true);

    @Test
    void usernameComesFromTheCredentials() {
        assertEquals("John.Smith", trainee.username());
    }

    @Test
    void requireRole_passesForTheSameRole() {
        assertDoesNotThrow(() -> trainee.requireRole(Role.TRAINEE));
    }

    @Test
    void requireRole_isForbiddenForAnotherRole() {
        ForbiddenOperationException e =
                assertThrows(ForbiddenOperationException.class, () -> trainee.requireRole(Role.TRAINER));

        assertEquals("This operation is available only to a trainer", e.getMessage());
    }
}
