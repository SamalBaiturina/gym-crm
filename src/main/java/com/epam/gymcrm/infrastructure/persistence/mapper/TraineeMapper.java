package com.epam.gymcrm.infrastructure.persistence.mapper;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import org.springframework.stereotype.Component;

@Component
public class TraineeMapper {

    private final UserMapper userMapper;

    public TraineeMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Trainee toDomain(TraineeEntity entity) {
        return new Trainee(entity.getId(), userMapper.toDomain(entity.getUser()),
                entity.getDateOfBirth(), entity.getAddress());
    }

    public TraineeEntity toEntity(Trainee trainee) {
        TraineeEntity entity = new TraineeEntity();
        entity.setUser(userMapper.toEntity(trainee.user()));
        entity.setDateOfBirth(trainee.dateOfBirth());
        entity.setAddress(trainee.address());
        return entity;
    }

    /** Copies the fields of the domain trainee into an already existing entity. */
    public void update(TraineeEntity target, Trainee source) {
        userMapper.update(target.getUser(), source.user());
        target.setDateOfBirth(source.dateOfBirth());
        target.setAddress(source.address());
    }
}