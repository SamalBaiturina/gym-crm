package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.ProfileDao;
import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.UserProfile;
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
        return find(traineeDao, credentials).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean matchesTrainer(Credentials credentials) {
        return find(trainerDao, credentials).isPresent();
    }

    /** Returns the trainee if the credentials are valid; unknown username and wrong password look the same. */
    @Transactional(readOnly = true)
    public Trainee authenticateTrainee(Credentials credentials) {
        return authenticate(traineeDao, "Trainee", credentials);
    }

    /** Returns the trainer if the credentials are valid; unknown username and wrong password look the same. */
    @Transactional(readOnly = true)
    public Trainer authenticateTrainer(Credentials credentials) {
        return authenticate(trainerDao, "Trainer", credentials);
    }

    private <T extends UserProfile<T>> Optional<T> find(ProfileDao<T> dao, Credentials credentials) {
        return dao.findByUsername(credentials.username())
                .filter(profile -> profile.user().passwordMatches(credentials.password()));
    }

    private <T extends UserProfile<T>> T authenticate(ProfileDao<T> dao, String role, Credentials credentials) {
        return find(dao, credentials).orElseThrow(() -> {
            log.warn("{} authentication failed for username={}", role, credentials.username());
            return new AuthenticationException();
        });
    }
}
