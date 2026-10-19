package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.PasswordChange;
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
    private TraineeService service;

    private Trainee existingTrainee(boolean active) {
        User user = new User("John", "Smith", "John.Smith", "secret", active);
        return new Trainee(1L, user, LocalDate.of(2000, 1, 1), "Astana");
    }

    private Trainee givenAuthenticatedTrainee(boolean active) {
        Trainee trainee = existingTrainee(active);
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenReturn(trainee);
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
        givenAuthenticatedTrainee(true);

        Trainee result = service.getByUsername(CREDENTIALS);

        assertEquals(1L, result.id());
    }

    @Test
    void getByUsername_withWrongPassword_throwsAndDoesNotTouchDao() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class, () -> service.getByUsername(CREDENTIALS));
        verifyNoInteractions(traineeDao);
    }

    // ---------- change password ----------

    @Test
    void changePassword_savesNewPassword() {
        givenAuthenticatedTrainee(true);

        service.changePassword(new PasswordChange(CREDENTIALS, "newPass"));

        assertEquals("newPass", savedTrainee().user().password());
    }

    @Test
    void changePassword_withWrongOldPassword_throwsAndSavesNothing() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class,
                () -> service.changePassword(new PasswordChange(CREDENTIALS, "newPass")));
        verify(traineeDao, never()).save(any());
    }

    // ---------- update ----------

    @Test
    void update_changesNamesBirthDateAndAddress_butKeepsCredentials() {
        givenAuthenticatedTrainee(true);
        Trainee changes = new Trainee(null, User.of("Johnny", "Smyth"), LocalDate.of(1999, 5, 5), "Almaty");

        service.update(CREDENTIALS, changes);

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
        givenAuthenticatedTrainee(true);
        Trainee changes = new Trainee(null, User.of("Johnny", ""), null, null);

        assertThrows(IllegalArgumentException.class, () -> service.update(CREDENTIALS, changes));
        verify(traineeDao, never()).save(any());
    }

    // ---------- activate / de-activate ----------

    @Test
    void toggleActive_deactivatesActiveTrainee() {
        givenAuthenticatedTrainee(true);

        boolean result = service.toggleActive(CREDENTIALS);

        assertFalse(result);
        assertFalse(savedTrainee().user().isActive());
    }

    @Test
    void toggleActive_activatesInactiveTrainee() {
        givenAuthenticatedTrainee(false);

        boolean result = service.toggleActive(CREDENTIALS);

        assertTrue(result);
        assertTrue(savedTrainee().user().isActive());
    }

    // ---------- delete ----------

    @Test
    void delete_removesTheAuthenticatedTrainee() {
        Trainee trainee = givenAuthenticatedTrainee(true);

        service.delete(CREDENTIALS);

        verify(traineeDao).delete(trainee);
    }

    @Test
    void delete_withWrongPassword_deletesNothing() {
        when(authenticationService.authenticateTrainee(CREDENTIALS)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class, () -> service.delete(CREDENTIALS));
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

        List<Trainer> result = service.getNotAssignedTrainers(CREDENTIALS);

        assertSame(trainers, result);
        verify(authenticationService).authenticateTrainee(CREDENTIALS);
    }

    @Test
    void updateTrainers_assignsFoundTrainers() {
        Trainee trainee = givenAuthenticatedTrainee(true);
        Trainer anna = trainer(5L, "Anna", "Brown");
        when(trainerDao.findByUsernames(java.util.Set.of("Anna.Brown"))).thenReturn(List.of(anna));

        List<Trainer> result = service.updateTrainers(CREDENTIALS, List.of("Anna.Brown"));

        assertEquals(List.of(anna), result);
        verify(traineeDao).updateTrainers(trainee, List.of(anna));
    }

    @Test
    void updateTrainers_withUnknownTrainer_throwsAndChangesNothing() {
        givenAuthenticatedTrainee(true);
        when(trainerDao.findByUsernames(java.util.Set.of("nobody"))).thenReturn(List.of());

        assertThrows(NoSuchElementException.class,
                () -> service.updateTrainers(CREDENTIALS, List.of("nobody")));
        verify(traineeDao, never()).updateTrainers(any(), anyList());
    }

    @Test
    void updateTrainers_withNullList_throws() {
        givenAuthenticatedTrainee(true);

        assertThrows(IllegalArgumentException.class, () -> service.updateTrainers(CREDENTIALS, null));
    }
}
