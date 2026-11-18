package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingService;
import com.epam.gymcrm.application.service.TrainingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for the controller tests. {@code @WebMvcTest} starts only the web layer: the controllers,
 * the error handler, the authentication interceptor and the two logging filters. The services are
 * replaced with mocks, which Spring resets after every test. All subclasses share one context.
 */
@WebMvcTest
abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected AuthenticationService authenticationService;
    @MockitoBean
    protected TraineeService traineeService;
    @MockitoBean
    protected TrainerService trainerService;
    @MockitoBean
    protected TrainingService trainingService;
    @MockitoBean
    protected TrainingTypeService trainingTypeService;
}
