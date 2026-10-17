package com.epam.gymcrm;

import com.epam.gymcrm.application.service.NewTraining;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.PasswordChange;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.infrastructure.config.AppConfig;
import com.epam.gymcrm.infrastructure.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            Trainer trainer = facade.createTrainer(
                    new Trainer(null, User.of("Anna", "Brown"), new TrainingType("YOGA")));
            Trainee trainee = facade.createTrainee(
                    new Trainee(null, User.of("John", "Smith"), LocalDate.of(2000, 1, 1), "Astana"));
            log.info("Created: {} and {}", trainer, trainee);

            Credentials trainerCredentials =
                    new Credentials(trainer.user().username(), trainer.user().password());
            Credentials traineeCredentials =
                    new Credentials(trainee.user().username(), trainee.user().password());

            log.info("Credentials match: {}", facade.matchesTrainee(traineeCredentials));
            log.info("Wrong password matches: {}",
                    facade.matchesTrainee(new Credentials(traineeCredentials.username(), "wrong")));

            log.info("Not assigned trainers: {}",
                    facade.getNotAssignedTrainers(traineeCredentials).size());
            facade.updateTraineeTrainers(traineeCredentials, List.of(trainerCredentials.username()));
            log.info("Not assigned trainers after assigning: {}",
                    facade.getNotAssignedTrainers(traineeCredentials).size());

            facade.addTraining(trainerCredentials,
                    new NewTraining(traineeCredentials.username(), "Morning yoga", new TrainingType("YOGA"),
                            LocalDate.now(), 60));
            log.info("Trainee trainings: {}",
                    facade.getTraineeTrainings(traineeCredentials, null, null, null, "YOGA"));
            log.info("Trainer trainings: {}",
                    facade.getTrainerTrainings(trainerCredentials, null, null, "John"));

            log.info("Trainee active after toggle: {}", facade.toggleTraineeActive(traineeCredentials));
            facade.changeTraineePassword(new PasswordChange(traineeCredentials, "NewPass123"));

            Credentials newTraineeCredentials = new Credentials(traineeCredentials.username(), "NewPass123");
            facade.deleteTrainee(newTraineeCredentials);
            log.info("Trainee still can log in after delete: {}", facade.matchesTrainee(newTraineeCredentials));
            log.info("Trainer trainings after trainee delete: {}",
                    facade.getTrainerTrainings(trainerCredentials, null, null, null).size());
        }
    }
}
