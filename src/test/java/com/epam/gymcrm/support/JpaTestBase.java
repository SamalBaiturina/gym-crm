package com.epam.gymcrm.support;

import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.application.port.out.TrainingTypeDao;
import com.epam.gymcrm.infrastructure.persistence.TraineeDaoImpl;
import com.epam.gymcrm.infrastructure.persistence.TrainerDaoImpl;
import com.epam.gymcrm.infrastructure.persistence.TrainingDaoImpl;
import com.epam.gymcrm.infrastructure.persistence.TrainingTypeDaoImpl;
import com.epam.gymcrm.infrastructure.persistence.mapper.TraineeMapper;
import com.epam.gymcrm.infrastructure.persistence.mapper.TrainerMapper;
import com.epam.gymcrm.infrastructure.persistence.mapper.TrainingMapper;
import com.epam.gymcrm.infrastructure.persistence.mapper.TrainingTypeMapper;
import com.epam.gymcrm.infrastructure.persistence.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;

/**
 * Base class for DAO integration tests. {@code @DataJpaTest} starts only the JPA part of the application
 * on in-memory H2; the context is shared by all test classes. The tests commit their own transactions
 * through {@link #tx}, so the transaction that Spring would wrap around each test is switched off
 * and the tables are emptied before every test instead.
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({TraineeDaoImpl.class, TrainerDaoImpl.class, TrainingDaoImpl.class, TrainingTypeDaoImpl.class,
        TraineeMapper.class, TrainerMapper.class, TrainingMapper.class, TrainingTypeMapper.class,
        UserMapper.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public abstract class JpaTestBase {

    @Autowired
    protected TraineeDao traineeDao;
    @Autowired
    protected TrainerDao trainerDao;
    @Autowired
    protected TrainingDao trainingDao;
    @Autowired
    protected TrainingTypeDao trainingTypeDao;

    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private DataSource dataSource;

    protected TransactionTemplate tx;
    protected JdbcTemplate jdbc;

    @BeforeEach
    protected void cleanDatabase() {
        tx = new TransactionTemplate(transactionManager);
        jdbc = new JdbcTemplate(dataSource);
        for (String table : List.of("training", "trainee_trainer", "trainee", "trainer", "users", "training_type")) {
            jdbc.execute("delete from " + table);
        }
        jdbc.execute("insert into training_type (name) values ('YOGA'), ('FITNESS')");
    }

    protected int count(String table) {
        return jdbc.queryForObject("select count(*) from " + table, Integer.class);
    }
}
