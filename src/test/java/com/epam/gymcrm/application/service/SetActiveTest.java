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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetActiveTest {

    private static final Credentials JOHN = new Credentials("John.Smith", "secret");
    private static final Credentials ANNA = new Credentials("Anna.Brown", "secret");

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private TraineeService traineeService;
    @InjectMocks
    private TrainerService trainerService;

    private Trainee trainee(boolean active) {
        return new Trainee(1L, new User("John", "Smith", "John.Smith", "secret", active), null, null);
    }

    private Trainer trainer(boolean active) {
        return new Trainer(5L, new User("Anna", "Brown", "Anna.Brown", "secret", active),
                new TrainingType(2L, "YOGA"));
    }

    @Test
    void traineeCanBeDeactivated() {
        when(authenticationService.authenticateTrainee(JOHN)).thenReturn(trainee(true));

        traineeService.setActive(JOHN, false);

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDao).save(captor.capture());
        assertFalse(captor.getValue().user().isActive());
    }

    @Test
    void settingTheSameValueTwiceKeepsIt() {
        when(authenticationService.authenticateTrainee(JOHN)).thenReturn(trainee(true));

        traineeService.setActive(JOHN, true);
        traineeService.setActive(JOHN, true);

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDao, times(2)).save(captor.capture());
        captor.getAllValues().forEach(saved -> assertTrue(saved.user().isActive()));
    }

    @Test
    void trainerCanBeDeactivated() {
        when(authenticationService.authenticateTrainer(ANNA)).thenReturn(trainer(true));

        trainerService.setActive(ANNA, false);

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDao).save(captor.capture());
        assertFalse(captor.getValue().user().isActive());
    }

    @Test
    void wrongPasswordChangesNothing() {
        when(authenticationService.authenticateTrainee(JOHN)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class, () -> traineeService.setActive(JOHN, false));
        verify(traineeDao, never()).save(any());
    }
}
