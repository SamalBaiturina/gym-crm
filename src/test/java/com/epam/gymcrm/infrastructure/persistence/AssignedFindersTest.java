package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.support.JpaTestBase;
import com.epam.gymcrm.support.TestData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssignedFindersTest extends JpaTestBase {

    private final TraineeDao traineeDao = bean(TraineeDao.class);
    private final TrainerDao trainerDao = bean(TrainerDao.class);

    @Test
    void findsTheTrainersAssignedToATrainee() {
        tx.executeWithoutResult(status -> {
            Trainee john = traineeDao.save(TestData.trainee("John", "Smith"));
            Trainer anna = trainerDao.save(TestData.trainer("Anna", "Brown", "YOGA"));
            trainerDao.save(TestData.trainer("Bob", "White", "FITNESS"));
            traineeDao.updateTrainers(john, List.of(anna));
        });

        List<Trainer> assigned = tx.execute(status -> trainerDao.findAssignedTo("John.Smith"));

        assertEquals(1, assigned.size());
        assertEquals("Anna.Brown", assigned.get(0).user().username());
        assertEquals("YOGA", assigned.get(0).specialization().name());
    }

    @Test
    void findsTheTraineesAssignedToATrainer() {
        tx.executeWithoutResult(status -> {
            Trainee john = traineeDao.save(TestData.trainee("John", "Smith"));
            Trainee mike = traineeDao.save(TestData.trainee("Mike", "Jones"));
            traineeDao.save(TestData.trainee("Kate", "Black"));
            Trainer anna = trainerDao.save(TestData.trainer("Anna", "Brown", "YOGA"));
            traineeDao.updateTrainers(john, List.of(anna));
            traineeDao.updateTrainers(mike, List.of(anna));
        });

        List<Trainee> assigned = tx.execute(status -> traineeDao.findAssignedTo("Anna.Brown"));

        Set<String> usernames = assigned.stream().map(t -> t.user().username()).collect(Collectors.toSet());
        assertEquals(Set.of("John.Smith", "Mike.Jones"), usernames);
    }

    @Test
    void returnsNothingForUsersWithoutAssignments() {
        tx.executeWithoutResult(status -> {
            traineeDao.save(TestData.trainee("John", "Smith"));
            trainerDao.save(TestData.trainer("Anna", "Brown", "YOGA"));
        });

        assertTrue(tx.execute(status -> trainerDao.findAssignedTo("John.Smith")).isEmpty());
        assertTrue(tx.execute(status -> traineeDao.findAssignedTo("Anna.Brown")).isEmpty());
        assertTrue(tx.execute(status -> trainerDao.findAssignedTo("nobody")).isEmpty());
    }
}
