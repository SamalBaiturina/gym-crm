package com.epam.gymcrm.infrastructure.storage;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.infrastructure.storage.StorageInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StorageInitializerTest {

    private StorageInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new StorageInitializer();
        initializer.setInitFile("data/test-init-data.csv");
    }

    @Test
    void fillsTraineeStorage() {
        Map<Long, Trainee> storage = new HashMap<>();

        initializer.postProcessAfterInitialization(storage, "traineeStorage");

        assertEquals(1, storage.size());
        Trainee trainee = storage.get(1L);
        assertEquals("John.Smith", trainee.getUsername());
        assertEquals(LocalDate.of(1990, 5, 15), trainee.getDateOfBirth());
        assertEquals("Astana Abay 1", trainee.getAddress());
    }

    @Test
    void fillsTrainerStorage() {
        Map<Long, Trainer> storage = new HashMap<>();

        initializer.postProcessAfterInitialization(storage, "trainerStorage");

        assertEquals(new TrainingType("YOGA"), storage.get(1L).getSpecialization());
    }

    @Test
    void fillsTrainingStorage() {
        Map<Long, Training> storage = new HashMap<>();

        initializer.postProcessAfterInitialization(storage, "trainingStorage");

        Training training = storage.get(1L);
        assertEquals("Morning yoga", training.getTrainingName());
        assertEquals(60, training.getTrainingDuration());
    }

    @Test
    void ignoresOtherBeans() {
        Object bean = new Object();

        assertSame(bean, initializer.postProcessAfterInitialization(bean, "someOtherBean"));
    }

    @Test
    void throwsWhenFileMissing() {
        initializer.setInitFile("data/missing.csv");

        assertThrows(IllegalStateException.class,
                () -> initializer.postProcessAfterInitialization(new HashMap<Long, Trainee>(), "traineeStorage"));
    }
}