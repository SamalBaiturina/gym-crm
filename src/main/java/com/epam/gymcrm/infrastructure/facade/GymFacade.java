package com.epam.gymcrm.infrastructure.facade;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {

    private static final Logger log = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    // Trainee
    public Trainee createTrainee(Trainee trainee) {
        return traineeService.create(trainee);
    }

    public Trainee updateTrainee(Trainee trainee) {
        return traineeService.update(trainee);
    }

    public void deleteTrainee(Long id) {
        traineeService.delete(id);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeService.findById(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.findAll();
    }

    // Trainer
    public Trainer createTrainer(Trainer trainer) {
        return trainerService.create(trainer);
    }

    public Trainer updateTrainer(Trainer trainer) {
        return trainerService.update(trainer);
    }

    public Optional<Trainer> getTrainer(Long id) {
        return trainerService.findById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.findAll();
    }

    // Training
    public Training createTraining(Training training) {
        return trainingService.create(training);
    }

    public Optional<Training> getTraining(Long id) {
        return trainingService.findById(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.findAll();
    }
}