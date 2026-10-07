package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Trainer;
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
        trainer.setUsername(usernameGenerator.generate(trainer.getFirstName(), trainer.getLastName()));
        trainer.setPassword(passwordGenerator.generate());
        trainer.setActive(true);
        Trainer saved = trainerDao.save(trainer);
        log.info("Created trainer id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    public Trainer update(Trainer trainer) {
        if (trainer.getId() == null || trainerDao.findById(trainer.getId()).isEmpty()) {
            log.warn("Trainer update failed, not found: id={}", trainer.getId());
            throw new NoSuchElementException("Trainer not found: " + trainer.getId());
        }
        Trainer saved = trainerDao.save(trainer);
        log.info("Updated trainer id={}", saved.getId());
        return saved;
    }

    public Optional<Trainer> findById(Long id) {
        return trainerDao.findById(id);
    }

    public List<Trainer> findAll() {
        return trainerDao.findAll();
    }
}
