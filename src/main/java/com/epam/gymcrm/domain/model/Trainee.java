package com.epam.gymcrm.domain.model;

import java.time.LocalDate;

public class Trainee extends User {
    private LocalDate dateOfBirth;
    private String address;

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "Trainee{" + super.toString() +
                ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + '\'' +
                '}';
    }
}