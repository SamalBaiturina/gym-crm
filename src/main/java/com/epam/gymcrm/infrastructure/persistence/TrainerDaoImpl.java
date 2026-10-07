package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.TrainerDao;
import com.epam.gymcrm.domain.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TrainerDaoImpl extends AbstractMapDao<Trainer> implements TrainerDao {

    public TrainerDaoImpl() {
        super("trainer");
    }

    @Override
    @Autowired
    public void setStorage(@Qualifier("trainerStorage") Map<Long, Trainer> storage) {
        super.setStorage(storage);
    }
}