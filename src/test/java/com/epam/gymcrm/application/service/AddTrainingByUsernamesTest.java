package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.exception.ForbiddenOperationException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.exception.ValidationException;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddTrainingByUsernamesTest {

    private static final LocalDate DATE = LocalDate.of(2026, 1, 10);

    @Mock
    private TrainingDao trainingDao;
    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private TrainingService service;

    private Trainee trainee() {
        return new Trainee(1L, new User("John", "Smith", "John.Smith", "secret", true), null, null);
    }

    private Trainer trainer() {
        return new Trainer(5L, new User("Anna", "Brown", "Anna.Brown", "secret", true),
                new TrainingType(2L, "YOGA"));
    }

    private AddTrainingCommand command(String name, int duration) {
        return new AddTrainingCommand("John.Smith", "John.Smith", "Anna.Brown", name, DATE, duration);
    }

    private AddTrainingCommand commandBy(String caller) {
        return new AddTrainingCommand(caller, "John.Smith", "Anna.Brown", "Morning yoga", DATE, 60);
    }

    @Test
    void savesTheTrainingWithTheTrainersSpecializationAsType() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer()));
        when(trainingDao.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            return training.withId(10L);
        });

        Training saved = service.addTraining(command("Morning yoga", 60));

        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingDao).save(captor.capture());
        Training sent = captor.getValue();
        assertEquals(10L, saved.id());
        assertEquals("John.Smith", sent.trainee().user().username());
        assertEquals("Anna.Brown", sent.trainer().user().username());
        assertEquals("YOGA", sent.trainingType().name());
        assertEquals("Morning yoga", sent.trainingName());
        assertEquals(DATE, sent.trainingDate());
        assertEquals(60, sent.trainingDuration());
    }

    @Test
    void trainerCanAddTheTraining() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.of(trainer()));
        when(trainingDao.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            return training.withId(11L);
        });

        Training saved = service.addTraining(commandBy("Anna.Brown"));

        assertEquals(11L, saved.id());
    }

    @Test
    void anotherUserIsForbiddenAndNothingIsLoaded() {
        ForbiddenOperationException e = assertThrows(ForbiddenOperationException.class,
                () -> service.addTraining(commandBy("Mike.Jones")));

        assertEquals("A training can be added only by its trainee or its trainer", e.getMessage());
        verifyNoInteractions(traineeDao, trainerDao, trainingDao);
    }

    @Test
    void unknownTraineeIsNotFound() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.addTraining(command("Morning yoga", 60)));

        assertEquals("Trainee not found: John.Smith", e.getMessage());
        verify(trainingDao, never()).save(any());
    }

    @Test
    void unknownTrainerIsNotFound() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee()));
        when(trainerDao.findByUsername("Anna.Brown")).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> service.addTraining(command("Morning yoga", 60)));

        assertEquals("Trainer not found: Anna.Brown", e.getMessage());
        verify(trainingDao, never()).save(any());
    }

    @Test
    void invalidInputIsRejectedBeforeAnyLookup() {
        assertThrows(ValidationException.class, () -> service.addTraining((AddTrainingCommand) null));
        assertThrows(ValidationException.class, () -> service.addTraining(command(" ", 60)));
        assertThrows(ValidationException.class, () -> service.addTraining(command("Morning yoga", 0)));
        assertThrows(ValidationException.class,
                () -> service.addTraining(new AddTrainingCommand("John.Smith", "John.Smith", "Anna.Brown", "Yoga", null, 60)));
        assertThrows(ValidationException.class,
                () -> service.addTraining(new AddTrainingCommand("John.Smith", " ", "Anna.Brown", "Yoga", DATE, 60)));
        assertThrows(ValidationException.class,
                () -> service.addTraining(new AddTrainingCommand("John.Smith", "John.Smith", null, "Yoga", DATE, 60)));
        assertThrows(ValidationException.class,
                () -> service.addTraining(new AddTrainingCommand(" ", "John.Smith", "Anna.Brown", "Yoga", DATE, 60)));

        verify(trainingDao, never()).save(any());
    }
}
