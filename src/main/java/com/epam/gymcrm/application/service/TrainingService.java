package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
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
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /** The authenticated trainer adds a training with the given trainee. */
    @Transactional
    public Training addTraining(Credentials trainerCredentials, NewTraining request) {
        Trainer trainer = authenticationService.authenticateTrainer(trainerCredentials);
        Validation.requireNotNull(request, "training");
        Validation.requireText(request.traineeUsername(), "traineeUsername");
        Validation.requireText(request.name(), "trainingName");
        Validation.requireNotNull(request.type(), "trainingType");
        Validation.requireText(request.type().name(), "trainingType");
        Validation.requireNotNull(request.date(), "trainingDate");
        Validation.requirePositive(request.duration(), "trainingDuration");

        Trainee trainee = traineeDao.findByUsername(request.traineeUsername())
                .orElseThrow(() -> new NoSuchElementException(
                        "Trainee not found: " + request.traineeUsername()));

        Training saved = trainingDao.save(new Training(null, trainee.id(), trainer.id(), request.name(),
                request.type(), request.date(), request.duration()));
        log.info("Added training id={}", saved.id());
        return saved;
    }

    /** Trainings of the trainee; every filter may be null. */
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(Credentials credentials, LocalDate from, LocalDate to,
                                              String trainerName, String trainingTypeName) {
        authenticationService.authenticateTrainee(credentials);
        return trainingDao.findForTrainee(credentials.username(), from, to, trainerName, trainingTypeName);
    }

    /** Trainings of the trainer; every filter may be null. */
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(Credentials credentials, LocalDate from, LocalDate to,
                                              String traineeName) {
        authenticationService.authenticateTrainer(credentials);
        return trainingDao.findForTrainer(credentials.username(), from, to, traineeName);
    }
}
