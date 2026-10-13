package com.epam.gymcrm.domain.model;

import java.time.LocalDate;

public record Training(Long id, Long traineeId, Long trainerId, String trainingName,
                       TrainingType trainingType, LocalDate trainingDate, int trainingDuration)
        implements Identifiable<Training> {

    @Override
    public Training withId(Long id) {
        return new Training(id, traineeId, trainerId, trainingName, trainingType,
                trainingDate, trainingDuration);
    }
}
