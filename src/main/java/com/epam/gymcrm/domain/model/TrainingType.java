package com.epam.gymcrm.domain.model;

import java.util.Objects;

/**
 * A kind of training. The list is fixed in the database. A type is identified by its unique name,
 * so a type created from a name alone equals the stored one that also has an id.
 */
public record TrainingType(Long id, String name) {

    public TrainingType(String name) {
        this(null, name);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TrainingType type && Objects.equals(name, type.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
