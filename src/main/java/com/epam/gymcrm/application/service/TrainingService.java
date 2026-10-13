package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    public Training create(Training training) {
        if (traineeDao.findById(training.traineeId()).isEmpty()) {
            log.warn("Trainee not found, id={}", training.traineeId());
            throw new NoSuchElementException("Trainee not found: " + training.traineeId());
        }
        if (trainerDao.findById(training.trainerId()).isEmpty()) {
            log.warn("Trainer not found, id={}", training.trainerId());
            throw new NoSuchElementException("Trainer not found: " + training.trainerId());
        }
        Training saved = trainingDao.save(training);
        log.info("Created training id={}", saved.id());
        return saved;
    }

    public Optional<Training> findById(Long id) {
        return trainingDao.findById(id);
    }

    public List<Training> findAll() {
        return trainingDao.findAll();
    }
}
