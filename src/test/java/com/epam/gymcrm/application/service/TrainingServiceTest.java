package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;

    private TrainingService service;

    private final Training training = new Training(null, 1L, 2L, "Morning", new TrainingType("YOGA"),
            LocalDate.of(2026, 9, 1), 60);

    @BeforeEach
    void setUp() {
        service = new TrainingService();
        service.setTrainingDao(trainingDao);
        service.setTraineeDao(traineeDao);
        service.setTrainerDao(trainerDao);
    }

    @Test
    void createSavesWhenTraineeAndTrainerExist() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(
                new Trainee(1L, User.of("John", "Smith"), null, null)));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(
                new Trainer(2L, User.of("Anna", "Brown"), new TrainingType("YOGA"))));
        when(trainingDao.save(training)).thenReturn(training.withId(1L));

        assertEquals(1L, service.create(training).id());
    }

    @Test
    void createThrowsWhenTraineeMissing() {
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.create(training));
        verify(trainingDao, never()).save(any());
    }

    @Test
    void createThrowsWhenTrainerMissing() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(
                new Trainee(1L, User.of("John", "Smith"), null, null)));
        when(trainerDao.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.create(training));
        verify(trainingDao, never()).save(any());
    }

    @Test
    void findByIdAndFindAllDelegateToDao() {
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training.withId(1L)));
        when(trainingDao.findAll()).thenReturn(List.of(training.withId(1L)));

        assertEquals(Optional.of(training.withId(1L)), service.findById(1L));
        assertEquals(1, service.findAll().size());
    }
}
