package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActivationControllerTest extends ControllerTestBase {

    private static final Credentials JOHN = new Credentials("John.Smith", "secret12345");
    private static final Credentials ANNA = new Credentials("Anna.Brown", "secret12345");
    private static final AuthenticatedUser JOHN_USER = new AuthenticatedUser(JOHN, Role.TRAINEE, true);
    private static final AuthenticatedUser ANNA_USER = new AuthenticatedUser(ANNA, Role.TRAINER, true);

    @BeforeEach
    void setUp() {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINEE, true));
        when(authenticationService.authenticateAndLoad(ANNA)).thenReturn(new AuthenticatedUser(ANNA, Role.TRAINER, true));
    }

    private static String basic(Credentials credentials) {
        String raw = credentials.username() + ":" + credentials.password();
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void traineeIsToggled() throws Exception {
        mockMvc.perform(patch("/trainees/John.Smith/active")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}"))
                .andExpect(status().isOk());

        verify(traineeService).toggleActive(JOHN_USER);
    }

    @Test
    void traineeIsToggledAgain() throws Exception {
        mockMvc.perform(patch("/trainees/John.Smith/active")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":true}"))
                .andExpect(status().isOk());

        verify(traineeService).toggleActive(JOHN_USER);
    }

    @Test
    void trainerIsToggled() throws Exception {
        mockMvc.perform(patch("/trainers/Anna.Brown/active")
                        .header(HttpHeaders.AUTHORIZATION, basic(ANNA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}"))
                .andExpect(status().isOk());

        verify(trainerService).toggleActive(ANNA_USER);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"isActive\":null}", "{\"isActive\":\"maybe\"}", "not json"})
    void invalidBodiesAreRejected(String body) throws Exception {
        mockMvc.perform(patch("/trainees/John.Smith/active")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    @Test
    void anotherUsersProfileCannotBeChanged() throws Exception {
        mockMvc.perform(patch("/trainees/Mike.Jones/active")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(traineeService);
    }

    @Test
    void withoutCredentialsIs401() throws Exception {
        mockMvc.perform(patch("/trainers/Anna.Brown/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(trainerService);
    }
}
