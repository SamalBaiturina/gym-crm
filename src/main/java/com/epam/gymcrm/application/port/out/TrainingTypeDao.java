package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.TrainingType;

import java.util.List;
import java.util.Optional;

/** Read-only access to the constant list of training types. */
public interface TrainingTypeDao {

    List<TrainingType> findAll();

    Optional<TrainingType> findById(Long id);

    /** The number of training types, counted by the database without loading the rows. */
    long count();
}
