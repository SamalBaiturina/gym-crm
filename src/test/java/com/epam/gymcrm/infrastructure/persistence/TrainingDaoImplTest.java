package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingDaoImplTest {

    private TrainingDaoImpl dao;

    @BeforeEach
    void setUp() {
        dao = new TrainingDaoImpl();
        dao.setStorage(new HashMap<>());
    }

    @Test
    void saveAndFind() {
        Training saved = dao.save(new Training(null, 1L, 1L, "Morning", new TrainingType("YOGA"),
                LocalDate.of(2026, 9, 1), 60));

        assertEquals(1L, saved.id());
        assertEquals(saved, dao.findById(1L).orElseThrow());
        assertTrue(dao.findById(2L).isEmpty());
        assertEquals(1, dao.findAll().size());
    }
}
