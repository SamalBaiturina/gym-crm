package com.epam.gymcrm.infrastructure.monitoring;

import com.epam.gymcrm.application.port.out.UsageMetrics;
import com.epam.gymcrm.domain.model.Role;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Counts the business events with Micrometer. Spring Boot exposes the counters in /actuator/prometheus
 * as gymcrm_registrations_total{role}, gymcrm_trainings_added_total and gymcrm_auth_failures_total.
 * All counters are created at startup, so they show 0 before the first event.
 */
@Component
public class MicrometerUsageMetrics implements UsageMetrics {

    private final Map<Role, Counter> registrations = new EnumMap<>(Role.class);
    private final Counter trainingsAdded;
    private final Counter authenticationFailures;

    public MicrometerUsageMetrics(MeterRegistry registry) {
        for (Role role : Role.values()) {
            registrations.put(role, Counter.builder("gymcrm.registrations")
                    .description("Created accounts, by role")
                    .tag("role", role.name().toLowerCase(Locale.ROOT))
                    .register(registry));
        }
        trainingsAdded = Counter.builder("gymcrm.trainings.added")
                .description("Trainings saved")
                .register(registry);
        authenticationFailures = Counter.builder("gymcrm.auth.failures")
                .description("Requests with an unknown username or a wrong password")
                .register(registry);
    }

    @Override
    public void registrationCompleted(Role role) {
        registrations.get(role).increment();
    }

    @Override
    public void trainingAdded() {
        trainingsAdded.increment();
    }

    @Override
    public void authenticationFailed() {
        authenticationFailures.increment();
    }
}
