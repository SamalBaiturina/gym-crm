package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationException;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginControllerTest extends ControllerTestBase {

    private static final Credentials JOHN = new Credentials("John.Smith", "secret12345");
    private static final String JOHN_HEADER = basic("John.Smith", "secret12345");

    private static String basic(String username, String password) {
        return "Basic " + encode(username + ":" + password);
    }

    private static String encode(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static Stream<String> malformedHeaders() {
        return Stream.of(
                "Bearer abc",
                "Basic",
                "Basic !!!not-base64",
                "Basic " + encode("no-colon"),
                "Basic " + encode("user:"),
                "Basic " + encode(":password"));
    }

    @Test
    void loginReturns200ForValidCredentials() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINEE, true));

        mockMvc.perform(get("/login").header(HttpHeaders.AUTHORIZATION, JOHN_HEADER))
                .andExpect(status().isOk());
    }

    @Test
    void passwordMayContainAColon() throws Exception {
        Credentials withColon = new Credentials("John.Smith", "pa:ss");
        when(authenticationService.authenticateAndLoad(withColon)).thenReturn(new AuthenticatedUser(withColon, Role.TRAINER, true));

        mockMvc.perform(get("/login").header(HttpHeaders.AUTHORIZATION, basic("John.Smith", "pa:ss")))
                .andExpect(status().isOk());
    }

    @Test
    void loginWithoutHeaderIs401WithChallenge() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"gym-crm\""))
                .andExpect(jsonPath("$.type").value("UNAUTHORIZED"));

        verifyNoInteractions(authenticationService);
    }

    @ParameterizedTest
    @MethodSource("malformedHeaders")
    void loginWithMalformedHeaderIs401(String authorization) throws Exception {
        mockMvc.perform(get("/login").header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("UNAUTHORIZED"));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void loginWithWrongCredentialsIs401() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenThrow(new AuthenticationException());

        mockMvc.perform(get("/login").header(HttpHeaders.AUTHORIZATION, JOHN_HEADER))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.details[0]").value("Invalid username or password"));
    }

    @Test
    void changePasswordOfTraineeGoesToTraineeService() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINEE, true));

        mockMvc.perform(put("/login")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newSecret1\"}"))
                .andExpect(status().isOk());

        verify(traineeService).changePassword(new AuthenticatedUser(JOHN, Role.TRAINEE, true), "newSecret1");
        verifyNoInteractions(trainerService);
    }

    @Test
    void changePasswordOfTrainerGoesToTrainerService() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINER, true));

        mockMvc.perform(put("/login")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newSecret1\"}"))
                .andExpect(status().isOk());

        verify(trainerService).changePassword(new AuthenticatedUser(JOHN, Role.TRAINER, true), "newSecret1");
        verifyNoInteractions(traineeService);
    }

    @Test
    void changePasswordRejectsBlankNewPassword() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINEE, true));

        mockMvc.perform(put("/login")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("VALIDATION_ERROR"));

        verifyNoInteractions(traineeService, trainerService);
    }

    @Test
    void changePasswordWithWrongOldPasswordIs401() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenThrow(new AuthenticationException());

        mockMvc.perform(put("/login")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newSecret1\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(traineeService, trainerService);
    }
}
