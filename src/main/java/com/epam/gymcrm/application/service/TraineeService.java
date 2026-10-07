package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.domain.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainee create(Trainee trainee) {
        trainee.setUsername(usernameGenerator.generate(trainee.getFirstName(), trainee.getLastName()));
        trainee.setPassword(passwordGenerator.generate());
        trainee.setActive(true);
        Trainee saved = traineeDao.save(trainee);
        log.info("Created trainee id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    public Trainee update(Trainee trainee) {
        if (trainee.getId() == null || traineeDao.findById(trainee.getId()).isEmpty()) {
            log.warn("Trainee update failed, not found: id={}", trainee.getId());
            throw new NoSuchElementException("Trainee not found: " + trainee.getId());
        }
        Trainee saved = traineeDao.save(trainee);
        log.info("Updated trainee id={}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        traineeDao.deleteById(id);
        log.info("Deleted trainee id={}", id);
    }

    public Optional<Trainee> findById(Long id) {
        return traineeDao.findById(id);
    }

    public List<Trainee> findAll() {
        return traineeDao.findAll();
    }
}