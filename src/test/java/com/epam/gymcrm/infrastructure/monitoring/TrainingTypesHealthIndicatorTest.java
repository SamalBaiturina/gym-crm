package com.epam.gymcrm.infrastructure.monitoring;

import com.epam.gymcrm.application.service.TrainingTypeService;
import com.epam.gymcrm.domain.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypesHealthIndicatorTest {

    @Mock
    private TrainingTypeService trainingTypeService;
    @InjectMocks
    private TrainingTypesHealthIndicator indicator;

    @Test
    void isUpAndCountsTheTypesWhenTheyExist() {
        when(trainingTypeService.getAll())
                .thenReturn(List.of(new TrainingType(1L, "FITNESS"), new TrainingType(2L, "YOGA")));

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(2, health.getDetails().get("count"));
    }

    @Test
    void isDownWhenTheTableIsEmpty() {
        when(trainingTypeService.getAll()).thenReturn(List.of());

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("reason"));
    }

    @Test
    void isDownWhenTheDatabaseCannotBeRead() {
        when(trainingTypeService.getAll()).thenThrow(new IllegalStateException("no connection"));

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }
}
