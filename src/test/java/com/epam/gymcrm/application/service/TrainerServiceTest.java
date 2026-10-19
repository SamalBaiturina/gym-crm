package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.PasswordChange;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    private static final Credentials CREDENTIALS = new Credentials("Anna.Brown", "secret");

    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private TrainerService service;

    private void givenAuthenticatedTrainer(boolean active) {
        User user = new User("Anna", "Brown", "Anna.Brown", "secret", active);
        when(authenticationService.authenticateTrainer(CREDENTIALS))
                .thenReturn(new Trainer(5L, user, new TrainingType("YOGA")));
    }

    private Trainer savedTrainer() {
        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDao).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void create_generatesCredentialsAndSaves() {
        when(usernameGenerator.generate("Anna", "Brown")).thenReturn("Anna.Brown");
        when(passwordGenerator.generate()).thenReturn("pass123456");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer toSave = invocation.getArgument(0);
            return toSave.withId(5L);
        });

        Trainer result = service.create(new Trainer(null, User.of("Anna", "Brown"), new TrainingType("YOGA")));

        assertEquals(5L, result.id());
        assertEquals("Anna.Brown", result.user().username());
        assertEquals("pass123456", result.user().password());
        assertTrue(result.user().isActive());
    }

    @Test
    void create_withoutSpecialization_throwsAndSavesNothing() {
        Trainer invalid = new Trainer(null, User.of("Anna", "Brown"), null);

        assertThrows(IllegalArgumentException.class, () -> service.create(invalid));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void create_withBlankLastName_throws() {
        Trainer invalid = new Trainer(null, User.of("Anna", null), new TrainingType("YOGA"));

        assertThrows(IllegalArgumentException.class, () -> service.create(invalid));
    }

    @Test
    void getByUsername_returnsAuthenticatedTrainer() {
        givenAuthenticatedTrainer(true);

        Trainer result = service.getByUsername(CREDENTIALS);

        assertEquals(5L, result.id());
    }

    @Test
    void getByUsername_withWrongPassword_throwsAndDoesNotTouchDao() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class, () -> service.getByUsername(CREDENTIALS));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void changePassword_savesNewPassword() {
        givenAuthenticatedTrainer(true);

        service.changePassword(new PasswordChange(CREDENTIALS, "newPass"));

        assertEquals("newPass", savedTrainer().user().password());
    }

    @Test
    void changePassword_withWrongOldPassword_throwsAndSavesNothing() {
        when(authenticationService.authenticateTrainer(CREDENTIALS)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class,
                () -> service.changePassword(new PasswordChange(CREDENTIALS, "newPass")));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void update_changesNamesAndSpecialization_butKeepsCredentials() {
        givenAuthenticatedTrainer(true);
        Trainer changes = new Trainer(null, User.of("Annie", "Braun"), new TrainingType("FITNESS"));

        service.update(CREDENTIALS, changes);

        Trainer saved = savedTrainer();
        assertEquals(5L, saved.id());
        assertEquals("Annie", saved.user().firstName());
        assertEquals("Braun", saved.user().lastName());
        assertEquals("Anna.Brown", saved.user().username());
        assertEquals("secret", saved.user().password());
        assertEquals("FITNESS", saved.specialization().name());
    }

    @Test
    void update_withoutSpecialization_throwsAndSavesNothing() {
        givenAuthenticatedTrainer(true);
        Trainer changes = new Trainer(null, User.of("Annie", "Braun"), null);

        assertThrows(IllegalArgumentException.class, () -> service.update(CREDENTIALS, changes));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void toggleActive_deactivatesActiveTrainer() {
        givenAuthenticatedTrainer(true);

        boolean result = service.toggleActive(CREDENTIALS);

        assertFalse(result);
        assertFalse(savedTrainer().user().isActive());
    }

    @Test
    void toggleActive_activatesInactiveTrainer() {
        givenAuthenticatedTrainer(false);

        boolean result = service.toggleActive(CREDENTIALS);

        assertTrue(result);
        assertTrue(savedTrainer().user().isActive());
    }
}
