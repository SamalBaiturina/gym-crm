package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationException;
import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.domain.exception.ForbiddenOperationException;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.infrastructure.web.security.AllowInactive;
import com.epam.gymcrm.infrastructure.web.security.BasicAuthInterceptor;
import com.epam.gymcrm.infrastructure.web.security.PublicEndpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasicAuthInterceptorTest {

    /** Dummy controller: the interceptor only looks at the annotations of its methods. */
    static class Handlers {
        public void secured() {
        }

        @PublicEndpoint
        public void open() {
        }

        @AllowInactive
        public void reactivation() {
        }
    }

    private static final Credentials JOHN = new Credentials("John.Smith", "secret12345");

    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private BasicAuthInterceptor interceptor;

    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private static HandlerMethod handler(String method) throws NoSuchMethodException {
        return new HandlerMethod(new Handlers(), Handlers.class.getMethod(method));
    }

    private static MockHttpServletRequest requestFrom(String username, String password) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String raw = username + ":" + password;
        request.addHeader(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8)));
        return request;
    }

    @Test
    void publicEndpointIsNotChecked() throws Exception {
        assertTrue(interceptor.preHandle(new MockHttpServletRequest(), response, handler("open")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void handlersThatAreNotControllersAreNotChecked() {
        assertTrue(interceptor.preHandle(new MockHttpServletRequest(), response, new Object()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void validCredentialsAreStoredInTheRequest() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINER, true));
        MockHttpServletRequest request = requestFrom("John.Smith", "secret12345");

        assertTrue(interceptor.preHandle(request, response, handler("secured")));

        AuthenticatedUser user = (AuthenticatedUser) request.getAttribute(BasicAuthInterceptor.AUTHENTICATED_USER);
        assertEquals(JOHN, user.credentials());
        assertEquals(Role.TRAINER, user.role());
    }

    @Test
    void ownUsernameInThePathIsAllowed() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINEE, true));
        MockHttpServletRequest request = requestFrom("John.Smith", "secret12345");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("username", "John.Smith"));

        assertTrue(interceptor.preHandle(request, response, handler("secured")));
    }

    @Test
    void anotherUsernameInThePathIsForbidden() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINEE, true));
        MockHttpServletRequest request = requestFrom("John.Smith", "secret12345");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("username", "Anna.Brown"));
        HandlerMethod handler = handler("secured");

        assertThrows(ForbiddenOperationException.class, () -> interceptor.preHandle(request, response, handler));
    }

    @Test
    void wrongCredentialsGive401EvenForAnotherUsersPath() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenThrow(new AuthenticationException());
        MockHttpServletRequest request = requestFrom("John.Smith", "secret12345");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("username", "Anna.Brown"));
        HandlerMethod handler = handler("secured");

        assertThrows(AuthenticationException.class, () -> interceptor.preHandle(request, response, handler));
    }

    @Test
    void deactivatedUserIsForbidden() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINEE, false));
        MockHttpServletRequest request = requestFrom("John.Smith", "secret12345");
        HandlerMethod handler = handler("secured");

        assertThrows(ForbiddenOperationException.class, () -> interceptor.preHandle(request, response, handler));
    }

    @Test
    void deactivatedUserCanCallReactivation() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINEE, false));
        MockHttpServletRequest request = requestFrom("John.Smith", "secret12345");

        assertTrue(interceptor.preHandle(request, response, handler("reactivation")));
    }
}
