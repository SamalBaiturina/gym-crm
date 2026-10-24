package com.epam.gymcrm.application.service;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;

import java.util.List;

/** A trainee together with the trainers assigned to him. */
public record TraineeProfile(Trainee trainee, List<Trainer> trainers) {
}
