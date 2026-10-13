package com.epam.gymcrm.infrastructure.persistence;

import com.epam.gymcrm.application.port.out.TrainingDao;
import com.epam.gymcrm.domain.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TrainingDaoImpl extends AbstractMapDao<Training> implements TrainingDao {

    public TrainingDaoImpl() {
        super("training");
    }

    @Override
    @Autowired
    public void setStorage(@Qualifier("trainingStorage") Map<Long, Training> storage) {
        super.setStorage(storage);
    }
}