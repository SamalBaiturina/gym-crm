package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceRoleTest {

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

    @Test
    void traineeCredentialsGiveTraineeRole() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));

        assertEquals(Role.TRAINEE, service.authenticate(new Credentials("John.Smith", "secret")));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void trainerCredentialsGiveTrainerRole() {
        when(traineeDao.findByUsername("Anna.Brown")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer()));

        assertEquals(Role.TRAINER, service.authenticate(new Credentials("Anna.Brown", "secret")));
    }

    @Test
    void wrongPasswordThrows() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> service.authenticate(new Credentials("John.Smith", "wrong")));
    }

    @Test
    void unknownUserThrows() {
        when(traineeDao.findByUsername("nobody")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> service.authenticate(new Credentials("nobody", "secret")));
    }
}
