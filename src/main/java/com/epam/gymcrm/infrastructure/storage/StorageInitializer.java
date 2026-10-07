package com.epam.gymcrm.infrastructure.storage;

import com.epam.gymcrm.domain.model.BaseEntity;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class StorageInitializer implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private String initFile;

    @Value("${storage.init.file}")
    public void setInitFile(String initFile) {
        this.initFile = initFile;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        switch (beanName) {
            case "traineeStorage" -> fill((Map<Long, Trainee>) bean, "TRAINEE", this::parseTrainee);
            case "trainerStorage" -> fill((Map<Long, Trainer>) bean, "TRAINER", this::parseTrainer);
            case "trainingStorage" -> fill((Map<Long, Training>) bean, "TRAINING", this::parseTraining);
            default -> { }
        }
        return bean;
    }

    private <T extends BaseEntity> void fill(Map<Long, T> storage, String type,
                                             Function<String[], T> parser) {
        List<String[]> rows = readRows(type);
        rows.stream().map(parser).forEach(e -> storage.put(e.getId(), e));
        log.info("Initialized {} storage with {} records", type, rows.size());
    }

    private List<String[]> readRows(String type) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(initFile).getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(line -> line.split(","))
                    .filter(parts -> parts[0].equals(type))
                    .toList();
        } catch (IOException e) {
            log.error("Cannot read init file: {}", initFile, e);
            throw new IllegalStateException("Cannot read init file: " + initFile, e);
        }
    }

    private Trainee parseTrainee(String[] p) {
        Trainee trainee = new Trainee();
        fillUser(trainee, p);
        trainee.setDateOfBirth(LocalDate.parse(p[7]));
        trainee.setAddress(p[8]);
        return trainee;
    }

    private Trainer parseTrainer(String[] p) {
        Trainer trainer = new Trainer();
        fillUser(trainer, p);
        trainer.setSpecialization(new TrainingType(p[7]));
        return trainer;
    }

    private Training parseTraining(String[] p) {
        Training training = new Training();
        training.setId(Long.parseLong(p[1]));
        training.setTraineeId(Long.parseLong(p[2]));
        training.setTrainerId(Long.parseLong(p[3]));
        training.setTrainingName(p[4]);
        training.setTrainingType(new TrainingType(p[5]));
        training.setTrainingDate(LocalDate.parse(p[6]));
        training.setTrainingDuration(Integer.parseInt(p[7]));
        return training;
    }

    private void fillUser(User user, String[] p) {
        user.setId(Long.parseLong(p[1]));
        user.setFirstName(p[2]);
        user.setLastName(p[3]);
        user.setUsername(p[4]);
        user.setPassword(p[5]);
        user.setActive(Boolean.parseBoolean(p[6]));
    }
}