package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.BaseEntity;

import java.util.List;
import java.util.Optional;

public interface BaseDao<T extends BaseEntity> {
    T save(T entity);
    Optional<T> findById(Long id);
    List<T> findAll();
}