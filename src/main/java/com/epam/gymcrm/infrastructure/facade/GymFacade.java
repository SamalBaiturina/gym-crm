package com.epam.gymcrm.infrastructure.facade;

import com.epam.gymcrm.application.service.AddTrainingCommand;
import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.PasswordChange;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.TraineeTrainingCriteria;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainerTrainingCriteria;
import com.epam.gymcrm.domain.model.Training;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Single entry point for all operations of the application; only delegates to the services.
 */
@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final AuthenticationService authenticationService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService,
                     TrainingService trainingService, AuthenticationService authenticationService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.authenticationService = authenticationService;
    }

    public Trainer createTrainer(Trainer trainer) {
        return trainerService.create(trainer);
    }

    public Trainee createTrainee(Trainee trainee) {
        return traineeService.create(trainee);
    }

    public boolean matchesTrainee(Credentials credentials) {
        return authenticationService.matchesTrainee(credentials);
    }

    public boolean matchesTrainer(Credentials credentials) {
        return authenticationService.matchesTrainer(credentials);
    }

    public Trainer getTrainer(Credentials credentials) {
        return trainerService.getByUsername(asTrainer(credentials));
    }

    public Trainee getTrainee(Credentials credentials) {
        return traineeService.getByUsername(asTrainee(credentials));
    }

    public void changeTraineePassword(PasswordChange change) {
        traineeService.changePassword(asTrainee(change.current()), change.newPassword());
    }

    public void changeTrainerPassword(PasswordChange change) {
        trainerService.changePassword(asTrainer(change.current()), change.newPassword());
    }

    public Trainer updateTrainer(Credentials credentials, Trainer changes) {
        return trainerService.update(asTrainer(credentials), changes);
    }

    public Trainee updateTrainee(Credentials credentials, Trainee changes) {
        return traineeService.update(asTrainee(credentials), changes);
    }

    public boolean toggleTraineeActive(Credentials credentials) {
        return traineeService.toggleActive(asTrainee(credentials));
    }

    public boolean toggleTrainerActive(Credentials credentials) {
        return trainerService.toggleActive(asTrainer(credentials));
    }

    public void deleteTrainee(Credentials credentials) {
        traineeService.delete(asTrainee(credentials));
    }

    public List<Training> getTraineeTrainings(Credentials credentials, TraineeTrainingCriteria criteria) {
        return trainingService.getTraineeTrainings(asTrainee(credentials), criteria);
    }

    public List<Training> getTrainerTrainings(Credentials credentials, TrainerTrainingCriteria criteria) {
        return trainingService.getTrainerTrainings(asTrainer(credentials), criteria);
    }

    /** The trainer, authenticated by his credentials, adds a training with the given trainee. */
    public Training addTraining(Credentials trainerCredentials, String traineeUsername, String name,
                                LocalDate date, int duration) {
        authenticationService.authenticateTrainer(trainerCredentials);
        String trainerUsername = trainerCredentials.username();
        return trainingService.addTraining(
                new AddTrainingCommand(trainerUsername, traineeUsername, trainerUsername, name, date, duration));
    }

    public List<Trainer> getNotAssignedTrainers(Credentials traineeCredentials) {
        return traineeService.getNotAssignedTrainers(asTrainee(traineeCredentials));
    }

    public List<Trainer> updateTraineeTrainers(Credentials credentials, List<String> trainerUsernames) {
        return traineeService.updateTrainers(asTrainee(credentials), trainerUsernames);
    }

    /** Checks the password once and returns the trainee as an already authenticated user. */
    private AuthenticatedUser asTrainee(Credentials credentials) {
        boolean active = authenticationService.authenticateTrainee(credentials).user().isActive();
        return new AuthenticatedUser(credentials, Role.TRAINEE, active);
    }

    /** Checks the password once and returns the trainer as an already authenticated user. */
    private AuthenticatedUser asTrainer(Credentials credentials) {
        boolean active = authenticationService.authenticateTrainer(credentials).user().isActive();
        return new AuthenticatedUser(credentials, Role.TRAINER, active);
    }
}
