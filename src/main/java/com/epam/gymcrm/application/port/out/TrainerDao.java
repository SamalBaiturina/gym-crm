package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.Trainer;

import java.util.Collection;
import java.util.List;

public interface TrainerDao extends ProfileDao<Trainer> {

    List<Trainer> findByUsernames(Collection<String> usernames);

    List<Trainer> findNotAssignedTo(String traineeUsername);

    /** Trainers assigned to the trainee with the given username. */
    List<Trainer> findAssignedTo(String traineeUsername);
}
