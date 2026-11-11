package com.epam.gymcrm.infrastructure.web.dto;

import com.epam.gymcrm.domain.model.TrainingType;

/** A training type as the API shows it: the id plus the name. */
public record TrainingTypeResponse(Long id, String name) {

    public static TrainingTypeResponse from(TrainingType type) {
        return new TrainingTypeResponse(type.id(), type.name());
    }
}
