package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.UsageMetrics;
import com.epam.gymcrm.domain.exception.ForbiddenOperationException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
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

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    private static final Credentials CREDENTIALS = new Credentials("John.Smith", "secret");
    private static final AuthenticatedUser USER = new AuthenticatedUser(CREDENTIALS, Role.TRAINEE, true);
    private static final AuthenticatedUser TRAINER_USER = new AuthenticatedUser(CREDENTIALS, Role.TRAINER, true);

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private UsageMetrics usageMetrics;
    @InjectMocks
    private TraineeService service;

    private Trainee existingTrainee(boolean active) {
        User user = new User("John", "Smith", "John.Smith", "secret", active);
        return new Trainee(1L, user, LocalDate.of(2000, 1, 1), "Astana");
    }

    private Trainee givenStoredTrainee(boolean active) {
        Trainee trainee = existingTrainee(active);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        return trainee;
    }

    private Trainee savedTrainee() {
        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDao).save(captor.capture());
        return captor.getValue();
    }

    // ---------- create ----------

    @Test
    void create_generatesCredentialsAndSaves() {
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("pass123456");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee toSave = invocation.getArgument(0);
            return toSave.withId(1L);
        });

        Trainee result = service.create(new Trainee(null, User.of("John", "Smith"), null, null));

        assertEquals(1L, result.id());
        assertEquals("John.Smith", result.user().username());
        assertEquals("pass123456", result.user().password());
        assertTrue(result.user().isActive());
    }

    @Test
    void register_buildsTheTraineeFromTheCommandAndCreatesIt() {
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("pass123456");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee toSave = invocation.getArgument(0);
            return toSave.withId(1L);
        });

        Trainee result = service.register(new RegisterTrainee("John", "Smith", LocalDate.of(2000, 1, 1), "Astana"));

        assertEquals(1L, result.id());
        assertEquals("John.Smith", result.user().username());
        assertEquals(LocalDate.of(2000, 1, 1), result.dateOfBirth());
        assertEquals("Astana", result.address());
    }

    @Test
    void register_withNullCommand_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null));
        verifyNoInteractions(traineeDao);
    }

    @Test
    void create_withBlankFirstName_throwsAndSavesNothing() {
        Trainee invalid = new Trainee(null, User.of(" ", "Smith"), null, null);

        assertThrows(IllegalArgumentException.class, () -> service.create(invalid));
        verifyNoInteractions(traineeDao);
    }

    @Test
    void create_withNullTrainee_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.create(null));
    }

    // ---------- get / authentication ----------

    @Test
    void getByUsername_returnsAuthenticatedTrainee() {
        givenStoredTrainee(true);

        Trainee result = service.getByUsername(USER);

        assertEquals(1L, result.id());
    }

    @Test
    void getByUsername_forTrainerUser_isForbiddenAndDoesNotTouchDao() {
        assertThrows(ForbiddenOperationException.class, () -> service.getByUsername(TRAINER_USER));
        verifyNoInteractions(traineeDao);
    }

    @Test
    void getByUsername_whenTheAccountIsGone_throwsNotFound() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getByUsername(USER));
    }

    // ---------- change password ----------

    @Test
    void changePassword_savesNewPassword() {
        givenStoredTrainee(true);

        service.changePassword(USER, "newPass");

        assertEquals("newPass", savedTrainee().user().password());
    }

    @Test
    void changePassword_withBlankPassword_throwsAndSavesNothing() {
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(USER, " "));
        verify(traineeDao, never()).save(any());
    }

    // ---------- update ----------

    @Test
    void update_changesNamesBirthDateAndAddress_butKeepsCredentials() {
        givenStoredTrainee(true);
        Trainee changes = new Trainee(null, User.of("Johnny", "Smyth"), LocalDate.of(1999, 5, 5), "Almaty");

        service.update(USER, changes);

        Trainee saved = savedTrainee();
        assertEquals(1L, saved.id());
        assertEquals("Johnny", saved.user().firstName());
        assertEquals("Smyth", saved.user().lastName());
        assertEquals("John.Smith", saved.user().username());
        assertEquals("secret", saved.user().password());
        assertEquals(LocalDate.of(1999, 5, 5), saved.dateOfBirth());
        assertEquals("Almaty", saved.address());
    }

    @Test
    void update_withBlankLastName_throwsAndSavesNothing() {
        givenStoredTrainee(true);
        Trainee changes = new Trainee(null, User.of("Johnny", ""), null, null);

        assertThrows(IllegalArgumentException.class, () -> service.update(USER, changes));
        verify(traineeDao, never()).save(any());
    }

    // ---------- activate / de-activate ----------

    @Test
    void toggleActive_deactivatesActiveTrainee() {
        givenStoredTrainee(true);

        boolean result = service.toggleActive(USER);

        assertFalse(result);
        assertFalse(savedTrainee().user().isActive());
    }

    @Test
    void toggleActive_activatesInactiveTrainee() {
        givenStoredTrainee(false);

        boolean result = service.toggleActive(USER);

        assertTrue(result);
        assertTrue(savedTrainee().user().isActive());
    }

    // ---------- delete ----------

    @Test
    void delete_removesTheAuthenticatedTrainee() {
        Trainee trainee = givenStoredTrainee(true);

        service.delete(USER);

        verify(traineeDao).delete(trainee);
    }

    @Test
    void delete_forTrainerUser_isForbiddenAndDeletesNothing() {
        assertThrows(ForbiddenOperationException.class, () -> service.delete(TRAINER_USER));
        verify(traineeDao, never()).delete(any());
    }

    // ---------- trainers ----------

    private Trainer trainer(long id, String first, String last) {
        return new Trainer(id, new User(first, last, first + "." + last, "x", true), new TrainingType("YOGA"));
    }

    @Test
    void getNotAssignedTrainers_returnsTrainersFromDao() {
        List<Trainer> trainers = List.of(trainer(5L, "Anna", "Brown"));
        when(trainerDao.findNotAssignedTo("John.Smith")).thenReturn(trainers);

        List<Trainer> result = service.getNotAssignedTrainers(USER);

        assertSame(trainers, result);
    }

    @Test
    void getNotAssignedTrainers_forTrainerUser_isForbidden() {
        assertThrows(ForbiddenOperationException.class, () -> service.getNotAssignedTrainers(TRAINER_USER));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void updateTrainers_assignsFoundTrainers() {
        Trainee trainee = givenStoredTrainee(true);
        Trainer anna = trainer(5L, "Anna", "Brown");
        when(trainerDao.findByUsernames(java.util.Set.of("Anna.Brown"))).thenReturn(List.of(anna));

        List<Trainer> result = service.updateTrainers(USER, List.of("Anna.Brown"));

        assertEquals(List.of(anna), result);
        verify(traineeDao).updateTrainers(trainee, List.of(anna));
    }

    @Test
    void updateTrainers_withUnknownTrainer_throwsAndChangesNothing() {
        givenStoredTrainee(true);
        when(trainerDao.findByUsernames(java.util.Set.of("nobody"))).thenReturn(List.of());

        assertThrows(NoSuchElementException.class,
                () -> service.updateTrainers(USER, List.of("nobody")));
        verify(traineeDao, never()).updateTrainers(any(), anyList());
    }

    @Test
    void updateTrainers_withNullList_throws() {
        givenStoredTrainee(true);

        assertThrows(IllegalArgumentException.class, () -> service.updateTrainers(USER, null));
    }
}
