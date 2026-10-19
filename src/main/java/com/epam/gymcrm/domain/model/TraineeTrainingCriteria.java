package com.epam.gymcrm.domain.model;

import java.time.LocalDate;

/**
 * Optional filters for the trainee's trainings list; a null (or blank) value means "do not filter by it".
 */
public record TraineeTrainingCriteria(LocalDate from, LocalDate to, String trainerName, String trainingType) {

    public TraineeTrainingCriteria {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
    }

    /** No filters at all. */
    public static TraineeTrainingCriteria any() {
        return new TraineeTrainingCriteria(null, null, null, null);
    }
}
