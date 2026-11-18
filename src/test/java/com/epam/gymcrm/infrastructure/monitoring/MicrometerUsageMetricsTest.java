package com.epam.gymcrm.infrastructure.monitoring;

import com.epam.gymcrm.domain.model.Role;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MicrometerUsageMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final MicrometerUsageMetrics metrics = new MicrometerUsageMetrics(registry);

    private double count(String name, String... tags) {
        return registry.get(name).tags(tags).counter().count();
    }

    @Test
    void everyCounterExistsAndStartsAtZero() {
        assertEquals(0, count("gymcrm.registrations", "role", "trainee"));
        assertEquals(0, count("gymcrm.registrations", "role", "trainer"));
        assertEquals(0, count("gymcrm.trainings.added"));
        assertEquals(0, count("gymcrm.auth.failures"));
    }

    @Test
    void registrationsAreCountedPerRole() {
        metrics.registrationCompleted(Role.TRAINEE);
        metrics.registrationCompleted(Role.TRAINEE);
        metrics.registrationCompleted(Role.TRAINER);

        assertEquals(2, count("gymcrm.registrations", "role", "trainee"));
        assertEquals(1, count("gymcrm.registrations", "role", "trainer"));
    }

    @Test
    void addedTrainingsAreCounted() {
        metrics.trainingAdded();

        assertEquals(1, count("gymcrm.trainings.added"));
    }

    @Test
    void authenticationFailuresAreCounted() {
        metrics.authenticationFailed();
        metrics.authenticationFailed();

        assertEquals(2, count("gymcrm.auth.failures"));
    }
}
