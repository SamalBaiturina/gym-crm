package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.ProfileDao;
import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Trainee operations: profile with trainers, delete, and the list of the trainee's trainers. */
@Service
public class TraineeService extends AbstractProfileService<Trainee> {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    public TraineeService() {
        super(Role.TRAINEE);
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

    /** Date of birth and address are the trainee's own fields. */
    @Override
    protected Trainee applyChanges(Trainee existing, Trainee changes) {
        return new Trainee(existing.id(), existing.user(), changes.dateOfBirth(), changes.address());
    }

    /** Registers a trainee from the plain input of the use case; username and password are generated. */
    @Transactional
    public Trainee register(RegisterTrainee command) {
        Validation.validated("register trainee", () -> Validation.requireNotNull(command, "trainee"));
        return create(new Trainee(null, User.of(command.firstName(), command.lastName()),
                command.dateOfBirth(), command.address()));
    }

    /** Deletes the trainee; trainings are deleted by cascade. */
    @Transactional
    public void delete(AuthenticatedUser user) {
        Trainee trainee = load(user);
        traineeDao.delete(trainee);
        log.info("Deleted trainee username={}", user.username());
    }

    @Transactional(readOnly = true)
    public TraineeProfile getProfile(AuthenticatedUser user) {
        Trainee trainee = load(user);
        return new TraineeProfile(trainee, trainerDao.findAssignedTo(user.username()));
    }

    /** Updates the profile, including the active flag, and returns it with the current trainers. */
    @Transactional
    public TraineeProfile updateProfile(AuthenticatedUser user, UpdateTrainee command) {
        Validation.validated("update trainee username=" + user.username(),
                () -> Validation.requireNotNull(command, "trainee"));
        Trainee changes = new Trainee(null, User.of(command.firstName(), command.lastName()),
                command.dateOfBirth(), command.address());
        Trainee saved = updateWithActive(user, changes, command.active());
        return new TraineeProfile(saved, trainerDao.findAssignedTo(user.username()));
    }

    @Transactional(readOnly = true)
    public List<Trainer> getNotAssignedTrainers(AuthenticatedUser user) {
        user.requireRole(Role.TRAINEE);
        return trainerDao.findNotAssignedTo(user.username());
    }

    /** Replaces the trainee's trainers with the given ones. */
    @Transactional
    public List<Trainer> updateTrainers(AuthenticatedUser user, List<String> trainerUsernames) {
        Trainee trainee = load(user);
        Validation.validated("update trainers of trainee username=" + user.username(),
                () -> Validation.requireNotNull(trainerUsernames, "trainerUsernames"));

        Set<String> requested = new HashSet<>(trainerUsernames);
        List<Trainer> trainers = trainerDao.findByUsernames(requested);
        if (trainers.size() != requested.size()) {
            Set<String> found = trainers.stream().map(t -> t.user().username()).collect(Collectors.toSet());
            requested.removeAll(found);
            log.warn("Cannot update trainers of trainee username={}: trainers not found {}",
                    user.username(), requested);
            throw new NotFoundException("Trainers not found: " + requested);
        }
        traineeDao.updateTrainers(trainee, trainers);
        log.info("Updated trainers of trainee username={}, count={}", user.username(), trainers.size());
        return trainers;
    }
}
