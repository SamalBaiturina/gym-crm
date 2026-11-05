package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TraineeProfile;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.application.service.UpdateTrainee;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, TraineeProfileControllerTest.MocksConfig.class})
class TraineeProfileControllerTest {

    @Configuration
    static class MocksConfig {
        @Bean
        static BeanFactoryPostProcessor serviceMocks() {
            return ServiceMocks.forWebLayer();
        }
    }

    private static final Credentials JOHN = new Credentials("John.Smith", "secret12345");
    private static final AuthenticatedUser JOHN_USER = new AuthenticatedUser(JOHN, Role.TRAINEE, true);
    private static final String JOHN_HEADER = "Basic " + Base64.getEncoder()
            .encodeToString("John.Smith:secret12345".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private WebApplicationContext webContext;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private TraineeService traineeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(authenticationService, traineeService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINEE, true));
    }

    private static Trainee trainee(String firstName, boolean active) {
        User user = new User(firstName, "Smith", "John.Smith", "secret12345", active);
        return new Trainee(1L, user, LocalDate.of(2000, 1, 1), "Astana");
    }

    private static Trainer anna() {
        User user = new User("Anna", "Brown", "Anna.Brown", "x", true);
        return new Trainer(5L, user, new TrainingType(2L, "YOGA"));
    }

    @Test
    void getReturnsTheProfileWithTrainers() throws Exception {
        when(traineeService.getProfile(JOHN_USER)).thenReturn(new TraineeProfile(trainee("John", true), List.of(anna())));

        mockMvc.perform(get("/trainees/John.Smith").header(HttpHeaders.AUTHORIZATION, JOHN_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.dateOfBirth").value("2000-01-01"))
                .andExpect(jsonPath("$.address").value("Astana"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.trainers.length()").value(1))
                .andExpect(jsonPath("$.trainers[0].username").value("Anna.Brown"))
                .andExpect(jsonPath("$.trainers[0].firstName").value("Anna"))
                .andExpect(jsonPath("$.trainers[0].lastName").value("Brown"))
                .andExpect(jsonPath("$.trainers[0].specialization.id").value(2))
                .andExpect(jsonPath("$.trainers[0].specialization.name").value("YOGA"));
    }

    @Test
    void getOfAnotherUserIs403() throws Exception {
        mockMvc.perform(get("/trainees/Mike.Jones").header(HttpHeaders.AUTHORIZATION, JOHN_HEADER))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("FORBIDDEN"));

        verifyNoInteractions(traineeService);
    }

    @Test
    void getWithoutCredentialsIs401() throws Exception {
        mockMvc.perform(get("/trainees/John.Smith"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(traineeService);
    }

    @Test
    void updateSendsTheChangesAndReturnsTheProfile() throws Exception {
        when(traineeService.updateProfile(eq(JOHN_USER), any(UpdateTrainee.class)))
                .thenReturn(new TraineeProfile(trainee("Johnny", false), List.of()));

        mockMvc.perform(put("/trainees/John.Smith")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Johnny","lastName":"Smythe","dateOfBirth":"1999-05-05",
                                 "address":"Almaty","isActive":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.trainers.length()").value(0));

        ArgumentCaptor<UpdateTrainee> captor = ArgumentCaptor.forClass(UpdateTrainee.class);
        verify(traineeService).updateProfile(eq(JOHN_USER), captor.capture());
        UpdateTrainee sent = captor.getValue();
        assertEquals("Johnny", sent.firstName());
        assertEquals("Smythe", sent.lastName());
        assertEquals(LocalDate.of(1999, 5, 5), sent.dateOfBirth());
        assertEquals("Almaty", sent.address());
        assertFalse(sent.active());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"firstName\":\"Johnny\",\"lastName\":\"Smythe\"}",
            "{\"firstName\":\" \",\"lastName\":\"Smythe\",\"isActive\":true}",
            "{\"firstName\":\"Johnny\",\"isActive\":true}"
    })
    void updateRejectsMissingRequiredFields(String body) throws Exception {
        mockMvc.perform(put("/trainees/John.Smith")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    @Test
    void deleteRemovesTheCallersProfile() throws Exception {
        mockMvc.perform(delete("/trainees/John.Smith").header(HttpHeaders.AUTHORIZATION, JOHN_HEADER))
                .andExpect(status().isOk());

        verify(traineeService).delete(JOHN_USER);
    }
}
