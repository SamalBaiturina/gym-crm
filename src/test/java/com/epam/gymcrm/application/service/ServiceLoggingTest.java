package com.epam.gymcrm.application.service;

import ch.qos.logback.classic.Level;
import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.support.LogCapture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Checks that failures are logged as warnings with the username before the exception is thrown,
 * and that a successful training is logged with both usernames.
 */
@ExtendWith(MockitoExtension.class)
class ServiceLoggingTest {

    private static final Credentials TRAINER = new Credentials("Anna.Brown", "secret");
    private static final Credentials TRAINEE = new Credentials("John.Smith", "secret");
    private static final LocalDate DATE = LocalDate.of(2026, 1, 10);

    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private TrainingService trainingService;
    @InjectMocks
    private TraineeService traineeService;

    private Trainer trainer() {
        return new Trainer(5L, new User("Anna", "Brown", "Anna.Brown", "secret", true), new TrainingType("YOGA"));
    }

    private Trainee trainee() {
        return new Trainee(1L, new User("John", "Smith", "John.Smith", "secret", true), null, null);
    }

    private NewTraining request(String name) {
        return new NewTraining("John.Smith", name, new TrainingType("YOGA"), DATE, 60);
    }

    @Test
    void addTraining_logsTrainerAndTraineeUsernames() {
        when(authenticationService.authenticateTrainer(TRAINER)).thenReturn(trainer());
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));
        when(trainingDao.save(any(Training.class))).thenAnswer(invocation -> {
            Training toSave = invocation.getArgument(0);
            return toSave.withId(10L);
        });

        try (LogCapture logs = new LogCapture(TrainingService.class)) {
            trainingService.addTraining(TRAINER, request("Yoga"));

            assertEquals(List.of("Added training id=10, trainer=Anna.Brown, trainee=John.Smith"),
                    logs.messages(Level.INFO));
        }
    }

    @Test
    void addTraining_withUnknownTrainee_logsWarning() {
        when(authenticationService.authenticateTrainer(TRAINER)).thenReturn(trainer());
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        try (LogCapture logs = new LogCapture(TrainingService.class)) {
            assertThrows(NoSuchElementException.class, () -> trainingService.addTraining(TRAINER, request("Yoga")));

            assertEquals(List.of("Cannot add training: trainee username=John.Smith not found "
                    + "(trainer username=Anna.Brown)"), logs.messages(Level.WARN));
        }
    }

    @Test
    void addTraining_withBlankName_logsValidationWarning() {
        when(authenticationService.authenticateTrainer(TRAINER)).thenReturn(trainer());

        try (LogCapture logs = new LogCapture(Validation.class)) {
            assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(TRAINER, request(" ")));

            assertEquals(List.of("Validation failed (add training by trainer username=Anna.Brown): "
                    + "trainingName is required"), logs.messages(Level.WARN));
        }
    }

    @Test
    void create_withBlankFirstName_logsValidationWarning() {
        Trainee invalid = new Trainee(null, User.of(" ", "Smith"), null, null);

        try (LogCapture logs = new LogCapture(Validation.class)) {
            assertThrows(IllegalArgumentException.class, () -> traineeService.create(invalid));

            assertEquals(List.of("Validation failed (create trainee): firstName is required"),
                    logs.messages(Level.WARN));
        }
    }

    @Test
    void update_withBlankLastName_logsValidationWarningWithUsername() {
        when(authenticationService.authenticateTrainee(TRAINEE)).thenReturn(trainee());
        Trainee changes = new Trainee(null, User.of("Johnny", ""), null, null);

        try (LogCapture logs = new LogCapture(Validation.class)) {
            assertThrows(IllegalArgumentException.class, () -> traineeService.update(TRAINEE, changes));

            assertEquals(List.of("Validation failed (update trainee username=John.Smith): lastName is required"),
                    logs.messages(Level.WARN));
        }
    }

    @Test
    void updateTrainers_withUnknownTrainer_logsWarning() {
        when(authenticationService.authenticateTrainee(TRAINEE)).thenReturn(trainee());
        when(trainerDao.findByUsernames(Set.of("nobody"))).thenReturn(List.of());

        try (LogCapture logs = new LogCapture(TraineeService.class)) {
            assertThrows(NoSuchElementException.class,
                    () -> traineeService.updateTrainers(TRAINEE, List.of("nobody")));

            assertEquals(List.of("Cannot update trainers of trainee username=John.Smith: "
                    + "trainers not found [nobody]"), logs.messages(Level.WARN));
        }
    }
}
