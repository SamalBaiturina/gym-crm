package com.epam.gymcrm.domain.model;

import java.time.LocalDate;

public record Trainee(Long id, User user, LocalDate dateOfBirth, String address)
        implements Identifiable<Trainee> {

    @Override
    public Trainee withId(Long id) {
        return new Trainee(id, user, dateOfBirth, address);
    }

    public Trainee withUser(User user) {
        return new Trainee(id, user, dateOfBirth, address);
    }
}
