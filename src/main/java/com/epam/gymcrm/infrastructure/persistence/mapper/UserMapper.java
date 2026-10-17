package com.epam.gymcrm.infrastructure.persistence.mapper;

import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        return new User(entity.getFirstName(), entity.getLastName(), entity.getUsername(),
                entity.getPassword(), entity.isActive());
    }

    public UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        update(entity, user);
        return entity;
    }

    /** Copies the fields of the domain user into an already existing entity. */
    public void update(UserEntity target, User source) {
        target.setFirstName(source.firstName());
        target.setLastName(source.lastName());
        target.setUsername(source.username());
        target.setPassword(source.password());
        target.setActive(source.isActive());
    }
}