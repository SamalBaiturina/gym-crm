package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.Trainer;

public interface TrainerDao extends BaseDao<Trainer>, UsernameLookup {
}
