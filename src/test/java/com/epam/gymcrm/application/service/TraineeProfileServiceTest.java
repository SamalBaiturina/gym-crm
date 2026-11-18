package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.exception.ForbiddenOperationException;
import com.epam.gymcrm.domain.exception.ValidationException;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeProfileServiceTest {

    private static final Credentials CREDENTIALS = new Credentials("John.Smith", "secret");
    private static final AuthenticatedUser USER = new AuthenticatedUser(CREDENTIALS, Role.TRAINEE, true);

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @InjectMocks
    private TraineeService service;

    private Trainee trainee() {
        User user = new User("John", "Smith", "John.Smith", "secret", true);
        return new Trainee(1L, user, LocalDate.of(2000, 1, 1), "Astana");
    }

    private Trainer trainer() {
        User user = new User("Anna", "Brown", "Anna.Brown", "secret", true);
        return new Trainer(5L, user, new TrainingType(2L, "YOGA"));
    }

    @Test
    void getProfileReturnsTheTraineeWithHisTrainers() {
        Trainee trainee = trainee();
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findAssignedTo("John.Smith")).thenReturn(List.of(trainer()));

        TraineeProfile profile = service.getProfile(USER);

        assertSame(trainee, profile.trainee());
        assertEquals(List.of(trainer()), profile.trainers());
    }

    @Test
    void getProfileOfTrainerUserIsForbidden() {
        AuthenticatedUser trainerUser = new AuthenticatedUser(CREDENTIALS, Role.TRAINER, true);

        assertThrows(ForbiddenOperationException.class, () -> service.getProfile(trainerUser));
        verifyNoInteractions(traineeDao, trainerDao);
    }

    @Test
    void updateProfileChangesNamesFieldsAndActiveFlag() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));
        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trainerDao.findAssignedTo("John.Smith")).thenReturn(List.of(trainer()));
        UpdateTrainee changes = new UpdateTrainee("Johnny", "Smythe", LocalDate.of(1999, 5, 5), "Almaty", false);

        TraineeProfile profile = service.updateProfile(USER, changes);

        User user = profile.trainee().user();
        assertEquals("Johnny", user.firstName());
        assertEquals("Smythe", user.lastName());
        assertEquals("John.Smith", user.username());
        assertEquals("secret", user.password());
        assertFalse(user.isActive());
        assertEquals(LocalDate.of(1999, 5, 5), profile.trainee().dateOfBirth());
        assertEquals("Almaty", profile.trainee().address());
        assertEquals(1, profile.trainers().size());
    }

    @Test
    void updateProfileRejectsBlankName() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));
        UpdateTrainee changes = new UpdateTrainee(" ", "Smythe", null, null, true);

        assertThrows(ValidationException.class, () -> service.updateProfile(USER, changes));
        verify(traineeDao, never()).save(any());
    }
}
