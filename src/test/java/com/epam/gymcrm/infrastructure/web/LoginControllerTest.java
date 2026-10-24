package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationException;
import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.PasswordChange;
import com.epam.gymcrm.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, LoginControllerTest.MocksConfig.class})
class LoginControllerTest {

    @Configuration
    static class MocksConfig {
        @Bean
        static BeanFactoryPostProcessor serviceMocks() {
            return ServiceMocks.forWebLayer();
        }
    }

    private static final Credentials JOHN = new Credentials("John.Smith", "secret12345");
    private static final String JOHN_HEADER = basic("John.Smith", "secret12345");

    @Autowired
    private WebApplicationContext webContext;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private TraineeService traineeService;
    @Autowired
    private TrainerService trainerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(authenticationService, traineeService, trainerService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    }

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
        when(authenticationService.authenticate(JOHN)).thenReturn(Role.TRAINEE);

        mockMvc.perform(get("/login").header(HttpHeaders.AUTHORIZATION, JOHN_HEADER))
                .andExpect(status().isOk());
    }

    @Test
    void passwordMayContainAColon() throws Exception {
        Credentials withColon = new Credentials("John.Smith", "pa:ss");
        when(authenticationService.authenticate(withColon)).thenReturn(Role.TRAINER);

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
        when(authenticationService.authenticate(JOHN)).thenThrow(new AuthenticationException());

        mockMvc.perform(get("/login").header(HttpHeaders.AUTHORIZATION, JOHN_HEADER))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.details[0]").value("Invalid username or password"));
    }

    @Test
    void changePasswordOfTraineeGoesToTraineeService() throws Exception {
        when(authenticationService.authenticate(JOHN)).thenReturn(Role.TRAINEE);

        mockMvc.perform(put("/login")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newSecret1\"}"))
                .andExpect(status().isOk());

        verify(traineeService).changePassword(new PasswordChange(JOHN, "newSecret1"));
        verifyNoInteractions(trainerService);
    }

    @Test
    void changePasswordOfTrainerGoesToTrainerService() throws Exception {
        when(authenticationService.authenticate(JOHN)).thenReturn(Role.TRAINER);

        mockMvc.perform(put("/login")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newSecret1\"}"))
                .andExpect(status().isOk());

        verify(trainerService).changePassword(new PasswordChange(JOHN, "newSecret1"));
        verifyNoInteractions(traineeService);
    }

    @Test
    void changePasswordRejectsBlankNewPassword() throws Exception {
        when(authenticationService.authenticate(JOHN)).thenReturn(Role.TRAINEE);

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
        when(authenticationService.authenticate(JOHN)).thenThrow(new AuthenticationException());

        mockMvc.perform(put("/login")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newSecret1\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(traineeService, trainerService);
    }
}
