package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TrainingTypeDao;
import com.epam.gymcrm.domain.exception.ValidationException;
import com.epam.gymcrm.domain.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;
    @InjectMocks
    private TrainingTypeService service;

    @Test
    void getAllReturnsAllTypes() {
        List<TrainingType> types = List.of(new TrainingType(1L, "FITNESS"), new TrainingType(2L, "YOGA"));
        when(trainingTypeDao.findAll()).thenReturn(types);

        assertEquals(types, service.getAll());
    }

    @Test
    void requireByIdReturnsTheType() {
        TrainingType yoga = new TrainingType(2L, "YOGA");
        when(trainingTypeDao.findById(2L)).thenReturn(Optional.of(yoga));

        assertEquals(yoga, service.requireById(2L));
    }

    @Test
    void requireByIdRejectsUnknownId() {
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());

        ValidationException e = assertThrows(ValidationException.class, () -> service.requireById(99L));
        assertEquals("Unknown training type id: 99", e.getMessage());
    }

    @Test
    void requireByIdRejectsNull() {
        assertThrows(ValidationException.class, () -> service.requireById(null));

        verifyNoInteractions(trainingTypeDao);
    }
}
