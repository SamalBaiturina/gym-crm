package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @InjectMocks
    private TrainingService service;

    @Test
    void create_savesTraining_whenTraineeAndTrainerExist() {
        Training training = training(1L, 2L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(new Trainer()));
        when(trainingDao.save(training)).thenReturn(training);

        assertSame(training, service.create(training));
    }

    @Test
    void create_throws_whenTraineeMissing() {
        Training training = training(1L, 2L);
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.create(training));
        verify(trainingDao, never()).save(any());
    }

    @Test
    void create_throws_whenTrainerMissing() {
        Training training = training(1L, 2L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.create(training));
        verify(trainingDao, never()).save(any());
    }

    @Test
    void findById_andFindAll_delegateToDao() {
        Training training = new Training();
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));
        when(trainingDao.findAll()).thenReturn(List.of(training));

        assertEquals(Optional.of(training), service.findById(1L));
        assertEquals(List.of(training), service.findAll());
    }

    private Training training(Long traineeId, Long trainerId) {
        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        return training;
    }
}
