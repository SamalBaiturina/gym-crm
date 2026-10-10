package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerDaoImplTest {

    private TrainerDaoImpl dao;

    @BeforeEach
    void setUp() {
        dao = new TrainerDaoImpl();
        dao.setStorage(new HashMap<>());
    }

    private Trainer trainer(String username) {
        return new Trainer(null, User.of("Anna", "Brown").withCredentials(username, "pwd"),
                new TrainingType("YOGA"));
    }

    @Test
    void saveAndFind() {
        Trainer saved = dao.save(trainer("Anna.Brown"));

        assertEquals(1L, saved.id());
        assertEquals(saved, dao.findById(1L).orElseThrow());
        assertEquals(1, dao.findAll().size());
    }

    @Test
    void findUsernamesStartingWith() {
        dao.save(trainer("Anna.Brown"));
        dao.save(trainer("Max.Miller"));

        assertEquals(1, dao.findUsernamesStartingWith("Anna.Brown").size());
        assertTrue(dao.findUsernamesStartingWith("Nobody").isEmpty());
    }
}
