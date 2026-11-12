package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.application.port.out.UsageMetrics;
import com.epam.gymcrm.domain.exception.ForbiddenOperationException;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Checks that the services report the business events to be counted, and only the successful ones. */
@ExtendWith(MockitoExtension.class)
class UsageMetricsReportingTest {

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
    private UsageMetrics usageMetrics;
    @InjectMocks
    private TraineeService traineeService;
    @InjectMocks
    private TrainerService trainerService;
    @InjectMocks
    private TrainingService trainingService;
    @InjectMocks
    private AuthenticationService authenticationService;

    private Trainee trainee() {
        return new Trainee(1L, new User("John", "Smith", "John.Smith", "secret", true), null, null);
    }

    private Trainer trainer() {
        return new Trainer(5L, new User("Anna", "Brown", "Anna.Brown", "secret", true), new TrainingType("YOGA"));
    }

    private void givenGeneratedCredentials(String firstName, String lastName, String username) {
        when(usernameGenerator.generate(firstName, lastName)).thenReturn(username);
        when(passwordGenerator.generate()).thenReturn("pass123456");
    }

    @Test
    void creatingATraineeReportsATraineeRegistration() {
        givenGeneratedCredentials("John", "Smith", "John.Smith");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee toSave = invocation.getArgument(0);
            return toSave.withId(1L);
        });

        traineeService.create(new Trainee(null, User.of("John", "Smith"), null, null));

        verify(usageMetrics).registrationCompleted(Role.TRAINEE);
    }

    @Test
    void creatingATrainerReportsATrainerRegistration() {
        givenGeneratedCredentials("Anna", "Brown", "Anna.Brown");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer toSave = invocation.getArgument(0);
            return toSave.withId(5L);
        });

        trainerService.create(new Trainer(null, User.of("Anna", "Brown"), new TrainingType("YOGA")));

        verify(usageMetrics).registrationCompleted(Role.TRAINER);
    }

    @Test
    void aRejectedRegistrationIsNotCounted() {
        Trainee invalid = new Trainee(null, User.of(" ", "Smith"), null, null);

        assertThrows(IllegalArgumentException.class, () -> traineeService.create(invalid));

        verifyNoInteractions(usageMetrics);
    }

    @Test
    void addingATrainingReportsIt() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer()));
        when(trainingDao.save(any(Training.class))).thenAnswer(invocation -> {
            Training toSave = invocation.getArgument(0);
            return toSave.withId(10L);
        });

        trainingService.addTraining(
                new AddTrainingCommand("Anna.Brown", "John.Smith", "Anna.Brown", "Yoga", DATE, 60));

        verify(usageMetrics).trainingAdded();
    }

    @Test
    void aForbiddenTrainingIsNotCounted() {
        AddTrainingCommand byStranger =
                new AddTrainingCommand("Mike.Jones", "John.Smith", "Anna.Brown", "Yoga", DATE, 60);

        assertThrows(ForbiddenOperationException.class, () -> trainingService.addTraining(byStranger));

        verifyNoInteractions(usageMetrics);
    }

    @Test
    void aWrongPasswordReportsAnAuthenticationFailure() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticateAndLoad(new Credentials("John.Smith", "wrong")));

        verify(usageMetrics).authenticationFailed();
    }

    @Test
    void aSuccessfulLoginIsNotAFailure() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));

        authenticationService.authenticateAndLoad(new Credentials("John.Smith", "secret"));

        verify(usageMetrics, never()).authenticationFailed();
    }
}
