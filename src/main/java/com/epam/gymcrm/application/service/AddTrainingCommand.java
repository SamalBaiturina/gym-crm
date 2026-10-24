package com.epam.gymcrm.application.service;

import java.time.LocalDate;

/** Request to add a training between a trainee and a trainer, both given by username. */
public record AddTrainingCommand(String traineeUsername, String trainerUsername, String name,
                                 LocalDate date, int duration) {
}
