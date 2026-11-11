package com.epam.gymcrm.infrastructure.facade;

import com.epam.gymcrm.application.service.AddTrainingCommand;
import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.PasswordChange;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.TraineeTrainingCriteria;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainerTrainingCriteria;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The facade only delegates, so every test checks that the right service is called.
 */
@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 12, 31);
    private static final Credentials CREDENTIALS = new Credentials("u", "p");

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private GymFacade facade;

    private final Trainee trainee = new Trainee(1L, new User("John", "Smith", "John.Smith", "p", true), null, null);
    private final Trainer trainer = new Trainer(5L, new User("Anna", "Brown", "Anna.Brown", "p", true),
            new TrainingType("YOGA"));
    private final Training training = new Training(1L, trainee, trainer, "Yoga", new TrainingType("YOGA"), FROM, 60);

    /** The facade checks the password once, then hands the services an already authenticated trainee. */
    private AuthenticatedUser givenTraineeAuthenticated() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);
        return new AuthenticatedUser(CREDENTIALS, Role.TRAINEE, true);
    }

    /** The facade checks the password once, then hands the services an already authenticated trainer. */
    private AuthenticatedUser givenTrainerAuthenticated() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenReturn(trainer);
        return new AuthenticatedUser(CREDENTIALS, Role.TRAINER, true);
    }

    @Test
    void createTrainer_delegates() {
        when(trainerService.create(trainer)).thenReturn(trainer);

        assertSame(trainer, facade.createTrainer(trainer));
    }

    @Test
    void createTrainee_delegates() {
        when(traineeService.create(trainee)).thenReturn(trainee);

        assertSame(trainee, facade.createTrainee(trainee));
    }

    @Test
    void matchesTrainee_delegates() {
        when(authenticationService.matchesTrainee(CREDENTIALS)).thenReturn(true);

        assertTrue(facade.matchesTrainee(CREDENTIALS));
    }

    @Test
    void matchesTrainer_delegates() {
        when(authenticationService.matchesTrainer(CREDENTIALS)).thenReturn(false);

        assertFalse(facade.matchesTrainer(CREDENTIALS));
    }

    @Test
    void getTrainer_delegates() {
        AuthenticatedUser user = givenTrainerAuthenticated();
        when(trainerService.getByUsername(user)).thenReturn(trainer);

        assertSame(trainer, facade.getTrainer(CREDENTIALS));
    }

    @Test
    void getTrainee_delegates() {
        AuthenticatedUser user = givenTraineeAuthenticated();
        when(traineeService.getByUsername(user)).thenReturn(trainee);

        assertSame(trainee, facade.getTrainee(CREDENTIALS));
    }

    @Test
    void changeTraineePassword_delegates() {
        AuthenticatedUser user = givenTraineeAuthenticated();

        facade.changeTraineePassword(new PasswordChange(CREDENTIALS, "new"));

        verify(traineeService).changePassword(user, "new");
    }

    @Test
    void changeTrainerPassword_delegates() {
        AuthenticatedUser user = givenTrainerAuthenticated();

        facade.changeTrainerPassword(new PasswordChange(CREDENTIALS, "new"));

        verify(trainerService).changePassword(user, "new");
    }

    @Test
    void updateTrainer_delegates() {
        AuthenticatedUser user = givenTrainerAuthenticated();
        when(trainerService.update(user, trainer)).thenReturn(trainer);

        assertSame(trainer, facade.updateTrainer(CREDENTIALS, trainer));
    }

    @Test
    void updateTrainee_delegates() {
        AuthenticatedUser user = givenTraineeAuthenticated();
        when(traineeService.update(user, trainee)).thenReturn(trainee);

        assertSame(trainee, facade.updateTrainee(CREDENTIALS, trainee));
    }

    @Test
    void toggleTraineeActive_delegates() {
        AuthenticatedUser user = givenTraineeAuthenticated();
        when(traineeService.toggleActive(user)).thenReturn(false);

        assertFalse(facade.toggleTraineeActive(CREDENTIALS));
    }

    @Test
    void toggleTrainerActive_delegates() {
        AuthenticatedUser user = givenTrainerAuthenticated();
        when(trainerService.toggleActive(user)).thenReturn(true);

        assertTrue(facade.toggleTrainerActive(CREDENTIALS));
    }

    @Test
    void deleteTrainee_delegates() {
        AuthenticatedUser user = givenTraineeAuthenticated();

        facade.deleteTrainee(CREDENTIALS);

        verify(traineeService).delete(user);
    }

    @Test
    void getTraineeTrainings_delegates() {
        TraineeTrainingCriteria criteria = new TraineeTrainingCriteria(FROM, TO, "Anna", "YOGA");
        AuthenticatedUser user = givenTraineeAuthenticated();
        when(trainingService.getTraineeTrainings(user, criteria)).thenReturn(List.of(training));

        assertSame(training, facade.getTraineeTrainings(CREDENTIALS, criteria).get(0));
    }

    @Test
    void getTrainerTrainings_delegates() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(FROM, TO, "John");
        AuthenticatedUser user = givenTrainerAuthenticated();
        when(trainingService.getTrainerTrainings(user, criteria)).thenReturn(List.of(training));

        assertSame(training, facade.getTrainerTrainings(CREDENTIALS, criteria).get(0));
    }

    @Test
    void addTraining_delegates() {
        AddTrainingCommand command = new AddTrainingCommand("u", "John.Smith", "u", "Yoga", FROM, 60);
        when(trainingService.addTraining(command)).thenReturn(training);

        assertSame(training, facade.addTraining(CREDENTIALS, "John.Smith", "Yoga", FROM, 60));
        verify(authenticationService).authenticateTrainer(CREDENTIALS);
    }

    @Test
    void getNotAssignedTrainers_delegates() {
        AuthenticatedUser user = givenTraineeAuthenticated();
        when(traineeService.getNotAssignedTrainers(user)).thenReturn(List.of(trainer));

        assertSame(trainer, facade.getNotAssignedTrainers(CREDENTIALS).get(0));
    }

    @Test
    void updateTraineeTrainers_delegates() {
        AuthenticatedUser user = givenTraineeAuthenticated();
        when(traineeService.updateTrainers(user, List.of("Anna.Brown"))).thenReturn(List.of(trainer));

        assertSame(trainer, facade.updateTraineeTrainers(CREDENTIALS, List.of("Anna.Brown")).get(0));
    }
}
