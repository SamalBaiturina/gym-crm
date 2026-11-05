package com.epam.gymcrm.infrastructure.web.security;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.domain.exception.ForbiddenOperationException;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

/**
 * Checks the Authorization header before every controller method except those marked @PublicEndpoint.
 * A deactivated user is rejected with 403 unless the method is marked @AllowInactive.
 * Exceptions thrown here are handled by GlobalExceptionHandler like any other.
 */
@Component
public class BasicAuthInterceptor implements HandlerInterceptor {

    /** Request attribute with the AuthenticatedUser. */
    public static final String AUTHENTICATED_USER = "authenticatedUser";

    private final AuthenticationService authenticationService;

    public BasicAuthInterceptor(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method) || method.hasMethodAnnotation(PublicEndpoint.class)) {
            return true;
        }
        Credentials credentials = BasicAuthParser.parse(request.getHeader(HttpHeaders.AUTHORIZATION));
        Role role = authenticationService.authenticate(credentials);
        requireActive(method, credentials);
        requireOwnUsername(request, credentials);
        request.setAttribute(AUTHENTICATED_USER, new AuthenticatedUser(credentials, role));
        return true;
    }

    /** A deactivated user may only reactivate himself. Checked after authentication, so 401 comes first. */
    private void requireActive(HandlerMethod method, Credentials credentials) {
        if (!method.hasMethodAnnotation(AllowInactive.class) && authenticationService.isDeactivated(credentials)) {
            throw new ForbiddenOperationException("The account is deactivated, activate it first");
        }
    }

    /** A user may work only with his own {username} resource. Checked after authentication, so 401 comes first. */
    @SuppressWarnings("unchecked")
    private void requireOwnUsername(HttpServletRequest request, Credentials credentials) {
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables == null || !pathVariables.containsKey("username")) {
            return;
        }
        if (!pathVariables.get("username").equals(credentials.username())) {
            throw new ForbiddenOperationException("Access to another user's data is not allowed");
        }
    }
}
