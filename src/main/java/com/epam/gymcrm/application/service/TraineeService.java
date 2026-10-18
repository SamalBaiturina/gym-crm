package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.ProfileDao;
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

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TraineeService extends AbstractProfileService<Trainee> {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    public TraineeService() {
        super("trainee");
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Override
    protected ProfileDao<Trainee> dao() {
        return traineeDao;
    }

    @Override
    protected Trainee authenticate(Credentials credentials) {
        return authentication().authenticateTrainee(credentials);
    }

    /** Date of birth and address are the trainee's own fields. */
    @Override
    protected Trainee applyChanges(Trainee existing, Trainee changes) {
        return new Trainee(existing.id(), existing.user(), changes.dateOfBirth(), changes.address());
    }

    /** Deletes the trainee; trainings are deleted by cascade. */
    @Transactional
    public void delete(Credentials credentials) {
        Trainee trainee = authenticate(credentials);
        traineeDao.delete(trainee);
        log.info("Deleted trainee username={}", credentials.username());
    }

    @Transactional(readOnly = true)
    public List<Trainer> getNotAssignedTrainers(Credentials credentials) {
        authenticate(credentials);
        return trainerDao.findNotAssignedTo(credentials.username());
    }

    /** Replaces the trainee's trainers with the given ones. */
    @Transactional
    public List<Trainer> updateTrainers(Credentials credentials, List<String> trainerUsernames) {
        Trainee trainee = authenticate(credentials);
        Validation.requireNotNull(trainerUsernames, "trainerUsernames");

        Set<String> requested = new HashSet<>(trainerUsernames);
        List<Trainer> trainers = trainerDao.findByUsernames(requested);
        if (trainers.size() != requested.size()) {
            Set<String> found = trainers.stream().map(t -> t.user().username()).collect(Collectors.toSet());
            requested.removeAll(found);
            throw new NoSuchElementException("Trainers not found: " + requested);
        }
        traineeDao.updateTrainers(trainee, trainers);
        log.info("Updated trainers of trainee username={}, count={}", credentials.username(), trainers.size());
        return trainers;
    }
}
