package com.epam.gymcrm.infrastructure.web.security;

import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;

/** The caller whose credentials were checked for the current request. */
public record AuthenticatedUser(Credentials credentials, Role role) {
}
