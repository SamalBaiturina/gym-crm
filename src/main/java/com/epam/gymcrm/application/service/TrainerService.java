package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.PasswordChange;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private AuthenticationService authenticationService;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /** Creates a trainer profile; username and password are generated. */
    @Transactional
    public Trainer create(Trainer trainer) {
        Validation.requireNotNull(trainer, "trainer");
        Validation.requireUser(trainer.user());
        requireSpecialization(trainer);
        User user = trainer.user();
        String username = usernameGenerator.generate(user.firstName(), user.lastName());
        String password = passwordGenerator.generate();
        Trainer saved = trainerDao.save(trainer.withUser(user.withCredentials(username, password)));
        log.info("Created trainer id={}, username={}", saved.id(), username);
        return saved;
    }

    @Transactional(readOnly = true)
    public Trainer getByUsername(Credentials credentials) {
        return authenticationService.authenticateTrainer(credentials);
    }

    @Transactional
    public void changePassword(PasswordChange change) {
        Trainer trainer = authenticationService.authenticateTrainer(change.current());
        trainerDao.save(trainer.withUser(trainer.user().withPassword(change.newPassword())));
        log.info("Password changed for trainer username={}", change.current().username());
    }

    /** Updates names and specialization; username and password stay the same. */
    @Transactional
    public Trainer update(Credentials credentials, Trainer changes) {
        Trainer existing = authenticationService.authenticateTrainer(credentials);
        Validation.requireNotNull(changes, "trainer");
        Validation.requireUser(changes.user());
        requireSpecialization(changes);
        User user = existing.user().withNames(changes.user().firstName(), changes.user().lastName());
        Trainer saved = trainerDao.save(new Trainer(existing.id(), user, changes.specialization()));
        log.info("Updated trainer username={}", credentials.username());
        return saved;
    }

    /** Flips the active flag on every call (not idempotent) and returns the new value. */
    @Transactional
    public boolean toggleActive(Credentials credentials) {
        Trainer trainer = authenticationService.authenticateTrainer(credentials);
        boolean active = !trainer.user().isActive();
        trainerDao.save(trainer.withUser(trainer.user().withActive(active)));
        log.info("Trainer username={} is now active={}", credentials.username(), active);
        return active;
    }

    private void requireSpecialization(Trainer trainer) {
        Validation.requireNotNull(trainer.specialization(), "specialization");
        Validation.requireText(trainer.specialization().name(), "specialization");
    }
}
