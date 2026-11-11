package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.ProfileDao;
import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.domain.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Trainer operations. The specialization is set at registration and does not change. */
@Service
public class TrainerService extends AbstractProfileService<Trainer> {

    private TrainerDao trainerDao;
    private TraineeDao traineeDao;
    private TrainingTypeService trainingTypeService;

    public TrainerService() {
        super(Role.TRAINER);
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainingTypeService(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Override
    protected ProfileDao<Trainer> dao() {
        return trainerDao;
    }

    /** The specialization is the trainer's own field. */
    @Override
    protected Trainer applyChanges(Trainer existing, Trainer changes) {
        return new Trainer(existing.id(), existing.user(), changes.specialization());
    }

    @Override
    protected void validateDetails(Trainer trainer) {
        Validation.requireNotNull(trainer.specialization(), "specialization");
        Validation.requireText(trainer.specialization().name(), "specialization");
    }

    /**
     * Registers a trainer from the plain input of the use case: finds the specialization by id and creates
     * the account in one transaction. An unknown specialization id is a mistake in the request.
     */
    @Transactional
    public Trainer register(RegisterTrainer command) {
        Validation.validated("register trainer", () -> Validation.requireNotNull(command, "trainer"));
        TrainingType specialization = trainingTypeService.requireById(command.specializationId());
        return create(new Trainer(null, User.of(command.firstName(), command.lastName()), specialization));
    }

    @Transactional(readOnly = true)
    public TrainerProfile getProfile(AuthenticatedUser user) {
        Trainer trainer = load(user);
        return new TrainerProfile(trainer, traineeDao.findAssignedTo(user.username()));
    }

    /**
     * Updates the names and the active flag. The specialization is read-only,
     * so the one the trainer already has is kept.
     */
    @Transactional
    public TrainerProfile updateProfile(AuthenticatedUser user, String firstName, String lastName,
                                        boolean active) {
        Trainer current = load(user);
        Trainer changes = new Trainer(current.id(), User.of(firstName, lastName), current.specialization());
        Trainer saved = updateWithActive(user, changes, active);
        return new TrainerProfile(saved, traineeDao.findAssignedTo(user.username()));
    }
}
