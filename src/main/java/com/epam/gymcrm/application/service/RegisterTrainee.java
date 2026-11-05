package com.epam.gymcrm.application.service;

import java.time.LocalDate;

/** Input of the use case "register a trainee". Date of birth and address are optional. */
public record RegisterTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
}
