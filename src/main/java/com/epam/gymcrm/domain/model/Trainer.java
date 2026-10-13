package com.epam.gymcrm.domain.model;

public record Trainer(Long id, User user, TrainingType specialization)
        implements Identifiable<Trainer> {

    @Override
    public Trainer withId(Long id) {
        return new Trainer(id, user, specialization);
    }

    public Trainer withUser(User user) {
        return new Trainer(id, user, specialization);
    }
}
