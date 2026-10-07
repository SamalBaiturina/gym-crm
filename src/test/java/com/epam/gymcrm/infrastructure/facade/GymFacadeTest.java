package com.epam.gymcrm.infrastructure.facade;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.infrastructure.facade.GymFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    @InjectMocks
    private GymFacade facade;

    @Test
    void traineeOperations_delegateToService() {
        Trainee trainee = new Trainee();
        when(traineeService.create(trainee)).thenReturn(trainee);
        when(traineeService.update(trainee)).thenReturn(trainee);
        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeService.findAll()).thenReturn(List.of(trainee));

        assertSame(trainee, facade.createTrainee(trainee));
        assertSame(trainee, facade.updateTrainee(trainee));
        assertEquals(Optional.of(trainee), facade.getTrainee(1L));
        assertEquals(List.of(trainee), facade.getAllTrainees());
        facade.deleteTrainee(1L);
        verify(traineeService).delete(1L);
    }

    @Test
    void trainerOperations_delegateToService() {
        Trainer trainer = new Trainer();
        when(trainerService.create(trainer)).thenReturn(trainer);
        when(trainerService.update(trainer)).thenReturn(trainer);
        when(trainerService.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerService.findAll()).thenReturn(List.of(trainer));

        assertSame(trainer, facade.createTrainer(trainer));
        assertSame(trainer, facade.updateTrainer(trainer));
        assertEquals(Optional.of(trainer), facade.getTrainer(1L));
        assertEquals(List.of(trainer), facade.getAllTrainers());
    }

    @Test
    void trainingOperations_delegateToService() {
        Training training = new Training();
        when(trainingService.create(training)).thenReturn(training);
        when(trainingService.findById(1L)).thenReturn(Optional.of(training));
        when(trainingService.findAll()).thenReturn(List.of(training));

        assertSame(training, facade.createTraining(training));
        assertEquals(Optional.of(training), facade.getTraining(1L));
        assertEquals(List.of(training), facade.getAllTrainings());
    }
}