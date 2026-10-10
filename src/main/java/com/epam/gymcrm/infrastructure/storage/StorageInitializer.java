package com.epam.gymcrm.infrastructure.storage;

import com.epam.gymcrm.domain.model.Identifiable;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
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
            default -> {
                // not a storage bean
            }
        }
        return bean;
    }

    private <T extends Identifiable<T>> void fill(Map<Long, T> storage, String type,
                                                  Function<String[], T> parser) {
        for (String[] parts : readRows(type)) {
            T model = parser.apply(parts);
            storage.put(model.id(), model);
        }
        log.info("Loaded {} {} record(s) from {}", storage.size(), type, initFile);
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

    private User parseUser(String[] p) {
        return new User(p[2], p[3], p[4], p[5], Boolean.parseBoolean(p[6]));
    }

    private Trainee parseTrainee(String[] p) {
        return new Trainee(Long.valueOf(p[1]), parseUser(p), LocalDate.parse(p[7]), p[8]);
    }

    private Trainer parseTrainer(String[] p) {
        return new Trainer(Long.valueOf(p[1]), parseUser(p), new TrainingType(p[7]));
    }

    private Training parseTraining(String[] p) {
        return new Training(Long.valueOf(p[1]), Long.valueOf(p[2]), Long.valueOf(p[3]), p[4],
                new TrainingType(p[5]), LocalDate.parse(p[6]), Integer.parseInt(p[7]));
    }
}
