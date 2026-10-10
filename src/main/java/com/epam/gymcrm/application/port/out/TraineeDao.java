package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.Trainee;

public interface TraineeDao extends BaseDao<Trainee>, UsernameLookup {

    void deleteById(Long id);
}
