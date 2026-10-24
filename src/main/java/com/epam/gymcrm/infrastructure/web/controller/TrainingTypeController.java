package com.epam.gymcrm.infrastructure.web.controller;

import com.epam.gymcrm.application.service.TrainingTypeService;
import com.epam.gymcrm.infrastructure.web.dto.TrainingTypeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/training-types")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;

    public TrainingTypeController(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    /** All training types. Needs a valid Authorization header like every endpoint except registration. */
    @GetMapping
    public List<TrainingTypeResponse> list() {
        return trainingTypeService.getAll().stream()
                .map(TrainingTypeResponse::from)
                .toList();
    }
}
