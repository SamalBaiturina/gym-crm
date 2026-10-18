package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.TraineeTrainingCriteria;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainerTrainingCriteria;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.support.JpaTestBase;
import com.epam.gymcrm.support.TestData;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks how many queries Hibernate runs, so that extra SELECTs and the N+1 problem
 * don't come back unnoticed. Uses Hibernate statistics, switched on only for these tests.
 */
class QueryCountTest extends JpaTestBase {

    private final TraineeDao traineeDao = bean(TraineeDao.class);
    private final TrainerDao trainerDao = bean(TrainerDao.class);
    private final TrainingDao trainingDao = bean(TrainingDao.class);

    private Statistics statistics;
    private Trainee john;
    private Trainer anna;
    private Trainer bob;

    /** John has three trainings with two different trainers; Mike has one with Anna. */
    @BeforeEach
    void seed() {
        statistics = bean(EntityManagerFactory.class).unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        tx.executeWithoutResult(status -> {
            john = traineeDao.save(TestData.trainee("John", "Smith"));
            Trainee mike = traineeDao.save(TestData.trainee("Mike", "Jones"));
            anna = trainerDao.save(TestData.trainer("Anna", "Brown", "YOGA"));
            bob = trainerDao.save(TestData.trainer("Bob", "White", "FITNESS"));
            trainingDao.save(training(john, anna, "Yoga basics", "YOGA"));
            trainingDao.save(training(john, bob, "Cardio", "FITNESS"));
            trainingDao.save(training(john, anna, "Yoga advanced", "YOGA"));
            trainingDao.save(training(mike, anna, "Yoga for Mike", "YOGA"));
        });
        statistics.clear();
    }

    private Training training(Trainee trainee, Trainer trainer, String name, String type) {
        return new Training(null, trainee, trainer, name, new TrainingType(type), LocalDate.of(2026, 1, 10), 60);
    }

    private long loads(Class<?> entity) {
        return statistics.getEntityStatistics(entity.getName()).getLoadCount();
    }

    // ---------- lists: one query, however many trainings there are ----------

    @Test
    void findForTrainee_runsOneQuery() {
        List<Training> result = tx.execute(status ->
                trainingDao.findForTrainee("John.Smith", TraineeTrainingCriteria.any()));

        assertEquals(3, result.size());
        assertEquals(1, statistics.getPrepareStatementCount());
    }

    @Test
    void findForTrainer_runsOneQuery() {
        List<Training> result = tx.execute(status ->
                trainingDao.findForTrainer("Anna.Brown", TrainerTrainingCriteria.any()));

        assertEquals(3, result.size());
        assertEquals(1, statistics.getPrepareStatementCount());
    }

    @Test
    void findAll_runsOneQuery() {
        List<Training> result = tx.execute(status -> trainingDao.findAll());

        assertEquals(4, result.size());
        assertEquals(1, statistics.getPrepareStatementCount());
    }

    // ---------- writes: no reloading of rows the service has already loaded ----------

    @Test
    void save_doesNotLoadTraineeAndTrainer() {
        // control: loading really is counted, otherwise the zeros below would prove nothing
        tx.execute(status -> traineeDao.findByUsername("John.Smith"));
        assertEquals(1, loads(TraineeEntity.class));
        statistics.clear();

        tx.executeWithoutResult(status -> trainingDao.save(training(john, anna, "New session", "YOGA")));

        assertEquals(0, loads(TraineeEntity.class));
        assertEquals(0, loads(TrainerEntity.class));
        assertEquals(5, count("training"));
    }

    @Test
    void updateTrainers_doesNotLoadTrainers() {
        tx.executeWithoutResult(status -> traineeDao.updateTrainers(john, List.of(anna, bob)));

        assertEquals(0, loads(TrainerEntity.class));
        assertEquals(2, count("trainee_trainer"));
    }
}
