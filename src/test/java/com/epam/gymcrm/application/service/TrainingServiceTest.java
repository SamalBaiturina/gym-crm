package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.exception.ForbiddenOperationException;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 1, 10);
    private static final AuthenticatedUser TRAINER_USER =
            new AuthenticatedUser(new Credentials("Anna.Brown", "secret"), Role.TRAINER, true);
    private static final AuthenticatedUser TRAINEE_USER =
            new AuthenticatedUser(new Credentials("John.Smith", "secret"), Role.TRAINEE, true);

    @Mock
    private TrainingDao trainingDao;
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

    // ---------- lists ----------

    @Test
    void getTraineeTrainings_passesCriteriaToDao() {
        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(DATE, LocalDate.of(2026, 12, 31), "Anna", "YOGA");
        List<Training> trainings = List.of(training());
        when(trainingDao.findForTrainee("John.Smith", criteria)).thenReturn(trainings);

        List<Training> result = service.getTraineeTrainings(TRAINEE_USER, criteria);

        assertSame(trainings, result);
    }

    @Test
    void getTraineeTrainings_forTrainerUser_isForbiddenAndDoesNotTouchDao() {
        assertThrows(ForbiddenOperationException.class,
                () -> service.getTraineeTrainings(TRAINER_USER, TraineeTrainingCriteria.any()));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void getTraineeTrainings_withNullCriteria_throwsAndDoesNotTouchDao() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getTraineeTrainings(TRAINEE_USER, null));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void getTrainerTrainings_passesCriteriaToDao() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(DATE, null, "John");
        List<Training> trainings = List.of(training());
        when(trainingDao.findForTrainer("Anna.Brown", criteria)).thenReturn(trainings);

        List<Training> result = service.getTrainerTrainings(TRAINER_USER, criteria);

        assertSame(trainings, result);
    }

    @Test
    void getTrainerTrainings_forTraineeUser_isForbiddenAndDoesNotTouchDao() {
        assertThrows(ForbiddenOperationException.class,
                () -> service.getTrainerTrainings(TRAINEE_USER, TrainerTrainingCriteria.any()));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void getTrainerTrainings_withNullCriteria_throwsAndDoesNotTouchDao() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getTrainerTrainings(TRAINER_USER, null));
        verifyNoInteractions(trainingDao);
    }
}
