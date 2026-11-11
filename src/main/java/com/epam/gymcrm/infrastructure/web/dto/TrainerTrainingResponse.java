package com.epam.gymcrm.infrastructure.web.dto;

import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.User;

import java.time.LocalDate;

/** A training in the trainer's list; traineeName is "FirstName LastName". */
public record TrainerTrainingResponse(String trainingName, LocalDate trainingDate, String trainingType,
                                      int trainingDuration, String traineeName) {

    public static TrainerTrainingResponse from(Training training) {
        User trainee = training.trainee().user();
        return new TrainerTrainingResponse(training.trainingName(), training.trainingDate(),
                training.trainingType().name(), training.trainingDuration(),
                trainee.firstName() + " " + trainee.lastName());
    }
}
