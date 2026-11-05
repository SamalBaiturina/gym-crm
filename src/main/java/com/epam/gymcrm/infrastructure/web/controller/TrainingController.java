package com.epam.gymcrm.infrastructure.web.controller;

import com.epam.gymcrm.application.service.AddTrainingCommand;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.infrastructure.web.dto.AddTrainingRequest;
import com.epam.gymcrm.domain.model.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.epam.gymcrm.infrastructure.web.security.BasicAuthInterceptor.AUTHENTICATED_USER;

/** Adding trainings. Trainings cannot be changed or deleted through the API. */
@Tag(name = "Trainings", description = "Adding trainings. A training cannot be changed or deleted "
        + "through the API.")
@RestController
@RequestMapping("/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    /** Only the trainee or the trainer of the training may add it. */
    @Operation(summary = "Add a training",
            description = "Only the trainee or the trainer of the training may add it. "
                    + "The training type is the specialization of the trainer.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Training added"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body"),
                    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials"),
                    @ApiResponse(responseCode = "403",
                            description = "Deactivated account, or the caller is not the trainee or the trainer"),
                    @ApiResponse(responseCode = "404", description = "Trainee or trainer does not exist")})
    @PostMapping
    public void add(@Parameter(hidden = true) @RequestAttribute(AUTHENTICATED_USER) AuthenticatedUser user,
                    @Valid @RequestBody AddTrainingRequest request) {
        trainingService.addTraining(new AddTrainingCommand(user.credentials().username(),
                request.traineeUsername(), request.trainerUsername(), request.trainingName(),
                request.trainingDate(), request.trainingDuration()));
    }
}
