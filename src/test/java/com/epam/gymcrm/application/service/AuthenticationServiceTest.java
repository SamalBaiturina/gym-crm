package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final Credentials TRAINEE_OK = new Credentials("John.Smith", "secret");
    private static final Credentials TRAINEE_WRONG = new Credentials("John.Smith", "wrong");
    private static final Credentials TRAINER_OK = new Credentials("Anna.Brown", "secret");
    private static final Credentials TRAINER_WRONG = new Credentials("Anna.Brown", "wrong");
    private static final Credentials NOBODY = new Credentials("nobody", "secret");

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @InjectMocks
    private AuthenticationService service;

    private Trainee trainee() {
        User user = new User("John", "Smith", "John.Smith", "secret", true);
        return new Trainee(1L, user, LocalDate.of(2000, 1, 1), "Astana");
    }

    private Trainer trainer() {
        User user = new User("Anna", "Brown", "Anna.Brown", "secret", true);
        return new Trainer(5L, user, new TrainingType("YOGA"));
    }

    // ---------- trainee ----------

    @Test
    void matchesTrainee_returnsTrue_forCorrectPassword() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));

        assertTrue(service.matchesTrainee(TRAINEE_OK));
    }

    @Test
    void matchesTrainee_returnsFalse_forWrongPassword() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));

        assertFalse(service.matchesTrainee(TRAINEE_WRONG));
    }

    @Test
    void matchesTrainee_returnsFalse_whenUserDoesNotExist() {
        when(traineeDao.findByUsername("nobody")).thenReturn(Optional.empty());

        assertFalse(service.matchesTrainee(NOBODY));
    }

    @Test
    void authenticateTrainee_returnsTheTrainee_forCorrectPassword() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));

        Trainee result = service.authenticateTrainee(TRAINEE_OK);

        assertEquals(1L, result.id());
    }

    @Test
    void authenticateTrainee_throws_forWrongPassword() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));

        assertThrows(AuthenticationException.class, () -> service.authenticateTrainee(TRAINEE_WRONG));
    }

    @Test
    void authenticateTrainee_throws_forUnknownUsername() {
        when(traineeDao.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> service.authenticateTrainee(NOBODY));
    }

    // ---------- trainer ----------

    @Test
    void matchesTrainer_returnsTrue_forCorrectPassword() {
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer()));

        assertTrue(service.matchesTrainer(TRAINER_OK));
    }

    @Test
    void matchesTrainer_returnsFalse_forWrongPasswordOrUnknownUser() {
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer()));
        when(trainerDao.findByUsername("nobody")).thenReturn(Optional.empty());

        assertFalse(service.matchesTrainer(TRAINER_WRONG));
        assertFalse(service.matchesTrainer(NOBODY));
    }

    @Test
    void authenticateTrainer_returnsTheTrainer_forCorrectPassword() {
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer()));

        Trainer result = service.authenticateTrainer(TRAINER_OK);

        assertEquals(5L, result.id());
    }

    @Test
    void authenticateTrainer_throws_forWrongPassword() {
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer()));

        assertThrows(AuthenticationException.class, () -> service.authenticateTrainer(TRAINER_WRONG));
    }

    @Test
    void authenticateTrainer_throws_forUnknownUsername() {
        when(trainerDao.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> service.authenticateTrainer(NOBODY));
    }
}
