package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.TraineeTrainingCriteria;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainerTrainingCriteria;
import com.epam.gymcrm.domain.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Adding trainings and listing them for a trainee or a trainer. */
@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private AuthenticationService authenticationService;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /** The authenticated trainer adds a training with the given trainee. */
    @Transactional
    public Training addTraining(Credentials trainerCredentials, NewTraining request) {
        Trainer trainer = authenticationService.authenticateTrainer(trainerCredentials);
        String trainerUsername = trainerCredentials.username();
        Validation.validated("add training by trainer username=" + trainerUsername, () -> {
            Validation.requireNotNull(request, "training");
            Validation.requireText(request.traineeUsername(), "traineeUsername");
            Validation.requireText(request.name(), "trainingName");
            Validation.requireNotNull(request.type(), "trainingType");
            Validation.requireText(request.type().name(), "trainingType");
            Validation.requireNotNull(request.date(), "trainingDate");
            Validation.requirePositive(request.duration(), "trainingDuration");
        });

        Trainee trainee = traineeDao.findByUsername(request.traineeUsername())
                .orElseThrow(() -> {
                    log.warn("Cannot add training: trainee username={} not found (trainer username={})",
                            request.traineeUsername(), trainerUsername);
                    return new NotFoundException("Trainee not found: " + request.traineeUsername());
                });

        Training saved = trainingDao.save(new Training(null, trainee, trainer, request.name(),
                request.type(), request.date(), request.duration()));
        log.info("Added training id={}, trainer={}, trainee={}",
                saved.id(), trainerUsername, request.traineeUsername());
        return saved;
    }

    /**
     * Adds a training between the trainee and the trainer found by username. The training type is
     * the trainer's specialization. The caller has already been authenticated and checked by the
     * web layer, so no credentials are passed here.
     */
    @Transactional
    public Training addTraining(AddTrainingCommand command) {
        Validation.validated("add training", () -> {
            Validation.requireNotNull(command, "training");
            Validation.requireText(command.traineeUsername(), "traineeUsername");
            Validation.requireText(command.trainerUsername(), "trainerUsername");
            Validation.requireText(command.name(), "trainingName");
            Validation.requireNotNull(command.date(), "trainingDate");
            Validation.requirePositive(command.duration(), "trainingDuration");
        });

        Trainee trainee = traineeDao.findByUsername(command.traineeUsername())
                .orElseThrow(() -> notFound("trainee", command.traineeUsername()));
        Trainer trainer = trainerDao.findByUsername(command.trainerUsername())
                .orElseThrow(() -> notFound("trainer", command.trainerUsername()));

        Training saved = trainingDao.save(new Training(null, trainee, trainer, command.name(),
                trainer.specialization(), command.date(), command.duration()));
        log.info("Added training id={}, trainer={}, trainee={}",
                saved.id(), command.trainerUsername(), command.traineeUsername());
        return saved;
    }

    /** Trainings of the trainee, narrowed by the optional criteria. */
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(Credentials credentials, TraineeTrainingCriteria criteria) {
        authenticationService.authenticateTrainee(credentials);
        Validation.validated("get trainings of trainee username=" + credentials.username(),
                () -> Validation.requireNotNull(criteria, "criteria"));
        return trainingDao.findForTrainee(credentials.username(), criteria);
    }

    /** Trainings of the trainer, narrowed by the optional criteria. */
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(Credentials credentials, TrainerTrainingCriteria criteria) {
        authenticationService.authenticateTrainer(credentials);
        Validation.validated("get trainings of trainer username=" + credentials.username(),
                () -> Validation.requireNotNull(criteria, "criteria"));
        return trainingDao.findForTrainer(credentials.username(), criteria);
    }

    private NotFoundException notFound(String role, String username) {
        log.warn("Cannot add training: {} username={} not found", role, username);
        return new NotFoundException(role.substring(0, 1).toUpperCase() + role.substring(1)
                + " not found: " + username);
    }
}
