package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TrainerProfile;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
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
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, TrainerProfileControllerTest.MocksConfig.class})
class TrainerProfileControllerTest {

    @Configuration
    static class MocksConfig {
        @Bean
        static BeanFactoryPostProcessor serviceMocks() {
            return ServiceMocks.forWebLayer();
        }
    }

    private static final Credentials ANNA = new Credentials("Anna.Brown", "secret12345");
    private static final String ANNA_HEADER = "Basic " + Base64.getEncoder()
            .encodeToString("Anna.Brown:secret12345".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private WebApplicationContext webContext;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private TrainerService trainerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(authenticationService, trainerService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
        when(authenticationService.authenticateAndLoad(ANNA)).thenReturn(new AuthenticatedUser(ANNA, Role.TRAINER, true));
    }

    private static Trainer trainer(String firstName, boolean active) {
        User user = new User(firstName, "Brown", "Anna.Brown", "secret12345", active);
        return new Trainer(5L, user, new TrainingType(2L, "YOGA"));
    }

    private static Trainee john() {
        User user = new User("John", "Smith", "John.Smith", "x", true);
        return new Trainee(1L, user, LocalDate.of(2000, 1, 1), "Astana");
    }

    @Test
    void getReturnsTheProfileWithTrainees() throws Exception {
        when(trainerService.getProfile(ANNA)).thenReturn(new TrainerProfile(trainer("Anna", true), List.of(john())));

        mockMvc.perform(get("/trainers/Anna.Brown").header(HttpHeaders.AUTHORIZATION, ANNA_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Anna.Brown"))
                .andExpect(jsonPath("$.firstName").value("Anna"))
                .andExpect(jsonPath("$.lastName").value("Brown"))
                .andExpect(jsonPath("$.specialization.id").value(2))
                .andExpect(jsonPath("$.specialization.name").value("YOGA"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.trainees.length()").value(1))
                .andExpect(jsonPath("$.trainees[0].username").value("John.Smith"))
                .andExpect(jsonPath("$.trainees[0].firstName").value("John"))
                .andExpect(jsonPath("$.trainees[0].lastName").value("Smith"));
    }

    @Test
    void getOfAnotherUserIs403() throws Exception {
        mockMvc.perform(get("/trainers/Bob.White").header(HttpHeaders.AUTHORIZATION, ANNA_HEADER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainerService);
    }

    @Test
    void updateIgnoresTheSpecializationFromTheRequest() throws Exception {
        when(trainerService.updateProfile(ANNA, "Annie", "Braun", false))
                .thenReturn(new TrainerProfile(trainer("Annie", false), List.of()));

        mockMvc.perform(put("/trainers/Anna.Brown")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Annie","lastName":"Braun","isActive":false,"specializationId":3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Annie"))
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.specialization.name").value("YOGA"));

        verify(trainerService).updateProfile(ANNA, "Annie", "Braun", false);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"firstName\":\"Annie\",\"lastName\":\"Braun\"}",
            "{\"firstName\":\"Annie\",\"lastName\":\" \",\"isActive\":true}"
    })
    void updateRejectsMissingRequiredFields(String body) throws Exception {
        mockMvc.perform(put("/trainers/Anna.Brown")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }
}
