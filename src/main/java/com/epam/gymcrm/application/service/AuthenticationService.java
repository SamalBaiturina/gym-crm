package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Transactional(readOnly = true)
    public boolean matchesTrainee(Credentials credentials) {
        return findTrainee(credentials).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean matchesTrainer(Credentials credentials) {
        return findTrainer(credentials).isPresent();
    }

    /** Returns the trainee if the credentials are valid; unknown username and wrong password look the same. */
    @Transactional(readOnly = true)
    public Trainee authenticateTrainee(Credentials credentials) {
        return findTrainee(credentials).orElseThrow(() -> {
            log.warn("Trainee authentication failed for username={}", credentials.username());
            return new AuthenticationException();
        });
    }

    /** Returns the trainer if the credentials are valid; unknown username and wrong password look the same. */
    @Transactional(readOnly = true)
    public Trainer authenticateTrainer(Credentials credentials) {
        return findTrainer(credentials).orElseThrow(() -> {
            log.warn("Trainer authentication failed for username={}", credentials.username());
            return new AuthenticationException();
        });
    }

    private Optional<Trainee> findTrainee(Credentials credentials) {
        return traineeDao.findByUsername(credentials.username())
                .filter(trainee -> trainee.user().passwordMatches(credentials.password()));
    }

    private Optional<Trainer> findTrainer(Credentials credentials) {
        return trainerDao.findByUsername(credentials.username())
                .filter(trainer -> trainer.user().passwordMatches(credentials.password()));
    }
}
