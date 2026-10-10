package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;

    private UsernameGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new UsernameGenerator();
        generator.setTraineeDao(traineeDao);
        generator.setTrainerDao(trainerDao);
        lenient().when(traineeDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of());
        lenient().when(trainerDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of());
    }

    @Test
    void returnsBaseWhenFree() {
        assertEquals("John.Smith", generator.generate("John", "Smith"));
    }

    @Test
    void addsSerialWhenTakenByTrainee() {
        lenient().when(traineeDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of("John.Smith"));

        assertEquals("John.Smith1", generator.generate("John", "Smith"));
    }

    @Test
    void takesIntoAccountTrainersAndExistingSerials() {
        lenient().when(traineeDao.findUsernamesStartingWith("John.Smith"))
                .thenReturn(List.of("John.Smith", "John.Smith1"));
        lenient().when(trainerDao.findUsernamesStartingWith("John.Smith"))
                .thenReturn(List.of("John.Smith2"));

        assertEquals("John.Smith3", generator.generate("John", "Smith"));
    }
}
