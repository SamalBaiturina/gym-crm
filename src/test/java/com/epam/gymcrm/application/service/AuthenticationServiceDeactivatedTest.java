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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceDeactivatedTest {

    private static final Credentials JOHN = new Credentials("John.Smith", "secret");
    private static final Credentials ANNA = new Credentials("Anna.Brown", "secret");

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @InjectMocks
    private AuthenticationService service;

    private Trainee trainee(boolean active) {
        return new Trainee(1L, new User("John", "Smith", "John.Smith", "secret", active), null, null);
    }

    private Trainer trainer(boolean active) {
        return new Trainer(5L, new User("Anna", "Brown", "Anna.Brown", "secret", active),
                new TrainingType("YOGA"));
    }

    @Test
    void deactivatedTraineeIsNotActive() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee(false)));

        assertFalse(service.authenticateAndLoad(JOHN).active());
    }

    @Test
    void activeTraineeIsActive() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee(true)));

        assertTrue(service.authenticateAndLoad(JOHN).active());
    }

    @Test
    void deactivatedTrainerIsNotActive() {
        when(traineeDao.findByUsername("Anna.Brown")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer(false)));

        assertFalse(service.authenticateAndLoad(ANNA).active());
    }

    @Test
    void wrongPasswordIsRejectedEvenForADeactivatedUser() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee(false)));
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> service.authenticateAndLoad(new Credentials("John.Smith", "wrong")));
    }
}
