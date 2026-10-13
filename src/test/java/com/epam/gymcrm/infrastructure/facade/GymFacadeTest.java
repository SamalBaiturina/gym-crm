package com.epam.gymcrm.infrastructure.facade;

import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;

    private GymFacade facade;

    private final Trainee trainee = new Trainee(1L, User.of("John", "Smith"), null, "Astana");
    private final Trainer trainer = new Trainer(1L, User.of("Anna", "Brown"), new TrainingType("YOGA"));
    private final Training training = new Training(1L, 1L, 1L, "Morning", new TrainingType("YOGA"),
            LocalDate.of(2026, 9, 1), 60);

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService);
    }

    @Test
    void delegatesTraineeOperations() {
        when(traineeService.create(trainee)).thenReturn(trainee);
        when(traineeService.update(trainee)).thenReturn(trainee);
        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeService.findAll()).thenReturn(List.of(trainee));

        assertEquals(trainee, facade.createTrainee(trainee));
        assertEquals(trainee, facade.updateTrainee(trainee));
        assertEquals(Optional.of(trainee), facade.getTrainee(1L));
        assertEquals(List.of(trainee), facade.getAllTrainees());
        facade.deleteTrainee(1L);
        verify(traineeService).delete(1L);
    }

    @Test
    void delegatesTrainerOperations() {
        when(trainerService.create(trainer)).thenReturn(trainer);
        when(trainerService.update(trainer)).thenReturn(trainer);
        when(trainerService.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerService.findAll()).thenReturn(List.of(trainer));

        assertEquals(trainer, facade.createTrainer(trainer));
        assertEquals(trainer, facade.updateTrainer(trainer));
        assertEquals(Optional.of(trainer), facade.getTrainer(1L));
        assertEquals(List.of(trainer), facade.getAllTrainers());
    }

    @Test
    void delegatesTrainingOperations() {
        when(trainingService.create(training)).thenReturn(training);
        when(trainingService.findById(1L)).thenReturn(Optional.of(training));
        when(trainingService.findAll()).thenReturn(List.of(training));

        assertEquals(training, facade.createTraining(training));
        assertEquals(Optional.of(training), facade.getTraining(1L));
        assertEquals(List.of(training), facade.getAllTrainings());
    }
}
