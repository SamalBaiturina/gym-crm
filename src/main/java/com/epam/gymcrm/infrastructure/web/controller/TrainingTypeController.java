package com.epam.gymcrm.infrastructure.web.controller;

import com.epam.gymcrm.application.service.TrainingTypeService;
import com.epam.gymcrm.infrastructure.web.dto.TrainingTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only list of training types. */
@Tag(name = "Training types", description = "Read-only list of training types.")
@RestController
@RequestMapping("/training-types")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;

    public TrainingTypeController(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    /** All training types. Needs a valid Authorization header like every endpoint except registration. */
    @Operation(summary = "List all training types",
            description = "The ids are used as the specialization when a trainer is registered.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "All training types"),
                    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials")})
    @GetMapping
    public List<TrainingTypeResponse> list() {
        return trainingTypeService.getAll().stream()
                .map(TrainingTypeResponse::from)
                .toList();
    }
}
