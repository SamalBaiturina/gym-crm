package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;

    private TraineeService service;

    @BeforeEach
    void setUp() {
        service = new TraineeService();
        service.setTraineeDao(traineeDao);
        service.setUsernameGenerator(usernameGenerator);
        service.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void createSetsGeneratedCredentials() {
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("pass123456");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> ((Trainee) inv.getArgument(0)).withId(1L));

        Trainee result = service.create(new Trainee(null, User.of("John", "Smith"),
                LocalDate.of(2000, 1, 1), "Astana"));

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDao).save(captor.capture());
        assertEquals("John.Smith", captor.getValue().user().username());
        assertEquals("pass123456", captor.getValue().user().password());
        assertTrue(captor.getValue().user().isActive());
        assertEquals(1L, result.id());
    }

    @Test
    void updateSavesExistingTrainee() {
        Trainee trainee = new Trainee(1L, User.of("John", "Smith"), null, "Astana");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeDao.save(trainee)).thenReturn(trainee);

        assertEquals(trainee, service.update(trainee));
    }

    @Test
    void updateThrowsWhenNotFound() {
        Trainee trainee = new Trainee(1L, User.of("John", "Smith"), null, "Astana");
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.update(trainee));
        verify(traineeDao, never()).save(any());
    }

    @Test
    void updateThrowsWhenIdIsNull() {
        Trainee trainee = new Trainee(null, User.of("John", "Smith"), null, "Astana");

        assertThrows(NoSuchElementException.class, () -> service.update(trainee));
    }

    @Test
    void deleteDelegatesToDao() {
        service.delete(1L);

        verify(traineeDao).deleteById(1L);
    }

    @Test
    void findByIdAndFindAllDelegateToDao() {
        Trainee trainee = new Trainee(1L, User.of("John", "Smith"), null, "Astana");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));

        assertEquals(Optional.of(trainee), service.findById(1L));
        assertEquals(List.of(trainee), service.findAll());
    }
}
