package com.epam.gymcrm.domain.model;

import java.time.LocalDate;

/**
 * Optional filters for the trainer's trainings list; a null (or blank) value means "do not filter by it".
 */
public record TrainerTrainingCriteria(LocalDate from, LocalDate to, String traineeName) {

    public TrainerTrainingCriteria {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
    }

    /** No filters at all. */
    public static TrainerTrainingCriteria any() {
        return new TrainerTrainingCriteria(null, null, null);
    }
}
