package com.epam.gymcrm.domain.model;

/** An object with a database id; withId returns a copy with the id set. */
public interface Identifiable<T> {

    Long id();

    T withId(Long id);
}
