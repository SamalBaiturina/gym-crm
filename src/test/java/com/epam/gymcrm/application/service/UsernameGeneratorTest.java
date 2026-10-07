package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @InjectMocks
    private UsernameGenerator generator;

    @Test
    void generate_returnsBaseUsername_whenFree() {
        assertEquals("John.Smith", generator.generate("John", "Smith"));
    }

    @Test
    void generate_addsSerial_whenTakenByTrainee() {
        when(traineeDao.findAll()).thenReturn(List.of(trainee("John.Smith")));

        assertEquals("John.Smith1", generator.generate("John", "Smith"));
    }

    @Test
    void generate_skipsTakenSerials_acrossTraineesAndTrainers() {
        when(traineeDao.findAll()).thenReturn(List.of(trainee("John.Smith")));
        when(trainerDao.findAll()).thenReturn(List.of(trainer("John.Smith1")));

        assertEquals("John.Smith2", generator.generate("John", "Smith"));
    }

    private Trainee trainee(String username) {
        Trainee t = new Trainee();
        t.setUsername(username);
        return t;
    }

    private Trainer trainer(String username) {
        Trainer t = new Trainer();
        t.setUsername(username);
        return t;
    }
}