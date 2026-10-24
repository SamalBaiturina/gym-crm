package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Builds the username as FirstName.LastName and adds a serial number if it is taken (John.Smith,
 * John.Smith1, ...). Usernames are unique across trainees and trainers.
 */
@Component
public class UsernameGenerator {

    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);

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

    /** Returns the first free username for the given names. */
    public String generate(String firstName, String lastName) {
        String base = firstName + "." + lastName;

        Set<String> taken = new HashSet<>(traineeDao.findUsernamesStartingWith(base));
        taken.addAll(trainerDao.findUsernamesStartingWith(base));

        String username = base;
        int serial = 0;
        while (taken.contains(username)) {
            serial++;
            username = base + serial;
        }
        log.debug("Generated username {}", username);
        return username;
    }
}
