package com.epam.gymcrm.infrastructure.persistence.mapper;

import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingTypeEntity;
import org.springframework.stereotype.Component;

/** Converts the {@link TrainingTypeEntity} to the domain {@code TrainingType}; training types are read-only. */
@Component
public class TrainingTypeMapper {

    public TrainingType toDomain(TrainingTypeEntity entity) {
        return new TrainingType(entity.getId(), entity.getName());
    }
}
