package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.domain.model.Trainee;
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
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @InjectMocks
    private TraineeService service;

    @Test
    void create_setsGeneratedCredentialsAndActivates() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Smith");
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("abcdefghij");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee result = service.create(trainee);

        assertEquals("John.Smith", result.getUsername());
        assertEquals("abcdefghij", result.getPassword());
        assertTrue(result.isActive());
    }

    @Test
    void update_throws_whenTraineeNotFound() {
        Trainee trainee = new Trainee();
        trainee.setId(99L);
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.update(trainee));
        verify(traineeDao, never()).save(any());
    }

    @Test
    void delete_delegatesToDao() {
        service.delete(1L);

        verify(traineeDao).deleteById(1L);
    }

    @Test
    void update_savesTrainee_whenExists() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeDao.save(trainee)).thenReturn(trainee);

        assertSame(trainee, service.update(trainee));
    }

    @Test
    void update_throws_whenIdIsNull() {
        assertThrows(NoSuchElementException.class, () -> service.update(new Trainee()));
    }

    @Test
    void findById_andFindAll_delegateToDao() {
        Trainee trainee = new Trainee();
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));

        assertEquals(Optional.of(trainee), service.findById(1L));
        assertEquals(List.of(trainee), service.findAll());
    }
}