package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AddTrainingCommand;
import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.TraineeTrainingCriteria;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainerTrainingCriteria;
import com.epam.gymcrm.domain.model.Training;
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
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, TrainingControllerTest.MocksConfig.class})
class TrainingControllerTest {

    @Configuration
    static class MocksConfig {
        @Bean
        static BeanFactoryPostProcessor serviceMocks() {
            return ServiceMocks.forWebLayer();
        }
    }

    private static final Credentials JOHN = new Credentials("John.Smith", "secret12345");
    private static final Credentials ANNA = new Credentials("Anna.Brown", "secret12345");
    private static final Credentials MIKE = new Credentials("Mike.Jones", "secret12345");

    private static final String ADD_BODY = """
            {"traineeUsername":"John.Smith","trainerUsername":"Anna.Brown","trainingName":"Morning yoga",
             "trainingDate":"2026-01-10","trainingDuration":60}
            """;
    private static final AddTrainingCommand ADD_COMMAND = new AddTrainingCommand(
            "John.Smith", "Anna.Brown", "Morning yoga", LocalDate.of(2026, 1, 10), 60);

    @Autowired
    private WebApplicationContext webContext;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private TrainingService trainingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(authenticationService, trainingService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
        when(authenticationService.authenticate(JOHN)).thenReturn(Role.TRAINEE);
        when(authenticationService.authenticate(ANNA)).thenReturn(Role.TRAINER);
        when(authenticationService.authenticate(MIKE)).thenReturn(Role.TRAINEE);
    }

    private static String basic(Credentials credentials) {
        String raw = credentials.username() + ":" + credentials.password();
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static Training training() {
        Trainee trainee = new Trainee(1L, new User("John", "Smith", "John.Smith", "x", true), null, null);
        Trainer trainer = new Trainer(5L, new User("Anna", "Brown", "Anna.Brown", "x", true),
                new TrainingType(2L, "YOGA"));
        return new Training(10L, trainee, trainer, "Morning yoga", new TrainingType(2L, "YOGA"),
                LocalDate.of(2026, 1, 10), 60);
    }

    // ---------- add training ----------

    @Test
    void traineeCanAddATraining() throws Exception {
        mockMvc.perform(post("/trainings")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_BODY))
                .andExpect(status().isOk());

        verify(trainingService).addTraining(ADD_COMMAND);
    }

