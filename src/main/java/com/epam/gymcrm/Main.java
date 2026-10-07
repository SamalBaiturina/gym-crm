package com.epam.gymcrm;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.infrastructure.config.AppConfig;
import com.epam.gymcrm.infrastructure.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            Trainee trainee = new Trainee();
            trainee.setFirstName("John");
            trainee.setLastName("Smith");
            trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
            trainee.setAddress("Astana");
            Trainee created = facade.createTrainee(trainee);
            log.info("Created: {}", created);

            Trainer trainer = new Trainer();
            trainer.setFirstName("Anna");
            trainer.setLastName("Brown");
            trainer.setSpecialization(new TrainingType("YOGA"));
            log.info("Created: {}", facade.createTrainer(trainer));

            Training training = new Training();
            training.setTraineeId(created.getId());
            training.setTrainerId(1L);
            training.setTrainingName("Test session");
            training.setTrainingType(new TrainingType("YOGA"));
            training.setTrainingDate(LocalDate.now());
            training.setTrainingDuration(60);
            log.info("Created: {}", facade.createTraining(training));

            facade.deleteTrainee(created.getId());
            log.info("Trainees left: {}", facade.getAllTrainees().size());
        }
    }
}