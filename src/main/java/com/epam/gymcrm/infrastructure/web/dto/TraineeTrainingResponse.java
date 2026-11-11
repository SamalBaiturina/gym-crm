package com.epam.gymcrm.infrastructure.web.dto;

import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.User;

import java.time.LocalDate;

/** A training in the trainee's list; trainerName is "FirstName LastName". */
public record TraineeTrainingResponse(String trainingName, LocalDate trainingDate, String trainingType,
                                      int trainingDuration, String trainerName) {

    public static TraineeTrainingResponse from(Training training) {
        User trainer = training.trainer().user();
        return new TraineeTrainingResponse(training.trainingName(), training.trainingDate(),
                training.trainingType().name(), training.trainingDuration(),
                trainer.firstName() + " " + trainer.lastName());
    }
}
