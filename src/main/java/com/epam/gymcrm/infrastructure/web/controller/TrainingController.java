package com.epam.gymcrm.infrastructure.web.controller;

import com.epam.gymcrm.application.service.AddTrainingCommand;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.infrastructure.web.dto.AddTrainingRequest;
import com.epam.gymcrm.infrastructure.web.security.AccessDeniedException;
import com.epam.gymcrm.infrastructure.web.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.epam.gymcrm.infrastructure.web.security.BasicAuthInterceptor.AUTHENTICATED_USER;

/** Adding trainings. Trainings cannot be changed or deleted through the API. */
@RestController
@RequestMapping("/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    /** Only the trainee or the trainer of the training may add it. */
    @PostMapping
    public void add(@RequestAttribute(AUTHENTICATED_USER) AuthenticatedUser user,
                    @Valid @RequestBody AddTrainingRequest request) {
        String caller = user.credentials().username();
        if (!caller.equals(request.traineeUsername()) && !caller.equals(request.trainerUsername())) {
            throw new AccessDeniedException("A training can be added only by its trainee or its trainer");
        }
        trainingService.addTraining(new AddTrainingCommand(request.traineeUsername(),
                request.trainerUsername(), request.trainingName(), request.trainingDate(),
                request.trainingDuration()));
    }
}
