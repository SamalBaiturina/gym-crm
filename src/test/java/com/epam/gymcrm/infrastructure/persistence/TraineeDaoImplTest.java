package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeDaoImplTest {

    private TraineeDaoImpl dao;

    @BeforeEach
    void setUp() {
        dao = new TraineeDaoImpl();
        Map<Long, Trainee> storage = new HashMap<>();
        dao.setStorage(storage);
    }

    private Trainee trainee(Long id, String username) {
        return new Trainee(id, User.of("John", "Smith").withCredentials(username, "pwd"), null, "Astana");
    }

    @Test
    void saveAssignsIncrementingIds() {
        assertEquals(1L, dao.save(trainee(null, "John.Smith")).id());
        assertEquals(2L, dao.save(trainee(null, "John.Smith1")).id());
    }

    @Test
    void saveKeepsExistingIdAndNextIdFollowsMax() {
        dao.save(trainee(10L, "John.Smith"));

        assertEquals(11L, dao.save(trainee(null, "John.Smith1")).id());
    }

    @Test
    void findByIdAndFindAll() {
        Trainee saved = dao.save(trainee(null, "John.Smith"));

        assertEquals(saved, dao.findById(saved.id()).orElseThrow());
        assertTrue(dao.findById(99L).isEmpty());
        assertEquals(1, dao.findAll().size());
    }

    @Test
    void deleteByIdRemovesTrainee() {
        Trainee saved = dao.save(trainee(null, "John.Smith"));

        dao.deleteById(saved.id());

        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    void findUsernamesStartingWithReturnsOnlyMatching() {
        dao.save(trainee(null, "John.Smith"));
        dao.save(trainee(null, "John.Smith1"));
        dao.save(trainee(null, "Aigul.Nurova"));

        List<String> result = dao.findUsernamesStartingWith("John.Smith");

        assertEquals(2, result.size());
        assertTrue(result.contains("John.Smith"));
        assertTrue(result.contains("John.Smith1"));
    }
}
