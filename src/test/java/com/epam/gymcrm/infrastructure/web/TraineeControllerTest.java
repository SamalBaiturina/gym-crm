package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.RegisterTrainee;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TraineeControllerTest extends ControllerTestBase {

    @BeforeEach
    void setUp() {
        when(traineeService.register(any(RegisterTrainee.class))).thenAnswer(invocation -> {
            RegisterTrainee command = invocation.getArgument(0);
            User user = User.of(command.firstName(), command.lastName())
                    .withCredentials("John.Smith", "secret12345");
            return new Trainee(1L, user, command.dateOfBirth(), command.address());
        });
    }

    @Test
    void registerReturnsGeneratedCredentials() throws Exception {
        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"John","lastName":"Smith",
                                 "dateOfBirth":"2000-01-01","address":"Astana"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.password").value("secret12345"));

        ArgumentCaptor<RegisterTrainee> captor = ArgumentCaptor.forClass(RegisterTrainee.class);
        verify(traineeService).register(captor.capture());
        RegisterTrainee sent = captor.getValue();
        assertEquals("John", sent.firstName());
        assertEquals("Smith", sent.lastName());
        assertEquals(LocalDate.of(2000, 1, 1), sent.dateOfBirth());
        assertEquals("Astana", sent.address());
    }

    @Test
    void registerWorksWithoutOptionalFields() throws Exception {
        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Smith\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<RegisterTrainee> captor = ArgumentCaptor.forClass(RegisterTrainee.class);
        verify(traineeService).register(captor.capture());
        assertNull(captor.getValue().dateOfBirth());
        assertNull(captor.getValue().address());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"lastName\":\"Smith\"}",
            "{\"firstName\":\"John\",\"lastName\":\" \"}"
    })
    void registerRejectsMissingRequiredFields(String body) throws Exception {
        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }
}
