package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.Trainee;

public interface TraineeDao extends BaseDao<Trainee> {
    void deleteById(Long id);
}