package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.support.JpaTestBase;
import com.epam.gymcrm.support.TestData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActiveNotAssignedTrainersTest extends JpaTestBase {

    @Test
    void returnsOnlyActiveTrainersThatAreNotAssigned() {
        tx.executeWithoutResult(status -> {
            Trainee john = traineeDao.save(TestData.trainee("John", "Smith"));
            trainerDao.save(TestData.trainer("Anna", "Brown", "YOGA"));
            Trainer bob = trainerDao.save(TestData.trainer("Bob", "White", "FITNESS"));
            Trainer cat = TestData.trainer("Cat", "Green", "YOGA");
            trainerDao.save(new Trainer(null, cat.user().withActive(false), cat.specialization()));
            traineeDao.updateTrainers(john, List.of(bob));
        });

        List<Trainer> result = tx.execute(status -> trainerDao.findNotAssignedTo("John.Smith"));

        assertEquals(1, result.size());
        assertEquals("Anna.Brown", result.get(0).user().username());
    }
}
