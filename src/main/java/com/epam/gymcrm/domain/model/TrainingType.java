package com.epam.gymcrm.domain.model;

import java.util.Objects;

public class TrainingType {

    private final String name;

    public TrainingType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(name, ((TrainingType) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "TrainingType{name='" + name + "'}";
    }
}