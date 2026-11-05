package com.epam.gymcrm.domain.model;

/**
 * Who is calling: the credentials that were checked, the role found for them and whether the account
 * is active. It is the result of one authentication pass, so nobody has to look the user up again.
 */
public record AuthenticatedUser(Credentials credentials, Role role, boolean active) {
}
