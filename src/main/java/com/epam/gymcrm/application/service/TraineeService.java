package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.User;
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
        User user = trainee.user();
        String username = usernameGenerator.generate(user.firstName(), user.lastName());
        String password = passwordGenerator.generate();
        Trainee saved = traineeDao.save(trainee.withUser(user.withCredentials(username, password)));
        log.info("Created trainee id={}, username={}", saved.id(), username);
        return saved;
    }

    public Trainee update(Trainee trainee) {
        if (trainee.id() == null || traineeDao.findById(trainee.id()).isEmpty()) {
            log.warn("Trainee not found for update, id={}", trainee.id());
            throw new NoSuchElementException("Trainee not found: " + trainee.id());
        }
        Trainee saved = traineeDao.save(trainee);
        log.info("Updated trainee id={}", saved.id());
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
