package com.epam.gymcrm.infrastructure.monitoring;

import com.epam.gymcrm.application.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

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
        when(trainingTypeService.count()).thenReturn(2L);

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(2L, health.getDetails().get("count"));
    }

    @Test
    void isDownWhenTheTableIsEmpty() {
        when(trainingTypeService.count()).thenReturn(0L);

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("reason"));
    }

    @Test
    void isDownWhenTheDatabaseCannotBeRead() {
        when(trainingTypeService.count()).thenThrow(new IllegalStateException("no connection"));

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }
}
