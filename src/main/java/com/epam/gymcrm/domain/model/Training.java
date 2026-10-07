package com.epam.gymcrm.domain.model;

import java.time.LocalDate;

public class Training extends BaseEntity {
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private int trainingDuration; // в минутах

    public void setTraineeId(Long traineeId) {
        this.traineeId = traineeId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public void setTrainingType(TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public void setTrainingDuration(int trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public Long getTraineeId() {
        return traineeId;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public int getTrainingDuration() {
        return trainingDuration;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    @Override
    public String toString() {
        return "Training{" +
                "id=" + getId() +
                ", traineeId=" + traineeId +
                ", trainerId=" + trainerId +
                ", trainingName='" + trainingName + '\'' +
                ", trainingType=" + trainingType +
                ", trainingDate=" + trainingDate +
                ", trainingDuration=" + trainingDuration +
                '}';
    }
}