package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;

import java.util.List;

public interface TraineeDao extends ProfileDao<Trainee> {

    /** Removes the trainee together with the user and the trainings (cascade). */
    void delete(Trainee trainee);

    /** Replaces the trainee's trainers with the given (already persisted) ones. */
    void updateTrainers(Trainee trainee, List<Trainer> trainers);
}
