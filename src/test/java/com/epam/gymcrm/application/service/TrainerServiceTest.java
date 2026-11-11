package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.exception.ForbiddenOperationException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.exception.ValidationException;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
    private static final AuthenticatedUser USER = new AuthenticatedUser(CREDENTIALS, Role.TRAINER, true);
    private static final AuthenticatedUser TRAINEE_USER = new AuthenticatedUser(CREDENTIALS, Role.TRAINEE, true);

    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private TrainingTypeService trainingTypeService;
    @InjectMocks
    private TrainerService service;

    private void givenStoredTrainer(boolean active) {
        User user = new User("Anna", "Brown", "Anna.Brown", "secret", active);
        when(trainerDao.findByUsername("Anna.Brown"))
                .thenReturn(Optional.of(new Trainer(5L, user, new TrainingType("YOGA"))));
    }

    private Trainer savedTrainer() {
        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDao).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void register_resolvesTheSpecializationAndCreatesTheTrainer() {
        when(trainingTypeService.requireById(2L)).thenReturn(new TrainingType(2L, "YOGA"));
        when(usernameGenerator.generate("Anna", "Brown")).thenReturn("Anna.Brown");
        when(passwordGenerator.generate()).thenReturn("pass123456");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer toSave = invocation.getArgument(0);
            return toSave.withId(5L);
        });

        Trainer result = service.register(new RegisterTrainer("Anna", "Brown", 2L));

        assertEquals(5L, result.id());
        assertEquals("Anna.Brown", result.user().username());
        assertEquals("YOGA", result.specialization().name());
    }

    @Test
    void register_withUnknownSpecialization_savesNothing() {
        when(trainingTypeService.requireById(99L))
                .thenThrow(new ValidationException("Unknown training type id: 99"));

        assertThrows(ValidationException.class, () -> service.register(new RegisterTrainer("Anna", "Brown", 99L)));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void register_withNullCommand_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null));
        verifyNoInteractions(trainerDao);
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
        givenStoredTrainer(true);

        Trainer result = service.getByUsername(USER);

        assertEquals(5L, result.id());
    }

    @Test
    void getByUsername_forTraineeUser_isForbiddenAndDoesNotTouchDao() {
        assertThrows(ForbiddenOperationException.class, () -> service.getByUsername(TRAINEE_USER));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void getByUsername_whenTheAccountIsGone_throwsNotFound() {
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getByUsername(USER));
    }

    @Test
    void changePassword_savesNewPassword() {
        givenStoredTrainer(true);

        service.changePassword(USER, "newPass");

        assertEquals("newPass", savedTrainer().user().password());
    }

    @Test
    void changePassword_withBlankPassword_throwsAndSavesNothing() {
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(USER, " "));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void update_changesNamesAndSpecialization_butKeepsCredentials() {
        givenStoredTrainer(true);
        Trainer changes = new Trainer(null, User.of("Annie", "Braun"), new TrainingType("FITNESS"));

        service.update(USER, changes);

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
        givenStoredTrainer(true);
        Trainer changes = new Trainer(null, User.of("Annie", "Braun"), null);

        assertThrows(IllegalArgumentException.class, () -> service.update(USER, changes));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void toggleActive_deactivatesActiveTrainer() {
        givenStoredTrainer(true);

        boolean result = service.toggleActive(USER);

        assertFalse(result);
        assertFalse(savedTrainer().user().isActive());
    }

    @Test
    void toggleActive_activatesInactiveTrainer() {
        givenStoredTrainer(false);

        boolean result = service.toggleActive(USER);

        assertTrue(result);
        assertTrue(savedTrainer().user().isActive());
    }
}
