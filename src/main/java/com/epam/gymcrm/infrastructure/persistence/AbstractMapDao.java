package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.BaseDao;
import com.epam.gymcrm.domain.model.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractMapDao<T extends Identifiable<T>> implements BaseDao<T> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String modelName;
    private Map<Long, T> storage;

    protected AbstractMapDao(String modelName) {
        this.modelName = modelName;
    }

    protected void setStorage(Map<Long, T> storage) {
        this.storage = storage;
    }

    @Override
    public synchronized T save(T model) {
        T toSave = model.id() == null ? model.withId(nextId()) : model;
        storage.put(toSave.id(), toSave);
        log.debug("Saved {} with id={}", modelName, toSave.id());
        return toSave;
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }

    protected List<String> usernamesStartingWith(Function<T, String> usernameGetter, String prefix) {
        return storage.values().stream()
                .map(usernameGetter)
                .filter(name -> name != null && name.startsWith(prefix))
                .toList();
    }

    private Long nextId() {
        return storage.keySet().stream().max(Long::compare).orElse(0L) + 1;
    }
}
