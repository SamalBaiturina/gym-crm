package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.BaseDao;
import com.epam.gymcrm.domain.model.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMapDao<T extends BaseEntity> implements BaseDao<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final String entityName;
    protected Map<Long, T> storage;

    protected AbstractMapDao(String entityName) {
        this.entityName = entityName;
    }

    public void setStorage(Map<Long, T> storage) {
        this.storage = storage;
    }

    @Override
    public synchronized T save(T entity) {
        if (entity.getId() == null) {
            entity.setId(nextId());
        }
        storage.put(entity.getId(), entity);
        log.debug("Saved {} with id={}", entityName, entity.getId());
        return entity;
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    private Long nextId() {
        return storage.keySet().stream().max(Long::compare).orElse(0L) + 1;
    }
}