package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.mapper.TraineeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class TraineeDaoImpl extends AbstractJpaDao implements TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private static final String SELECT_TRAINEE = "select t from TraineeEntity t join fetch t.user u ";

    private final TraineeMapper traineeMapper;

    public TraineeDaoImpl(TraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
    }

    @Override
    public Trainee save(Trainee trainee) {
        if (trainee.id() == null) {
            TraineeEntity entity = traineeMapper.toEntity(trainee);
            entityManager.persist(entity);
            log.debug("Persisted trainee id={}", entity.getId());
            return traineeMapper.toDomain(entity);
        }
        TraineeEntity entity = getEntity(trainee.id());
        traineeMapper.update(entity, trainee);
        log.debug("Updated trainee id={}", entity.getId());
        return traineeMapper.toDomain(entity);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TraineeEntity.class, id))
                .map(traineeMapper::toDomain);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        return entityManager.createQuery(SELECT_TRAINEE + "where u.username = :username", TraineeEntity.class)
                .setParameter("username", username)
                .getResultList().stream().findFirst()
                .map(traineeMapper::toDomain);
    }

    @Override
    public List<Trainee> findAll() {
        return entityManager.createQuery(SELECT_TRAINEE, TraineeEntity.class)
                .getResultList().stream()
                .map(traineeMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(Trainee trainee) {
        // find() takes the entity from the persistence context if the service has already loaded it
        TraineeEntity entity = entityManager.find(TraineeEntity.class, trainee.id());
        if (entity != null) {
            entityManager.remove(entity);
            log.debug("Removed trainee id={}", trainee.id());
        }
    }

    @Override
    public void updateTrainers(Trainee trainee, List<Trainer> trainers) {
        TraineeEntity entity = getEntity(trainee.id());
        // references carry only the id, so no SELECT is needed to fill the join table
        Set<TrainerEntity> references = trainers.stream()
                .map(trainer -> entityManager.getReference(TrainerEntity.class, trainer.id()))
                .collect(Collectors.toSet());
        entity.getTrainers().clear();
        entity.getTrainers().addAll(references);
        log.debug("Trainee id={} now has {} trainers", trainee.id(), references.size());
    }

    private TraineeEntity getEntity(Long id) {
        TraineeEntity entity = entityManager.find(TraineeEntity.class, id);
        if (entity == null) {
            throw new NoSuchElementException("Trainee not found: " + id);
        }
        return entity;
    }
}
