package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, ActivationControllerTest.MocksConfig.class})
class ActivationControllerTest {

    @Configuration
    static class MocksConfig {
        @Bean
        static BeanFactoryPostProcessor serviceMocks() {
            return ServiceMocks.forWebLayer();
        }
    }

    private static final Credentials JOHN = new Credentials("John.Smith", "secret12345");
    private static final Credentials ANNA = new Credentials("Anna.Brown", "secret12345");

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

        verify(traineeService).toggleActive(JOHN);
    }

    @Test
    void traineeIsToggledAgain() throws Exception {
        mockMvc.perform(patch("/trainees/John.Smith/active")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":true}"))
                .andExpect(status().isOk());

        verify(traineeService).toggleActive(JOHN);
    }

    @Test
    void trainerIsToggled() throws Exception {
        mockMvc.perform(patch("/trainers/Anna.Brown/active")
                        .header(HttpHeaders.AUTHORIZATION, basic(ANNA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}"))
                .andExpect(status().isOk());

        verify(trainerService).toggleActive(ANNA);
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
