package com.epam.gymcrm.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void equals_isBasedOnId() {
        Trainee a = new Trainee();
        a.setId(1L);
        Trainee b = new Trainee();
        b.setId(1L);
        Trainee c = new Trainee();
        c.setId(2L);

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, null);
    }

    @Test
    void equals_differentClassesWithSameId_areNotEqual() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        Trainer trainer = new Trainer();
        trainer.setId(1L);

        assertNotEquals(trainee, trainer);
    }

    @Test
    void traineeToString_hidesPassword() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setFirstName("John");
        trainee.setLastName("Smith");
        trainee.setUsername("John.Smith");
        trainee.setPassword("secret12345");
        trainee.setActive(true);
        trainee.setDateOfBirth(LocalDate.of(1990, 5, 15));
        trainee.setAddress("Astana");

        String text = trainee.toString();

        assertTrue(text.contains("John.Smith"));
        assertTrue(text.contains("Astana"));
        assertFalse(text.contains("secret12345"));
    }

    @Test
    void trainerToString_hidesPassword() {
        Trainer trainer = new Trainer();
        trainer.setUsername("Anna.Brown");
        trainer.setPassword("secret12345");
        trainer.setSpecialization(new TrainingType("YOGA"));

        String text = trainer.toString();

        assertTrue(text.contains("YOGA"));
        assertFalse(text.contains("secret12345"));
    }

    @Test
    void trainingGettersAndToString() {
        Training training = new Training();
        training.setId(5L);
        training.setTraineeId(1L);
        training.setTrainerId(2L);
        training.setTrainingName("Yoga");
        training.setTrainingType(new TrainingType("YOGA"));
        training.setTrainingDate(LocalDate.of(2026, 9, 1));
        training.setTrainingDuration(60);

        assertEquals(1L, training.getTraineeId());
        assertEquals(2L, training.getTrainerId());
        assertEquals("Yoga", training.getTrainingName());
        assertEquals(new TrainingType("YOGA"), training.getTrainingType());
        assertEquals(LocalDate.of(2026, 9, 1), training.getTrainingDate());
        assertEquals(60, training.getTrainingDuration());
        assertTrue(training.toString().contains("id=5"));
    }

    @Test
    void trainingType_equalsAndHashCodeAreBasedOnName() {
        TrainingType a = new TrainingType("Yoga");
        TrainingType b = new TrainingType("Yoga");
        TrainingType c = new TrainingType("Zumba");

        assertEquals("Yoga", a.getName());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertTrue(a.toString().contains("Yoga"));
    }
}