package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TraineeDao extends BaseDao<Trainee>, UsernameLookup {

    Optional<Trainee> findByUsername(String username);

    /** Removes the trainee together with the user and the trainings (cascade). */
    void delete(Trainee trainee);

    /** Replaces the trainee's trainers with the given (already persisted) ones. */
    void updateTrainers(Trainee trainee, List<Trainer> trainers);
}
