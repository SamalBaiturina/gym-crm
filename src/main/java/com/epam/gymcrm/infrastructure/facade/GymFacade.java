package com.epam.gymcrm.infrastructure.facade;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.NewTraining;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.PasswordChange;
import com.epam.gymcrm.domain.model.TraineeTrainingCriteria;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainerTrainingCriteria;
import com.epam.gymcrm.domain.model.Training;
import org.springframework.stereotype.Component;

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
        return trainerService.getByUsername(credentials);
    }

    public Trainee getTrainee(Credentials credentials) {
        return traineeService.getByUsername(credentials);
    }

    public void changeTraineePassword(PasswordChange change) {
        traineeService.changePassword(change);
    }

    public void changeTrainerPassword(PasswordChange change) {
        trainerService.changePassword(change);
    }

    public Trainer updateTrainer(Credentials credentials, Trainer changes) {
        return trainerService.update(credentials, changes);
    }

    public Trainee updateTrainee(Credentials credentials, Trainee changes) {
        return traineeService.update(credentials, changes);
    }

    public boolean toggleTraineeActive(Credentials credentials) {
        return traineeService.toggleActive(credentials);
    }

    public boolean toggleTrainerActive(Credentials credentials) {
        return trainerService.toggleActive(credentials);
    }

    public void deleteTrainee(Credentials credentials) {
        traineeService.delete(credentials);
    }

    public List<Training> getTraineeTrainings(Credentials credentials, TraineeTrainingCriteria criteria) {
        return trainingService.getTraineeTrainings(credentials, criteria);
    }

    public List<Training> getTrainerTrainings(Credentials credentials, TrainerTrainingCriteria criteria) {
        return trainingService.getTrainerTrainings(credentials, criteria);
    }

    public Training addTraining(Credentials trainerCredentials, NewTraining training) {
        return trainingService.addTraining(trainerCredentials, training);
    }

    public List<Trainer> getNotAssignedTrainers(Credentials traineeCredentials) {
        return traineeService.getNotAssignedTrainers(traineeCredentials);
    }

    public List<Trainer> updateTraineeTrainers(Credentials credentials, List<String> trainerUsernames) {
        return traineeService.updateTrainers(credentials, trainerUsernames);
    }
}
