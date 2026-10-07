package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.infrastructure.persistence.TrainingDaoImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainingDaoImplTest {

    private TrainingDaoImpl dao;

    @BeforeEach
    void setUp() {
        dao = new TrainingDaoImpl();
        dao.setStorage(new HashMap<>());
    }

    @Test
    void save_assignsId_andFindByIdReturnsIt() {
        Training saved = dao.save(new Training());

        assertEquals(1L, saved.getId());
        assertEquals(Optional.of(saved), dao.findById(1L));
    }

    @Test
    void findAll_returnsAllSaved() {
        dao.save(new Training());
        dao.save(new Training());

        assertEquals(2, dao.findAll().size());
    }
}