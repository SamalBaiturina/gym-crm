package com.epam.gymcrm.infrastructure.facade;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.NewTraining;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

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

    // 1. Create trainer profile
    public Trainer createTrainer(Trainer trainer) {
        return trainerService.create(trainer);
    }

    // 2. Create trainee profile
    public Trainee createTrainee(Trainee trainee) {
        return traineeService.create(trainee);
    }

    // 3. Trainee username and password matching
    public boolean matchesTrainee(String username, String password) {
        return authenticationService.matchesTrainee(username, password);
    }

    // 4. Trainer username and password matching
    public boolean matchesTrainer(String username, String password) {
        return authenticationService.matchesTrainer(username, password);
    }

    // 5. Select trainer profile by username
    public Trainer getTrainer(String username, String password) {
        return trainerService.getByUsername(username, password);
    }

    // 6. Select trainee profile by username
    public Trainee getTrainee(String username, String password) {
        return traineeService.getByUsername(username, password);
    }

    // 7. Trainee password change
    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    // 8. Trainer password change
    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    // 9. Update trainer profile
    public Trainer updateTrainer(String username, String password, Trainer changes) {
        return trainerService.update(username, password, changes);
    }

    // 10. Update trainee profile
    public Trainee updateTrainee(String username, String password, Trainee changes) {
        return traineeService.update(username, password, changes);
    }

    // 11. Activate/De-activate trainee
    public boolean toggleTraineeActive(String username, String password) {
        return traineeService.toggleActive(username, password);
    }

    // 12. Activate/De-activate trainer
    public boolean toggleTrainerActive(String username, String password) {
        return trainerService.toggleActive(username, password);
    }

    // 13. Delete trainee profile by username
    public void deleteTrainee(String username, String password) {
        traineeService.delete(username, password);
    }

    // 14. Get trainee trainings list by criteria
    public List<Training> getTraineeTrainings(String username, String password, LocalDate from,
                                              LocalDate to, String trainerName, String trainingTypeName) {
        return trainingService.getTraineeTrainings(username, password, from, to, trainerName, trainingTypeName);
    }

    // 15. Get trainer trainings list by criteria
    public List<Training> getTrainerTrainings(String username, String password, LocalDate from,
                                              LocalDate to, String traineeName) {
        return trainingService.getTrainerTrainings(username, password, from, to, traineeName);
    }

    // 16. Add training
    public Training addTraining(String trainerUsername, String trainerPassword, NewTraining training) {
        return trainingService.addTraining(trainerUsername, trainerPassword, training);
    }

    // 17. Get trainers not assigned to the trainee
    public List<Trainer> getNotAssignedTrainers(String traineeUsername, String traineePassword) {
        return traineeService.getNotAssignedTrainers(traineeUsername, traineePassword);
    }

    // 18. Update trainee's trainers list
    public List<Trainer> updateTraineeTrainers(String username, String password, List<String> trainerUsernames) {
        return traineeService.updateTrainers(username, password, trainerUsernames);
    }
}