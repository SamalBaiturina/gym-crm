package com.epam.gymcrm.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void passwordMatches_isTrueOnlyForTheSamePassword() {
        User user = User.of("John", "Smith").withCredentials("John.Smith", "secret");

        assertTrue(user.passwordMatches("secret"));
        assertFalse(user.passwordMatches("wrong"));
        assertFalse(user.passwordMatches(null));
    }

    @Test
    void passwordMatches_isFalseWhenUserHasNoPassword() {
        assertFalse(User.of("John", "Smith").passwordMatches("secret"));
    }

    @Test
    void credentialsKeepUsernameAndPassword() {
        Credentials credentials = new Credentials("John.Smith", "secret");

        assertEquals("John.Smith", credentials.username());
        assertEquals("secret", credentials.password());
    }

    @Test
    void credentialsRequireUsernameAndPassword() {
        assertThrows(IllegalArgumentException.class, () -> new Credentials(null, "secret"));
        assertThrows(IllegalArgumentException.class, () -> new Credentials(" ", "secret"));
        assertThrows(IllegalArgumentException.class, () -> new Credentials("John.Smith", null));
        assertThrows(IllegalArgumentException.class, () -> new Credentials("John.Smith", ""));
    }

    @Test
    void credentialsToStringHidesPassword() {
        Credentials credentials = new Credentials("John.Smith", "secret");

        assertFalse(credentials.toString().contains("secret"));
        assertTrue(credentials.toString().contains("John.Smith"));
    }

    @Test
    void passwordChangeKeepsCurrentCredentialsAndNewPassword() {
        Credentials current = new Credentials("John.Smith", "secret");

        PasswordChange change = new PasswordChange(current, "newPass");

        assertEquals(current, change.current());
        assertEquals("newPass", change.newPassword());
    }

    @Test
    void passwordChangeRequiresCurrentCredentialsAndNewPassword() {
        Credentials current = new Credentials("John.Smith", "secret");

        assertThrows(IllegalArgumentException.class, () -> new PasswordChange(null, "newPass"));
        assertThrows(IllegalArgumentException.class, () -> new PasswordChange(current, null));
        assertThrows(IllegalArgumentException.class, () -> new PasswordChange(current, " "));
    }

    @Test
    void passwordChangeToStringHidesPasswords() {
        PasswordChange change = new PasswordChange(new Credentials("John.Smith", "secret"), "newPass");

        assertFalse(change.toString().contains("secret"));
        assertFalse(change.toString().contains("newPass"));
        assertTrue(change.toString().contains("John.Smith"));
    }

    @Test
    void traineeCriteria_anyHasNoFilters() {
        TraineeTrainingCriteria criteria = TraineeTrainingCriteria.any();

        assertNull(criteria.from());
        assertNull(criteria.to());
        assertNull(criteria.trainerName());
        assertNull(criteria.trainingType());
    }

    @Test
    void traineeCriteria_rejectsPeriodWhereFromIsAfterTo() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 2, 1);

        assertThrows(IllegalArgumentException.class, () -> new TraineeTrainingCriteria(from, to, null, null));
    }

    @Test
    void traineeCriteria_acceptsSameDayAndOpenEndedPeriods() {
        LocalDate day = LocalDate.of(2026, 3, 1);

        assertDoesNotThrow(() -> new TraineeTrainingCriteria(day, day, null, null));
        assertDoesNotThrow(() -> new TraineeTrainingCriteria(day, null, null, null));
        assertDoesNotThrow(() -> new TraineeTrainingCriteria(null, day, null, null));
    }

    @Test
    void trainerCriteria_anyHasNoFilters() {
        TrainerTrainingCriteria criteria = TrainerTrainingCriteria.any();

        assertNull(criteria.from());
        assertNull(criteria.to());
        assertNull(criteria.traineeName());
    }

    @Test
    void trainerCriteria_rejectsPeriodWhereFromIsAfterTo() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 2, 1);

        assertThrows(IllegalArgumentException.class, () -> new TrainerTrainingCriteria(from, to, null));
    }

    @Test
    void trainerCriteria_acceptsSameDayAndOpenEndedPeriods() {
        LocalDate day = LocalDate.of(2026, 3, 1);

        assertDoesNotThrow(() -> new TrainerTrainingCriteria(day, day, null));
        assertDoesNotThrow(() -> new TrainerTrainingCriteria(day, null, null));
        assertDoesNotThrow(() -> new TrainerTrainingCriteria(null, day, null));
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
        Trainee trainee = new Trainee(1L, User.of("John", "Smith"), null, null);
        Trainer trainer = new Trainer(2L, User.of("Anna", "Brown"), new TrainingType("YOGA"));
        Training training = new Training(null, trainee, trainer, "Morning", new TrainingType("YOGA"),
                LocalDate.of(2026, 9, 1), 60);

        Training withId = training.withId(7L);

        assertNull(training.id());
        assertEquals(7L, withId.id());
        assertEquals(trainee, withId.trainee());
        assertEquals(trainer, withId.trainer());
        assertEquals("Morning", withId.trainingName());
        assertEquals(60, withId.trainingDuration());
        assertEquals(LocalDate.of(2026, 9, 1), withId.trainingDate());
    }
}
