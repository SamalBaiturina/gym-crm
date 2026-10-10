package com.epam.gymcrm;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
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

            Trainee created = facade.createTrainee(
                    new Trainee(null, User.of("John", "Smith"), LocalDate.of(2000, 1, 1), "Astana"));
            log.info("Trainee created: {}", created);

            Trainer trainer = facade.createTrainer(
                    new Trainer(null, User.of("Anna", "Brown"), new TrainingType("YOGA")));
            log.info("Trainer created: {}", trainer);

            Training training = facade.createTraining(new Training(null, created.id(), trainer.id(),
                    "Test session", new TrainingType("YOGA"), LocalDate.now(), 60));
            log.info("Training created: {}", training);

            facade.deleteTrainee(created.id());
            log.info("Trainees left: {}", facade.getAllTrainees().size());
        }
    }
}
