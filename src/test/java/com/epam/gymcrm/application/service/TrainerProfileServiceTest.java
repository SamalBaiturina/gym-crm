package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerProfileServiceTest {

    private static final Credentials CREDENTIALS = new Credentials("Anna.Brown", "secret");
    private static final AuthenticatedUser USER = new AuthenticatedUser(CREDENTIALS, Role.TRAINER, true);

    @Mock
    private TrainerDao trainerDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @InjectMocks
    private TrainerService service;

    private Trainer trainer() {
        User user = new User("Anna", "Brown", "Anna.Brown", "secret", true);
        return new Trainer(5L, user, new TrainingType(2L, "YOGA"));
    }

    private Trainee trainee() {
        User user = new User("John", "Smith", "John.Smith", "secret", true);
        return new Trainee(1L, user, LocalDate.of(2000, 1, 1), "Astana");
    }

    @Test
    void getProfileReturnsTheTrainerWithHisTrainees() {
        Trainer trainer = trainer();
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer));
        when(traineeDao.findAssignedTo("Anna.Brown")).thenReturn(List.of(trainee()));

        TrainerProfile profile = service.getProfile(USER);

        assertSame(trainer, profile.trainer());
        assertEquals(List.of(trainee()), profile.trainees());
    }

    @Test
    void updateProfileChangesNamesAndActiveFlagAndKeepsTheSpecialization() {
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer()));
        when(trainerDao.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(traineeDao.findAssignedTo("Anna.Brown")).thenReturn(List.of(trainee()));

        TrainerProfile profile = service.updateProfile(USER, "Annie", "Braun", false);

        User user = profile.trainer().user();
        assertEquals("Annie", user.firstName());
        assertEquals("Braun", user.lastName());
        assertEquals("Anna.Brown", user.username());
        assertFalse(user.isActive());
        assertEquals("YOGA", profile.trainer().specialization().name());
        assertEquals(1, profile.trainees().size());
    }

    @Test
    void updateProfileRejectsBlankName() {
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer()));

        assertThrows(ValidationException.class, () -> service.updateProfile(USER, "Annie", " ", true));
        verify(trainerDao, never()).save(any());
    }
}
