package com.epam.gymcrm.infrastructure.monitoring;

import com.epam.gymcrm.application.service.TrainingTypeService;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Health check of the training types. The application cannot register a trainer or add a training without them.
 * The table is filled by db/schema.sql, so an empty table means that the schema script was not run.
 * Shown in /actuator/health as "trainingTypes".
 */
@Component
public class TrainingTypesHealthIndicator extends AbstractHealthIndicator {

    private final TrainingTypeService trainingTypeService;

    public TrainingTypesHealthIndicator(TrainingTypeService trainingTypeService) {
        super("Training types health check failed");
        this.trainingTypeService = trainingTypeService;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        int count = trainingTypeService.getAll().size();
        if (count == 0) {
            builder.down().withDetail("reason", "no training types, run db/schema.sql");
        } else {
            builder.up().withDetail("count", count);
        }
    }
}
