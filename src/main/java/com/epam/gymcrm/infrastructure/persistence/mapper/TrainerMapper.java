package com.epam.gymcrm.infrastructure.persistence.mapper;

import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper {

    private final UserMapper userMapper;
    private final TrainingTypeMapper trainingTypeMapper;

    public TrainerMapper(UserMapper userMapper, TrainingTypeMapper trainingTypeMapper) {
        this.userMapper = userMapper;
        this.trainingTypeMapper = trainingTypeMapper;
    }

    public Trainer toDomain(TrainerEntity entity) {
        return new Trainer(entity.getId(), userMapper.toDomain(entity.getUser()),
                trainingTypeMapper.toDomain(entity.getSpecialization()));
    }

    public TrainerEntity toEntity(Trainer trainer, TrainingTypeEntity specialization) {
        TrainerEntity entity = new TrainerEntity();
        entity.setUser(userMapper.toEntity(trainer.user()));
        entity.setSpecialization(specialization);
        return entity;
    }

    /** Copies the fields of the domain trainer into an already existing entity. */
    public void update(TrainerEntity target, Trainer source, TrainingTypeEntity specialization) {
        userMapper.update(target.getUser(), source.user());
        target.setSpecialization(specialization);
    }
}