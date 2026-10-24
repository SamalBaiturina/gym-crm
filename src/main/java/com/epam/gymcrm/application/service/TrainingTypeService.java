package com.epam.gymcrm.application.service;

import com.epam.gymcrm.application.port.out.TrainingTypeDao;
import com.epam.gymcrm.domain.exception.ValidationException;
import com.epam.gymcrm.domain.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingTypeService {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeService.class);

    private TrainingTypeDao trainingTypeDao;

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Transactional(readOnly = true)
    public List<TrainingType> getAll() {
        return trainingTypeDao.findAll();
    }

    /** Resolves a type that a request refers to by id. An unknown id is a mistake in the request (400). */
    @Transactional(readOnly = true)
    public TrainingType requireById(Long id) {
        if (id == null) {
            throw new ValidationException("training type id is required");
        }
        return trainingTypeDao.findById(id).orElseThrow(() -> {
            log.warn("Training type id={} not found", id);
            return new ValidationException("Unknown training type id: " + id);
        });
    }
}
