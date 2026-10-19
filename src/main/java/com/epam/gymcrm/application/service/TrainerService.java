package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.ProfileDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrainerService extends AbstractProfileService<Trainer> {

    private TrainerDao trainerDao;

    public TrainerService() {
        super("trainer");
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
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
}
