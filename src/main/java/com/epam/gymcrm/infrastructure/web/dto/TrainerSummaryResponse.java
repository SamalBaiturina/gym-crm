package com.epam.gymcrm.infrastructure.web.dto;

import com.epam.gymcrm.domain.model.Trainer;

/** A trainer inside a trainee's profile. */
public record TrainerSummaryResponse(String username, String firstName, String lastName,
                                     TrainingTypeResponse specialization) {

    public static TrainerSummaryResponse from(Trainer trainer) {
        return new TrainerSummaryResponse(trainer.user().username(), trainer.user().firstName(),
                trainer.user().lastName(), TrainingTypeResponse.from(trainer.specialization()));
    }
}
