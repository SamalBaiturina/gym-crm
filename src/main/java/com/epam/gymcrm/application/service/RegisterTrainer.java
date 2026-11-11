package com.epam.gymcrm.application.service;

/** Input of the use case "register a trainer". The specialization is given by the id of a training type. */
public record RegisterTrainer(String firstName, String lastName, Long specializationId) {
}
