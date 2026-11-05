package com.epam.gymcrm.application.service;

import java.time.LocalDate;

/**
 * Request to add a training between a trainee and a trainer, both given by username.
 * The caller is the user who makes the request; only the trainee or the trainer of the training may add it.
 */
public record AddTrainingCommand(String caller, String traineeUsername, String trainerUsername, String name,
                                 LocalDate date, int duration) {
}
