package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.mapper.TrainerMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class TrainerDaoImpl extends AbstractProfileDao<TrainerEntity, Trainer> implements TrainerDao {

    private static final String SELECT_TRAINER =
            "select t from TrainerEntity t join fetch t.user u join fetch t.specialization ";

    private final TrainerMapper trainerMapper;

    public TrainerDaoImpl(TrainerMapper trainerMapper) {
        super(TrainerEntity.class, "trainer", SELECT_TRAINER);
        this.trainerMapper = trainerMapper;
    }

    @Override
    protected Trainer toDomain(TrainerEntity entity) {
        return trainerMapper.toDomain(entity);
    }

    @Override
    protected TrainerEntity newEntity(Trainer trainer) {
        return trainerMapper.toEntity(trainer, findTrainingType(trainer.specialization().name()));
    }

    @Override
    protected void copyInto(TrainerEntity entity, Trainer trainer) {
        trainerMapper.update(entity, trainer, findTrainingType(trainer.specialization().name()));
    }

    @Override
    public List<Trainer> findByUsernames(Collection<String> usernames) {
        if (usernames.isEmpty()) {
            return List.of();
        }
        return entityManager.createQuery(SELECT_TRAINER + "where u.username in :usernames", TrainerEntity.class)
                .setParameter("usernames", usernames)
                .getResultList().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Trainer> findNotAssignedTo(String traineeUsername) {
        return entityManager.createQuery(SELECT_TRAINER
                                + "where t.id not in (select a.id from TraineeEntity te "
                                + "join te.trainers a join te.user tu where tu.username = :username)",
                        TrainerEntity.class)
                .setParameter("username", traineeUsername)
                .getResultList().stream()
                .map(this::toDomain)
                .toList();
    }
}
