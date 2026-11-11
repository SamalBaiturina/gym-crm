package com.epam.gymcrm.infrastructure.web.dto;

import com.epam.gymcrm.domain.model.Trainee;

/** A trainee inside a trainer's profile. */
public record TraineeSummaryResponse(String username, String firstName, String lastName) {

    public static TraineeSummaryResponse from(Trainee trainee) {
        return new TraineeSummaryResponse(trainee.user().username(), trainee.user().firstName(),
                trainee.user().lastName());
    }
}
