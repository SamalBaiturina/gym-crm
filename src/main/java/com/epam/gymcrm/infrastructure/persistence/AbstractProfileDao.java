package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.ProfileDao;
import com.epam.gymcrm.domain.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * The storage code that trainee and trainer DAOs share: save (insert or update), find by id,
 * find by username and find all. Subclasses supply the entity mapping.
 *
 * @param <E> the JPA entity
 * @param <T> the domain profile
 */
public abstract class AbstractProfileDao<E, T extends UserProfile<T>> extends AbstractJpaDao
        implements ProfileDao<T> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Class<E> entityClass;
    private final String role;
    private final String selectQuery;

    /**
     * @param selectQuery a JPQL select that fetches the user under the alias "u" and ends with a space
     */
    protected AbstractProfileDao(Class<E> entityClass, String role, String selectQuery) {
        this.entityClass = entityClass;
        this.role = role;
        this.selectQuery = selectQuery;
    }

    protected abstract T toDomain(E entity);

    protected abstract E newEntity(T profile);

    /** Copies the fields of the profile into an already existing entity. */
    protected abstract void copyInto(E entity, T profile);

    @Override
    public T save(T profile) {
        if (profile.id() == null) {
            E entity = newEntity(profile);
            entityManager.persist(entity);
            T saved = toDomain(entity);
            log.debug("Persisted {} id={}", role, saved.id());
            return saved;
        }
        E entity = getEntity(profile.id());
        copyInto(entity, profile);
        log.debug("Updated {} id={}", role, profile.id());
        return toDomain(entity);
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(entityManager.find(entityClass, id)).map(this::toDomain);
    }

    @Override
    public Optional<T> findByUsername(String username) {
        return entityManager.createQuery(selectQuery + "where u.username = :username", entityClass)
                .setParameter("username", username)
                .getResultList().stream().findFirst()
                .map(this::toDomain);
    }

    @Override
    public List<T> findAll() {
        return entityManager.createQuery(selectQuery, entityClass)
                .getResultList().stream()
                .map(this::toDomain)
                .toList();
    }

    protected E getEntity(Long id) {
        E entity = entityManager.find(entityClass, id);
        if (entity == null) {
            throw new NoSuchElementException(role + " not found: " + id);
        }
        return entity;
    }
}
