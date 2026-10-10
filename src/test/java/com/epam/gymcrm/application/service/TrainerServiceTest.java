package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TrainerService service;

    @BeforeEach
    void setUp() {
        service = new TrainerService();
        service.setTrainerDao(trainerDao);
        service.setUsernameGenerator(usernameGenerator);
        service.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void createSetsGeneratedCredentials() {
        when(usernameGenerator.generate("Anna", "Brown")).thenReturn("Anna.Brown");
        when(passwordGenerator.generate()).thenReturn("trainer1234");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> ((Trainer) inv.getArgument(0)).withId(1L));

        Trainer result = service.create(new Trainer(null, User.of("Anna", "Brown"), new TrainingType("YOGA")));

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDao).save(captor.capture());
        assertEquals("Anna.Brown", captor.getValue().user().username());
        assertEquals("trainer1234", captor.getValue().user().password());
        assertEquals(1L, result.id());
    }

    @Test
    void updateSavesExistingTrainer() {
        Trainer trainer = new Trainer(1L, User.of("Anna", "Brown"), new TrainingType("YOGA"));
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerDao.save(trainer)).thenReturn(trainer);

        assertEquals(trainer, service.update(trainer));
    }

    @Test
    void updateThrowsWhenNotFound() {
        Trainer trainer = new Trainer(1L, User.of("Anna", "Brown"), new TrainingType("YOGA"));
        when(trainerDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.update(trainer));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void updateThrowsWhenIdIsNull() {
        Trainer trainer = new Trainer(null, User.of("Anna", "Brown"), new TrainingType("YOGA"));

        assertThrows(NoSuchElementException.class, () -> service.update(trainer));
    }

    @Test
    void findByIdAndFindAllDelegateToDao() {
        Trainer trainer = new Trainer(1L, User.of("Anna", "Brown"), new TrainingType("YOGA"));
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerDao.findAll()).thenReturn(List.of(trainer));

        assertEquals(Optional.of(trainer), service.findById(1L));
        assertEquals(List.of(trainer), service.findAll());
    }
}
