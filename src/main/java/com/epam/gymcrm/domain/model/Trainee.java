package com.epam.gymcrm.domain.model;

import java.time.LocalDate;

public record Trainee(Long id, User user, LocalDate dateOfBirth, String address)
        implements UserProfile<Trainee> {

    @Override
    public Trainee withId(Long id) {
        return new Trainee(id, user, dateOfBirth, address);
    }

    @Override
    public Trainee withUser(User user) {
        return new Trainee(id, user, dateOfBirth, address);
    }
}
