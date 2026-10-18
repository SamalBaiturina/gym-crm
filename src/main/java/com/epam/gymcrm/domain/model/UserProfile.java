package com.epam.gymcrm.domain.model;

/**
 * A profile that has a user account: a trainee or a trainer.
 */
public interface UserProfile<T extends UserProfile<T>> extends Identifiable<T> {

    User user();

    T withUser(User user);
}