    @Test
    void trainerCanAddATraining() throws Exception {
        mockMvc.perform(post("/trainings")
                        .header(HttpHeaders.AUTHORIZATION, basic(ANNA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_BODY))
                .andExpect(status().isOk());

        verify(trainingService).addTraining(ADD_COMMAND);
    }

    @Test
    void anotherUserCannotAddATraining() throws Exception {
        mockMvc.perform(post("/trainings")
                        .header(HttpHeaders.AUTHORIZATION, basic(MIKE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("FORBIDDEN"));

        verifyNoInteractions(trainingService);
    }

    @Test
    void addWithoutCredentialsIs401() throws Exception {
        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(trainingService);
    }

    @Test
    void unknownTrainerIs404() throws Exception {
        when(trainingService.addTraining(ADD_COMMAND))
                .thenThrow(new NotFoundException("Trainer not found: Anna.Brown"));

        mockMvc.perform(post("/trainings")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details[0]").value("Trainer not found: Anna.Brown"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"traineeUsername\":\"John.Smith\",\"trainerUsername\":\"Anna.Brown\",\"trainingDate\":\"2026-01-10\",\"trainingDuration\":60}",
            "{\"traineeUsername\":\"John.Smith\",\"trainerUsername\":\"Anna.Brown\",\"trainingName\":\"Yoga\",\"trainingDuration\":60}",
            "{\"traineeUsername\":\"John.Smith\",\"trainerUsername\":\"Anna.Brown\",\"trainingName\":\"Yoga\",\"trainingDate\":\"2026-01-10\"}",
            "{\"traineeUsername\":\"John.Smith\",\"trainerUsername\":\"Anna.Brown\",\"trainingName\":\"Yoga\",\"trainingDate\":\"2026-01-10\",\"trainingDuration\":0}",
            "{\"traineeUsername\":\"John.Smith\",\"trainerUsername\":\"Anna.Brown\",\"trainingName\":\"Yoga\",\"trainingDate\":\"2026-01-10\",\"trainingDuration\":-5}",
            "{\"traineeUsername\":\" \",\"trainerUsername\":\"Anna.Brown\",\"trainingName\":\"Yoga\",\"trainingDate\":\"2026-01-10\",\"trainingDuration\":60}",
            "{\"traineeUsername\":\"John.Smith\",\"trainerUsername\":\"Anna.Brown\",\"trainingName\":\"Yoga\",\"trainingDate\":\"not-a-date\",\"trainingDuration\":60}"
    })
    void addRejectsInvalidBodies(String body) throws Exception {
        mockMvc.perform(post("/trainings")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingService);
    }

    // ---------- trainee's trainings ----------

    @Test
    void traineeTrainingsAreListedWithoutFilters() throws Exception {
        when(trainingService.getTraineeTrainings(JOHN, TraineeTrainingCriteria.any()))
                .thenReturn(List.of(training()));

        mockMvc.perform(get("/trainees/John.Smith/trainings").header(HttpHeaders.AUTHORIZATION, basic(JOHN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trainingName").value("Morning yoga"))
                .andExpect(jsonPath("$[0].trainingDate").value("2026-01-10"))
                .andExpect(jsonPath("$[0].trainingType").value("YOGA"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60))
                .andExpect(jsonPath("$[0].trainerName").value("Anna Brown"));
    }

    @Test
    void traineeTrainingsPassAllFiltersToTheService() throws Exception {
        mockMvc.perform(get("/trainees/John.Smith/trainings")
                        .param("periodFrom", "2026-01-01")
                        .param("periodTo", "2026-01-31")
                        .param("trainerName", "Anna")
                        .param("trainingType", "YOGA")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN)))
                .andExpect(status().isOk());

        verify(trainingService).getTraineeTrainings(JOHN, new TraineeTrainingCriteria(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "Anna", "YOGA"));
    }

    @Test
    void traineeTrainingsRejectABadDate() throws Exception {
        mockMvc.perform(get("/trainees/John.Smith/trainings")
                        .param("periodFrom", "yesterday")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingService);
    }

    @Test
    void traineeTrainingsRejectAPeriodThatEndsBeforeItStarts() throws Exception {
        mockMvc.perform(get("/trainees/John.Smith/trainings")
                        .param("periodFrom", "2026-02-01")
                        .param("periodTo", "2026-01-01")
                        .header(HttpHeaders.AUTHORIZATION, basic(JOHN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0]").value("from must not be after to"));

        verifyNoInteractions(trainingService);
    }

    @Test
    void traineeTrainingsOfAnotherUserIs403() throws Exception {
        mockMvc.perform(get("/trainees/Mike.Jones/trainings").header(HttpHeaders.AUTHORIZATION, basic(JOHN)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(trainingService);
    }

    // ---------- trainer's trainings ----------

    @Test
    void trainerTrainingsAreListedWithTheTraineeName() throws Exception {
        when(trainingService.getTrainerTrainings(ANNA, TrainerTrainingCriteria.any()))
                .thenReturn(List.of(training()));

        mockMvc.perform(get("/trainers/Anna.Brown/trainings").header(HttpHeaders.AUTHORIZATION, basic(ANNA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning yoga"))
                .andExpect(jsonPath("$[0].trainingDate").value("2026-01-10"))
                .andExpect(jsonPath("$[0].trainingType").value("YOGA"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60))
                .andExpect(jsonPath("$[0].traineeName").value("John Smith"));
    }

    @Test
    void trainerTrainingsPassFiltersToTheService() throws Exception {
        mockMvc.perform(get("/trainers/Anna.Brown/trainings")
                        .param("periodFrom", "2026-01-01")
                        .param("periodTo", "2026-01-31")
                        .param("traineeName", "John")
                        .header(HttpHeaders.AUTHORIZATION, basic(ANNA)))
                .andExpect(status().isOk());

        verify(trainingService).getTrainerTrainings(ANNA, new TrainerTrainingCriteria(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "John"));
    }
}
