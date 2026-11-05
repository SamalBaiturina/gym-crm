package com.epam.gymcrm.domain.model;

import com.epam.gymcrm.domain.exception.ForbiddenOperationException;

import java.util.Locale;

/**
 * Who is calling: the credentials that were checked, the role found for them and whether the account
 * is active. It is the result of one authentication pass, so nobody has to check the password again.
 */
public record AuthenticatedUser(Credentials credentials, Role role, boolean active) {

    /** The username of the authenticated user. */
    public String username() {
        return credentials.username();
    }

    /**
     * Stops an operation that belongs to another role.
     *
     * @throws ForbiddenOperationException if the user does not have the expected role
     */
    public void requireRole(Role expected) {
        if (role != expected) {
            throw new ForbiddenOperationException(
                    "This operation is available only to a " + expected.name().toLowerCase(Locale.ROOT));
        }
    }
}
