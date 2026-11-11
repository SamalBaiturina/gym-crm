package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.support.JpaTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTypeDaoImplTest extends JpaTestBase {

    @Test
    void findAllReturnsTheTypesWithIds() {
        List<TrainingType> types = tx.execute(status -> trainingTypeDao.findAll());

        assertEquals(List.of("YOGA", "FITNESS"), types.stream().map(TrainingType::name).toList());
        types.forEach(type -> assertNotNull(type.id()));
    }

    @Test
    void findByIdFindsAnExistingType() {
        TrainingType yoga = tx.execute(status -> trainingTypeDao.findAll()).stream()
                .filter(type -> type.name().equals("YOGA"))
                .findFirst().orElseThrow();

        TrainingType found = tx.execute(status -> trainingTypeDao.findById(yoga.id())).orElseThrow();

        assertEquals("YOGA", found.name());
        assertEquals(yoga.id(), found.id());
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertTrue(tx.execute(status -> trainingTypeDao.findById(999_999L)).isEmpty());
    }
}
