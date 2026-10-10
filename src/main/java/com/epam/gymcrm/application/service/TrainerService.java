package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

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

    public Trainer create(Trainer trainer) {
        User user = trainer.user();
        String username = usernameGenerator.generate(user.firstName(), user.lastName());
        String password = passwordGenerator.generate();
        Trainer saved = trainerDao.save(trainer.withUser(user.withCredentials(username, password)));
        log.info("Created trainer id={}, username={}", saved.id(), username);
        return saved;
    }

    public Trainer update(Trainer trainer) {
        if (trainer.id() == null || trainerDao.findById(trainer.id()).isEmpty()) {
            log.warn("Trainer not found for update, id={}", trainer.id());
            throw new NoSuchElementException("Trainer not found: " + trainer.id());
        }
        Trainer saved = trainerDao.save(trainer);
        log.info("Updated trainer id={}", saved.id());
        return saved;
    }

    public Optional<Trainer> findById(Long id) {
        return trainerDao.findById(id);
    }

    public List<Trainer> findAll() {
        return trainerDao.findAll();
    }
}
