package com.epam.gymcrm.domain.model;

public class Trainer extends User {
    private TrainingType specialization;

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    @Override
    public String toString() {
        return "Trainer{" + super.toString() +
                ", specialization=" + specialization +
                '}';
    }
}