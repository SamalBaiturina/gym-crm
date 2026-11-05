package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.exception.ForbiddenOperationException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.TraineeTrainingCriteria;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainerTrainingCriteria;
import com.epam.gymcrm.domain.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Adding trainings and listing them for a trainee or a trainer. */
@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    /**
     * Adds a training between the trainee and the trainer found by username. The training type is
     * the trainer's specialization. The caller must be the trainee or the trainer of the training.
     * The caller has already proved who he is (the web layer or the facade authenticates him),
     * so no password is passed here.
     *
     * @throws ForbiddenOperationException if the caller is neither the trainee nor the trainer
     */
    @Transactional
    public Training addTraining(AddTrainingCommand command) {
        Validation.validated("add training", () -> {
            Validation.requireNotNull(command, "training");
            Validation.requireText(command.caller(), "caller");
            Validation.requireText(command.traineeUsername(), "traineeUsername");
            Validation.requireText(command.trainerUsername(), "trainerUsername");
            Validation.requireText(command.name(), "trainingName");
            Validation.requireNotNull(command.date(), "trainingDate");
            Validation.requirePositive(command.duration(), "trainingDuration");
        });
        requireTraineeOrTrainer(command);

        Trainee trainee = traineeDao.findByUsername(command.traineeUsername())
                .orElseThrow(() -> notFound("trainee", command.traineeUsername()));
        Trainer trainer = trainerDao.findByUsername(command.trainerUsername())
                .orElseThrow(() -> notFound("trainer", command.trainerUsername()));

        Training saved = trainingDao.save(new Training(null, trainee, trainer, command.name(),
                trainer.specialization(), command.date(), command.duration()));
        log.info("Added training id={}, trainer={}, trainee={}",
                saved.id(), command.trainerUsername(), command.traineeUsername());
        return saved;
    }

    /** Trainings of the trainee, narrowed by the optional criteria. */
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(AuthenticatedUser user, TraineeTrainingCriteria criteria) {
        user.requireRole(Role.TRAINEE);
        Validation.validated("get trainings of trainee username=" + user.username(),
                () -> Validation.requireNotNull(criteria, "criteria"));
        return trainingDao.findForTrainee(user.username(), criteria);
    }

    /** Trainings of the trainer, narrowed by the optional criteria. */
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(AuthenticatedUser user, TrainerTrainingCriteria criteria) {
        user.requireRole(Role.TRAINER);
        Validation.validated("get trainings of trainer username=" + user.username(),
                () -> Validation.requireNotNull(criteria, "criteria"));
        return trainingDao.findForTrainer(user.username(), criteria);
    }

    private void requireTraineeOrTrainer(AddTrainingCommand command) {
        if (!command.caller().equals(command.traineeUsername())
                && !command.caller().equals(command.trainerUsername())) {
            log.warn("Cannot add training: caller username={} is neither trainee username={} nor trainer username={}",
                    command.caller(), command.traineeUsername(), command.trainerUsername());
            throw new ForbiddenOperationException("A training can be added only by its trainee or its trainer");
        }
    }

    private NotFoundException notFound(String role, String username) {
        log.warn("Cannot add training: {} username={} not found", role, username);
        return new NotFoundException(role.substring(0, 1).toUpperCase() + role.substring(1)
                + " not found: " + username);
    }
}
