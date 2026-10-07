package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Trainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @InjectMocks
    private TrainerService service;

    @Test
    void create_setsGeneratedCredentialsAndActivates() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Anna");
        trainer.setLastName("Brown");
        when(usernameGenerator.generate("Anna", "Brown")).thenReturn("Anna.Brown");
        when(passwordGenerator.generate()).thenReturn("abcdefghij");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = service.create(trainer);

        assertEquals("Anna.Brown", result.getUsername());
        assertEquals("abcdefghij", result.getPassword());
        assertTrue(result.isActive());
    }

    @Test
    void update_savesTrainer_whenExists() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerDao.save(trainer)).thenReturn(trainer);

        assertSame(trainer, service.update(trainer));
    }

    @Test
    void update_throws_whenTrainerNotFound() {
        Trainer trainer = new Trainer();
        trainer.setId(99L);
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.update(trainer));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void update_throws_whenIdIsNull() {
        assertThrows(NoSuchElementException.class, () -> service.update(new Trainer()));
    }

    @Test
    void findById_andFindAll_delegateToDao() {
        Trainer trainer = new Trainer();
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerDao.findAll()).thenReturn(List.of(trainer));

        assertEquals(Optional.of(trainer), service.findById(1L));
        assertEquals(List.of(trainer), service.findAll());
    }
}