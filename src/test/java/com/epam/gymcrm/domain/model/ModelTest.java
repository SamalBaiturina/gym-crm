package com.epam.gymcrm.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelTest {

    @Test
    void userOfHasNoCredentials() {
        User user = User.of("John", "Smith");

        assertEquals("John", user.firstName());
        assertEquals("Smith", user.lastName());
        assertNull(user.username());
        assertNull(user.password());
        assertFalse(user.isActive());
    }

    @Test
    void withCredentialsReturnsActiveCopy() {
        User user = User.of("John", "Smith").withCredentials("John.Smith", "secret");

        assertEquals("John.Smith", user.username());
        assertEquals("secret", user.password());
        assertTrue(user.isActive());
    }

    @Test
    void userToStringHidesPassword() {
        User user = User.of("John", "Smith").withCredentials("John.Smith", "secret");

        assertFalse(user.toString().contains("secret"));
        assertTrue(user.toString().contains("John.Smith"));
    }

    @Test
    void traineeWithIdAndWithUserKeepOtherFields() {
        Trainee trainee = new Trainee(null, User.of("John", "Smith"), LocalDate.of(2000, 1, 1), "Astana");

        Trainee withId = trainee.withId(5L);
        Trainee withUser = withId.withUser(User.of("Jane", "Doe"));

        assertNull(trainee.id());
        assertEquals(5L, withId.id());
        assertEquals("Astana", withId.address());
        assertEquals("Jane", withUser.user().firstName());
        assertEquals(5L, withUser.id());
        assertEquals(LocalDate.of(2000, 1, 1), withUser.dateOfBirth());
    }

    @Test
    void trainerWithIdAndWithUserKeepOtherFields() {
        Trainer trainer = new Trainer(null, User.of("Anna", "Brown"), new TrainingType("YOGA"));

        Trainer withId = trainer.withId(3L);
        Trainer withUser = withId.withUser(User.of("Eva", "Green"));

        assertEquals(3L, withId.id());
        assertEquals(new TrainingType("YOGA"), withId.specialization());
        assertEquals("Eva", withUser.user().firstName());
        assertEquals(3L, withUser.id());
    }

    @Test
    void trainingWithIdKeepsOtherFields() {
        Training training = new Training(null, 1L, 2L, "Morning", new TrainingType("YOGA"),
                LocalDate.of(2026, 9, 1), 60);

        Training withId = training.withId(7L);

        assertEquals(7L, withId.id());
        assertEquals(1L, withId.traineeId());
        assertEquals(2L, withId.trainerId());
        assertEquals("Morning", withId.trainingName());
        assertEquals(60, withId.trainingDuration());
        assertEquals(LocalDate.of(2026, 9, 1), withId.trainingDate());
    }
}
