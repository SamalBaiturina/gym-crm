package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.ProfileDao;
import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Checks a username and password against trainees and trainers. An unknown username and a wrong
 * password give the same result, so the response does not reveal which usernames exist.
 */
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

    /**
     * Finds out who is calling in one pass: looks the user up by username (the trainees first, then the
     * trainers), checks the password and returns the role and the active flag together with the credentials.
     *
     * @throws AuthenticationException if there is no such user or the password is wrong
     */
    @Transactional(readOnly = true)
    public AuthenticatedUser authenticateAndLoad(Credentials credentials) {
        return find(traineeDao, credentials)
                .map(trainee -> new AuthenticatedUser(credentials, Role.TRAINEE, trainee.user().isActive()))
                .or(() -> find(trainerDao, credentials)
                        .map(trainer -> new AuthenticatedUser(credentials, Role.TRAINER, trainer.user().isActive())))
                .orElseThrow(() -> {
                    log.warn("Authentication failed for username={}", credentials.username());
                    return new AuthenticationException();
                });
    }

    private <T extends UserProfile<T>> Optional<T> find(ProfileDao<T> dao, Credentials credentials) {
        return dao.findByUsername(credentials.username())
                .filter(profile -> profile.user().passwordMatches(credentials.password()));
    }
}
