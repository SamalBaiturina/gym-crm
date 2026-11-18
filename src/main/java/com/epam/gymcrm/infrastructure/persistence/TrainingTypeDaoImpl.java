package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.TrainingTypeDao;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingTypeEntity;
import com.epam.gymcrm.infrastructure.persistence.mapper.TrainingTypeMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Training types are only read: the table is filled by the schema script.
 * It does not extend AbstractJpaDao on purpose: that class is the username lookup, and one is enough.
 */
@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    private final TrainingTypeMapper mapper;
    private EntityManager entityManager;

    public TrainingTypeDaoImpl(TrainingTypeMapper mapper) {
        this.mapper = mapper;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<TrainingType> findAll() {
        return entityManager.createQuery("select t from TrainingTypeEntity t order by t.id", TrainingTypeEntity.class)
                .getResultList().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return entityManager.createQuery("select count(t) from TrainingTypeEntity t", Long.class).getSingleResult();
    }

    @Override
    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainingTypeEntity.class, id))
                .map(mapper::toDomain);
    }
}
