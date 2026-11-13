package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.TrainerProfile;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainerProfileControllerTest extends ControllerTestBase {

    private static final Credentials ANNA = new Credentials("Anna.Brown", "secret12345");
    private static final AuthenticatedUser ANNA_USER = new AuthenticatedUser(ANNA, Role.TRAINER, true);
    private static final String ANNA_HEADER = "Basic " + Base64.getEncoder()
            .encodeToString("Anna.Brown:secret12345".getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
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
        when(trainerService.getProfile(ANNA_USER)).thenReturn(new TrainerProfile(trainer("Anna", true), List.of(john())));

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
        when(trainerService.updateProfile(ANNA_USER, "Annie", "Braun", false))
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

        verify(trainerService).updateProfile(ANNA_USER, "Annie", "Braun", false);
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
