package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
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
@ContextConfiguration(classes = {WebConfig.class, TraineeTrainersControllerTest.MocksConfig.class})
class TraineeTrainersControllerTest {

    @Configuration
    static class MocksConfig {
        @Bean
        static BeanFactoryPostProcessor serviceMocks() {
            return ServiceMocks.forWebLayer();
        }
    }

    private static final Credentials JOHN = new Credentials("John.Smith", "secret12345");
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
        when(authenticationService.authenticate(JOHN)).thenReturn(Role.TRAINEE);
    }

    private static Trainer trainer(long id, String first, String last, String type) {
        User user = new User(first, last, first + "." + last, "x", true);
        return new Trainer(id, user, new TrainingType(2L, type));
    }

    @Test
    void notAssignedTrainersAreListed() throws Exception {
        when(traineeService.getNotAssignedTrainers(JOHN))
                .thenReturn(List.of(trainer(5L, "Anna", "Brown", "YOGA")));

        mockMvc.perform(get("/trainees/John.Smith/not-assigned-trainers")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("Anna.Brown"))
                .andExpect(jsonPath("$[0].firstName").value("Anna"))
                .andExpect(jsonPath("$[0].lastName").value("Brown"))
                .andExpect(jsonPath("$[0].specialization.id").value(2))
                .andExpect(jsonPath("$[0].specialization.name").value("YOGA"));
    }

    @Test
    void notAssignedTrainersOfAnotherUserIs403() throws Exception {
        mockMvc.perform(get("/trainees/Mike.Jones/not-assigned-trainers")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(traineeService);
    }

    @Test
    void updateTrainersSendsUsernamesAndReturnsTheNewList() throws Exception {
        when(traineeService.updateTrainers(JOHN, List.of("Anna.Brown", "Bob.White")))
                .thenReturn(List.of(trainer(5L, "Anna", "Brown", "YOGA"), trainer(6L, "Bob", "White", "FITNESS")));

        mockMvc.perform(put("/trainees/John.Smith/trainers")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainers\":[{\"username\":\"Anna.Brown\"},{\"username\":\"Bob.White\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("Anna.Brown"))
                .andExpect(jsonPath("$[1].username").value("Bob.White"));

        verify(traineeService).updateTrainers(JOHN, List.of("Anna.Brown", "Bob.White"));
    }

    @Test
    void emptyListRemovesAllTrainers() throws Exception {
        mockMvc.perform(put("/trainees/John.Smith/trainers")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainers\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(traineeService).updateTrainers(JOHN, List.of());
    }

    @Test
    void unknownTrainerIs404() throws Exception {
        when(traineeService.updateTrainers(JOHN, List.of("Zed.Nobody")))
                .thenThrow(new NotFoundException("Trainers not found: [Zed.Nobody]"));

        mockMvc.perform(put("/trainees/John.Smith/trainers")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainers\":[{\"username\":\"Zed.Nobody\"}]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("NOT_FOUND"))
                .andExpect(jsonPath("$.details[0]").value("Trainers not found: [Zed.Nobody]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"trainers\":null}",
            "{\"trainers\":[{}]}",
            "{\"trainers\":[{\"username\":\" \"}]}",
            "{\"trainers\":[null]}"
    })
    void updateTrainersRejectsInvalidBodies(String body) throws Exception {
        mockMvc.perform(put("/trainees/John.Smith/trainers")
                        .header(HttpHeaders.AUTHORIZATION, JOHN_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }
}
