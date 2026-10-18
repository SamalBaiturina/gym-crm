package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.TraineeTrainingCriteria;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainerTrainingCriteria;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 1, 10);
    private static final Credentials TRAINER_CREDENTIALS = new Credentials("Anna.Brown", "secret");
    private static final Credentials TRAINEE_CREDENTIALS = new Credentials("John.Smith", "secret");

    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private TrainingService service;

    private Trainer trainer() {
        return new Trainer(5L, new User("Anna", "Brown", "Anna.Brown", "secret", true), new TrainingType("YOGA"));
    }

    private Trainee trainee() {
        return new Trainee(1L, new User("John", "Smith", "John.Smith", "secret", true), null, null);
    }

    private Training training() {
        return new Training(1L, trainee(), trainer(), "Yoga", new TrainingType("YOGA"), DATE, 60);
    }

    private NewTraining request(String name, int duration) {
        return new NewTraining("John.Smith", name, new TrainingType("YOGA"), DATE, duration);
    }

    // ---------- add training ----------

    @Test
    void addTraining_savesTrainingWithTheTraineeAndTheTrainer() {
        when(authenticationService.authenticateTrainer(TRAINER_CREDENTIALS)).thenReturn(trainer());
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));
        when(trainingDao.save(any(Training.class))).thenAnswer(invocation -> {
            Training toSave = invocation.getArgument(0);
            return toSave.withId(10L);
        });

        Training result = service.addTraining(TRAINER_CREDENTIALS, request("Yoga", 60));

        assertEquals(10L, result.id());
        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingDao).save(captor.capture());
        Training saved = captor.getValue();
        assertEquals(trainee(), saved.trainee());
        assertEquals(trainer(), saved.trainer());
        assertEquals("Yoga", saved.trainingName());
        assertEquals("YOGA", saved.trainingType().name());
        assertEquals(DATE, saved.trainingDate());
        assertEquals(60, saved.trainingDuration());
    }

    @Test
    void addTraining_withWrongPassword_throwsAndSavesNothing() {
        when(authenticationService.authenticateTrainer(TRAINER_CREDENTIALS))
                .thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class,
                () -> service.addTraining(TRAINER_CREDENTIALS, request("Yoga", 60)));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void addTraining_withUnknownTrainee_throwsAndSavesNothing() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.addTraining(TRAINER_CREDENTIALS, request("Yoga", 60)));
        verify(trainingDao, never()).save(any());
    }

    @Test
    void addTraining_withBlankName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addTraining(TRAINER_CREDENTIALS, request(" ", 60)));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void addTraining_withNonPositiveDuration_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addTraining(TRAINER_CREDENTIALS, request("Yoga", 0)));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void addTraining_withMissingDate_throws() {
        NewTraining noDate = new NewTraining("John.Smith", "Yoga", new TrainingType("YOGA"), null, 60);

        assertThrows(IllegalArgumentException.class, () -> service.addTraining(TRAINER_CREDENTIALS, noDate));
    }

    @Test
    void addTraining_withMissingType_throws() {
        NewTraining noType = new NewTraining("John.Smith", "Yoga", null, DATE, 60);

        assertThrows(IllegalArgumentException.class, () -> service.addTraining(TRAINER_CREDENTIALS, noType));
    }

    @Test
    void addTraining_withMissingTraineeUsername_throws() {
        NewTraining noTrainee = new NewTraining(" ", "Yoga", new TrainingType("YOGA"), DATE, 60);

        assertThrows(IllegalArgumentException.class, () -> service.addTraining(TRAINER_CREDENTIALS, noTrainee));
    }

    @Test
    void addTraining_withNullRequest_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.addTraining(TRAINER_CREDENTIALS, null));
    }

    // ---------- lists ----------

    @Test
    void getTraineeTrainings_passesCriteriaToDao() {
        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(DATE, LocalDate.of(2026, 12, 31), "Anna", "YOGA");
        List<Training> trainings = List.of(training());
        when(trainingDao.findForTrainee("John.Smith", criteria)).thenReturn(trainings);

        List<Training> result = service.getTraineeTrainings(TRAINEE_CREDENTIALS, criteria);

        assertSame(trainings, result);
        verify(authenticationService).authenticateTrainee(TRAINEE_CREDENTIALS);
    }

    @Test
    void getTraineeTrainings_withWrongPassword_throwsAndDoesNotTouchDao() {
        when(authenticationService.authenticateTrainee(TRAINEE_CREDENTIALS))
                .thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class,
                () -> service.getTraineeTrainings(TRAINEE_CREDENTIALS, TraineeTrainingCriteria.any()));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void getTraineeTrainings_withNullCriteria_throwsAndDoesNotTouchDao() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getTraineeTrainings(TRAINEE_CREDENTIALS, null));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void getTrainerTrainings_passesCriteriaToDao() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(DATE, null, "John");
        List<Training> trainings = List.of(training());
        when(trainingDao.findForTrainer("Anna.Brown", criteria)).thenReturn(trainings);

        List<Training> result = service.getTrainerTrainings(TRAINER_CREDENTIALS, criteria);

        assertSame(trainings, result);
        verify(authenticationService).authenticateTrainer(TRAINER_CREDENTIALS);
    }

    @Test
    void getTrainerTrainings_withWrongPassword_throwsAndDoesNotTouchDao() {
        when(authenticationService.authenticateTrainer(TRAINER_CREDENTIALS))
                .thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class,
                () -> service.getTrainerTrainings(TRAINER_CREDENTIALS, TrainerTrainingCriteria.any()));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void getTrainerTrainings_withNullCriteria_throwsAndDoesNotTouchDao() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerTrainings(TRAINER_CREDENTIALS, null));
        verifyNoInteractions(trainingDao);
    }
}
