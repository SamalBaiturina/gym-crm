package com.epam.gymcrm.infrastructure.facade;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.NewTraining;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.PasswordChange;
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
    private final Training training = new Training(1L, 1L, 5L, "Yoga", new TrainingType("YOGA"), FROM, 60);

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
        when(trainerService.getByUsername(CREDENTIALS)).thenReturn(trainer);

        assertSame(trainer, facade.getTrainer(CREDENTIALS));
    }

    @Test
    void getTrainee_delegates() {
        when(traineeService.getByUsername(CREDENTIALS)).thenReturn(trainee);

        assertSame(trainee, facade.getTrainee(CREDENTIALS));
    }

    @Test
    void changeTraineePassword_delegates() {
        PasswordChange change = new PasswordChange(CREDENTIALS, "new");

        facade.changeTraineePassword(change);

        verify(traineeService).changePassword(change);
    }

    @Test
    void changeTrainerPassword_delegates() {
        PasswordChange change = new PasswordChange(CREDENTIALS, "new");

        facade.changeTrainerPassword(change);

        verify(trainerService).changePassword(change);
    }

    @Test
    void updateTrainer_delegates() {
        when(trainerService.update(CREDENTIALS, trainer)).thenReturn(trainer);

        assertSame(trainer, facade.updateTrainer(CREDENTIALS, trainer));
    }

    @Test
    void updateTrainee_delegates() {
        when(traineeService.update(CREDENTIALS, trainee)).thenReturn(trainee);

        assertSame(trainee, facade.updateTrainee(CREDENTIALS, trainee));
    }

    @Test
    void toggleTraineeActive_delegates() {
        when(traineeService.toggleActive(CREDENTIALS)).thenReturn(false);

        assertFalse(facade.toggleTraineeActive(CREDENTIALS));
    }

    @Test
    void toggleTrainerActive_delegates() {
        when(trainerService.toggleActive(CREDENTIALS)).thenReturn(true);

        assertTrue(facade.toggleTrainerActive(CREDENTIALS));
    }

    @Test
    void deleteTrainee_delegates() {
        facade.deleteTrainee(CREDENTIALS);

        verify(traineeService).delete(CREDENTIALS);
    }

    @Test
    void getTraineeTrainings_delegates() {
        TraineeTrainingCriteria criteria = new TraineeTrainingCriteria(FROM, TO, "Anna", "YOGA");
        when(trainingService.getTraineeTrainings(CREDENTIALS, criteria)).thenReturn(List.of(training));

        assertSame(training, facade.getTraineeTrainings(CREDENTIALS, criteria).get(0));
    }

    @Test
    void getTrainerTrainings_delegates() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(FROM, TO, "John");
        when(trainingService.getTrainerTrainings(CREDENTIALS, criteria)).thenReturn(List.of(training));

        assertSame(training, facade.getTrainerTrainings(CREDENTIALS, criteria).get(0));
    }

    @Test
    void addTraining_delegates() {
        NewTraining request = new NewTraining("John.Smith", "Yoga", new TrainingType("YOGA"), FROM, 60);
        when(trainingService.addTraining(CREDENTIALS, request)).thenReturn(training);

        assertSame(training, facade.addTraining(CREDENTIALS, request));
    }

    @Test
    void getNotAssignedTrainers_delegates() {
        when(traineeService.getNotAssignedTrainers(CREDENTIALS)).thenReturn(List.of(trainer));

        assertSame(trainer, facade.getNotAssignedTrainers(CREDENTIALS).get(0));
    }

    @Test
    void updateTraineeTrainers_delegates() {
        when(traineeService.updateTrainers(CREDENTIALS, List.of("Anna.Brown"))).thenReturn(List.of(trainer));

        assertSame(trainer, facade.updateTraineeTrainers(CREDENTIALS, List.of("Anna.Brown")).get(0));
    }
}
