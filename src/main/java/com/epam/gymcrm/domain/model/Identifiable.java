package com.epam.gymcrm.domain.model;

public interface Identifiable<T> {

    Long id();

    T withId(Long id);
}
