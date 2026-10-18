package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.model.TraineeTrainingCriteria;
import com.epam.gymcrm.domain.model.TrainerTrainingCriteria;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingTypeEntity;
import com.epam.gymcrm.infrastructure.persistence.mapper.TrainingMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDaoImpl extends AbstractJpaDao implements TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);

    /**
     * Loads the training together with both sides, so mapping to the domain needs no extra queries.
     */
    private static final String SELECT_TRAINING = "select t from TrainingEntity t "
            + "join fetch t.trainingType "
            + "join fetch t.trainee te join fetch te.user "
            + "join fetch t.trainer tr join fetch tr.user join fetch tr.specialization ";

    private final TrainingMapper trainingMapper;

    public TrainingDaoImpl(TrainingMapper trainingMapper) {
        this.trainingMapper = trainingMapper;
    }

    /**
     * The trainee and the trainer of the training must already be persisted.
     */
    @Override
    public Training save(Training training) {
        if (training.id() != null) {
            log.warn("Cannot save training id={}: updating a training is not supported", training.id());
            throw new IllegalArgumentException("Updating a training is not supported");
        }
        // references carry only the id: enough for the foreign keys, and no SELECT is needed
        TraineeEntity trainee = entityManager.getReference(TraineeEntity.class, training.trainee().id());
        TrainerEntity trainer = entityManager.getReference(TrainerEntity.class, training.trainer().id());
        TrainingTypeEntity type = findTrainingType(training.trainingType().name());

        TrainingEntity entity = trainingMapper.toEntity(training, trainee, trainer, type);
        entityManager.persist(entity);
        log.debug("Persisted training id={}", entity.getId());
        return training.withId(entity.getId());
    }

    @Override
    public Optional<Training> findById(Long id) {
        return entityManager.createQuery(SELECT_TRAINING + "where t.id = :id", TrainingEntity.class)
                .setParameter("id", id)
                .getResultList().stream().findFirst()
                .map(trainingMapper::toDomain);
    }

    @Override
    public List<Training> findAll() {
        return entityManager.createQuery(SELECT_TRAINING, TrainingEntity.class)
                .getResultList().stream()
                .map(trainingMapper::toDomain)
                .toList();
    }

    @Override
    public List<Training> findForTrainee(String traineeUsername, TraineeTrainingCriteria criteria) {
        return search("trainee", traineeUsername, "trainer",
                new Filters(criteria.from(), criteria.to(), criteria.trainerName(), criteria.trainingType()));
    }

    @Override
    public List<Training> findForTrainer(String trainerUsername, TrainerTrainingCriteria criteria) {
        return search("trainer", trainerUsername, "trainee",
                new Filters(criteria.from(), criteria.to(), criteria.traineeName(), null));
    }

    /**
     * Filters shared by both searches; otherName is the first name of the other side of the training.
     */
    private record Filters(LocalDate from, LocalDate to, String otherName, String trainingType) {
    }

    /**
     * Builds the query dynamically: only the filters that were actually passed are added.
     * ownerAttribute is the side whose username we search by, otherAttribute is the side filtered by name.
     */
    private List<Training> search(String ownerAttribute, String ownerUsername,
                                  String otherAttribute, Filters filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> query = cb.createQuery(TrainingEntity.class);
        Root<TrainingEntity> training = query.from(TrainingEntity.class);
        fetchAll(training);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(training.get(ownerAttribute).get("user").get("username"), ownerUsername));
        if (filters.from() != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.<LocalDate>get("date"), filters.from()));
        }
        if (filters.to() != null) {
            predicates.add(cb.lessThanOrEqualTo(training.<LocalDate>get("date"), filters.to()));
        }
        if (hasText(filters.otherName())) {
            predicates.add(cb.equal(
                    cb.lower(training.get(otherAttribute).get("user").<String>get("firstName")),
                    filters.otherName().toLowerCase()));
        }
        if (hasText(filters.trainingType())) {
            predicates.add(cb.equal(training.get("trainingType").get("name"), filters.trainingType()));
        }
        query.select(training)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.asc(training.get("date")));

        return entityManager.createQuery(query).getResultList().stream()
                .map(trainingMapper::toDomain)
                .toList();
    }

    /**
     * Same fetches as SELECT_TRAINING, for the Criteria query.
     */
    private static void fetchAll(Root<TrainingEntity> training) {
        training.fetch("trainingType");
        Fetch<TrainingEntity, TraineeEntity> trainee = training.fetch("trainee");
        trainee.fetch("user");
        Fetch<TrainingEntity, TrainerEntity> trainer = training.fetch("trainer");
        trainer.fetch("user");
        trainer.fetch("specialization");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
