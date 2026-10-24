package com.epam.gymcrm.application.service;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;

import java.util.List;

/** A trainer together with the trainees assigned to him. */
public record TrainerProfile(Trainer trainer, List<Trainee> trainees) {
}
