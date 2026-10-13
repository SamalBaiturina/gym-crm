package com.epam.gymcrm.infrastructure.storage;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageInitializerTest {

    private StorageInitializer initializer(String file) {
        StorageInitializer initializer = new StorageInitializer();
        initializer.setInitFile(file);
        return initializer;
    }

    @Test
    void loadsTrainees() {
        Map<Long, Trainee> storage = new HashMap<>();

        initializer("data/test-init-data.csv").postProcessAfterInitialization(storage, "traineeStorage");

        Trainee trainee = storage.get(1L);
        assertEquals("John.Smith", trainee.user().username());
        assertEquals(LocalDate.of(1990, 5, 15), trainee.dateOfBirth());
        assertEquals("Astana Abay 1", trainee.address());
    }

    @Test
    void loadsTrainers() {
        Map<Long, Trainer> storage = new HashMap<>();

        initializer("data/test-init-data.csv").postProcessAfterInitialization(storage, "trainerStorage");

        assertEquals("Anna.Brown", storage.get(1L).user().username());
        assertEquals(new TrainingType("YOGA"), storage.get(1L).specialization());
    }

    @Test
    void loadsTrainings() {
        Map<Long, Training> storage = new HashMap<>();

        initializer("data/test-init-data.csv").postProcessAfterInitialization(storage, "trainingStorage");

        Training training = storage.get(1L);
        assertEquals("Morning yoga", training.trainingName());
        assertEquals(new TrainingType("YOGA"), training.trainingType());
        assertEquals(60, training.trainingDuration());
    }

    @Test
    void ignoresOtherBeans() {
        Object bean = new Object();

        assertSame(bean, initializer("data/test-init-data.csv").postProcessAfterInitialization(bean, "other"));
    }

    @Test
    void throwsWhenFileMissing() {
        Map<Long, Trainee> storage = new HashMap<>();
        StorageInitializer initializer = initializer("data/missing.csv");

        assertThrows(IllegalStateException.class,
                () -> initializer.postProcessAfterInitialization(storage, "traineeStorage"));
        assertTrue(storage.isEmpty());
    }
}
