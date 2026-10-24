package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.ProfileDao;
import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Trainer operations. The specialization is set at registration and does not change. */
@Service
public class TrainerService extends AbstractProfileService<Trainer> {

    private TrainerDao trainerDao;
    private TraineeDao traineeDao;

    public TrainerService() {
        super("trainer");
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Override
    protected ProfileDao<Trainer> dao() {
        return trainerDao;
    }

    @Override
    protected Trainer authenticate(Credentials credentials) {
        return authentication().authenticateTrainer(credentials);
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

    @Transactional(readOnly = true)
    public TrainerProfile getProfile(Credentials credentials) {
        Trainer trainer = authenticate(credentials);
        return new TrainerProfile(trainer, traineeDao.findAssignedTo(credentials.username()));
    }

    /**
     * Updates the names and the active flag. The specialization is read-only,
     * so the one the trainer already has is kept.
     */
    @Transactional
    public TrainerProfile updateProfile(Credentials credentials, String firstName, String lastName,
                                        boolean active) {
        Trainer current = authenticate(credentials);
        Trainer changes = new Trainer(current.id(), User.of(firstName, lastName), current.specialization());
        Trainer saved = updateWithActive(credentials, changes, active);
        return new TrainerProfile(saved, traineeDao.findAssignedTo(credentials.username()));
    }
}
