package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.infrastructure.persistence.TraineeDaoImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeDaoImplTest {

    private TraineeDaoImpl dao;

    @BeforeEach
    void setUp() {
        dao = new TraineeDaoImpl();
        dao.setStorage(new HashMap<>());
    }

    @Test
    void save_assignsIncrementingIds() {
        Trainee first = dao.save(new Trainee());
        Trainee second = dao.save(new Trainee());

        assertEquals(1L, first.getId());
        assertEquals(2L, second.getId());
    }

    @Test
    void findById_returnsEmpty_whenMissing() {
        assertTrue(dao.findById(42L).isEmpty());
    }

    @Test
    void deleteById_removesTrainee() {
        Trainee saved = dao.save(new Trainee());

        dao.deleteById(saved.getId());

        assertTrue(dao.findById(saved.getId()).isEmpty());
    }
}