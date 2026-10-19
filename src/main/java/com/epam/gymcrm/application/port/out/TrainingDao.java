package com.epam.gymcrm.application.port.out;

import com.epam.gymcrm.domain.model.TraineeTrainingCriteria;
import com.epam.gymcrm.domain.model.TrainerTrainingCriteria;
import com.epam.gymcrm.domain.model.Training;

import java.util.List;

public interface TrainingDao extends BaseDao<Training> {

    /** Trainings of the trainee with the given username, narrowed by the optional criteria. */
    List<Training> findForTrainee(String traineeUsername, TraineeTrainingCriteria criteria);

    /** Trainings of the trainer with the given username, narrowed by the optional criteria. */
    List<Training> findForTrainer(String trainerUsername, TrainerTrainingCriteria criteria);
}
