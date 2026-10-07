package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.infrastructure.persistence.TrainerDaoImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainerDaoImplTest {

    private TrainerDaoImpl dao;

    @BeforeEach
    void setUp() {
        dao = new TrainerDaoImpl();
        dao.setStorage(new HashMap<>());
    }

    @Test
    void save_assignsId_andFindByIdReturnsIt() {
        Trainer saved = dao.save(new Trainer());

        assertEquals(1L, saved.getId());
        assertEquals(Optional.of(saved), dao.findById(1L));
    }

    @Test
    void findAll_returnsAllSaved() {
        dao.save(new Trainer());
        dao.save(new Trainer());

        assertEquals(2, dao.findAll().size());
    }
}