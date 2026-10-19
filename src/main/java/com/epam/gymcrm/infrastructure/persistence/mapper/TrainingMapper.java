package com.epam.gymcrm.infrastructure.persistence.mapper;

import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingTypeMapper trainingTypeMapper;

    public TrainingMapper(TraineeMapper traineeMapper, TrainerMapper trainerMapper,
                          TrainingTypeMapper trainingTypeMapper) {
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainingTypeMapper = trainingTypeMapper;
    }

    public Training toDomain(TrainingEntity entity) {
        return new Training(entity.getId(),
                traineeMapper.toDomain(entity.getTrainee()),
                trainerMapper.toDomain(entity.getTrainer()),
                entity.getName(), trainingTypeMapper.toDomain(entity.getTrainingType()),
                entity.getDate(), entity.getDuration());
    }

    public TrainingEntity toEntity(Training training, TraineeEntity trainee,
                                   TrainerEntity trainer, TrainingTypeEntity type) {
        TrainingEntity entity = new TrainingEntity();
        entity.setTrainee(trainee);
        entity.setTrainer(trainer);
        entity.setTrainingType(type);
        entity.setName(training.trainingName());
        entity.setDate(training.trainingDate());
        entity.setDuration(training.trainingDuration());
        return entity;
    }
}
