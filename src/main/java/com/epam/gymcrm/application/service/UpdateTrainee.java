package com.epam.gymcrm.application.service;

import java.time.LocalDate;

/** Input of the use case "update a trainee profile": new personal data and the new active flag. */
public record UpdateTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address,
                            boolean active) {
}
