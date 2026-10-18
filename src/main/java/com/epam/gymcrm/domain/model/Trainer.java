package com.epam.gymcrm.domain.model;

public record Trainer(Long id, User user, TrainingType specialization)
        implements UserProfile<Trainer> {

    @Override
    public Trainer withId(Long id) {
        return new Trainer(id, user, specialization);
    }

    @Override
    public Trainer withUser(User user) {
        return new Trainer(id, user, specialization);
    }
}
