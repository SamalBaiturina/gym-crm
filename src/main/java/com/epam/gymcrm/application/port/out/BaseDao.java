package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.Identifiable;

import java.util.List;
import java.util.Optional;

public interface BaseDao<T extends Identifiable<T>> {

    T save(T model);

    Optional<T> findById(Long id);

    List<T> findAll();
}
