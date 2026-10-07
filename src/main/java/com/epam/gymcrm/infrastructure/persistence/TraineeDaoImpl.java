package com.epam.gymcrm.infrastructure.persistence;
import com.epam.gymcrm.application.port.out.TraineeDao;
import com.epam.gymcrm.domain.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import java.util.Map;


@Repository
public class TraineeDaoImpl extends AbstractMapDao<Trainee> implements TraineeDao {

    public TraineeDaoImpl() {
        super("trainee");
    }

    @Override
    @Autowired
    public void setStorage(@Qualifier("traineeStorage") Map<Long, Trainee> storage) {
        super.setStorage(storage);
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
        log.debug("Deleted trainee with id={}", id);
    }
}