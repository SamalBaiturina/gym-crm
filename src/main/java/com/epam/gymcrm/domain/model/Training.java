package com.epam.gymcrm.domain.model;

import java.time.LocalDate;

public record Training(Long id, Trainee trainee, Trainer trainer, String trainingName,
                       TrainingType trainingType, LocalDate trainingDate, int trainingDuration)
        implements Identifiable<Training> {

    @Override
    public Training withId(Long id) {
        return new Training(id, trainee, trainer, trainingName, trainingType,
                trainingDate, trainingDuration);
    }
}
