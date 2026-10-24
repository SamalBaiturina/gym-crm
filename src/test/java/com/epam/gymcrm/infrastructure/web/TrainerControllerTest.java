package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingTypeService;
import com.epam.gymcrm.domain.exception.ValidationException;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, TrainerControllerTest.MocksConfig.class})
class TrainerControllerTest {

    @Configuration
    static class MocksConfig {
        @Bean
        static BeanFactoryPostProcessor serviceMocks() {
            return ServiceMocks.forWebLayer();
        }
    }

    @Autowired
    private WebApplicationContext webContext;
    @Autowired
    private TrainerService trainerService;
    @Autowired
    private TrainingTypeService trainingTypeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(trainerService, trainingTypeService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
        when(trainingTypeService.requireById(2L)).thenReturn(new TrainingType(2L, "YOGA"));
        when(trainerService.create(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer trainer = invocation.getArgument(0);
            return trainer.withId(5L).withUser(trainer.user().withCredentials("Anna.Brown", "secret12345"));
        });
    }

    @Test
    void registerReturnsGeneratedCredentialsAndUsesTheSpecialization() throws Exception {
        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Anna\",\"lastName\":\"Brown\",\"specializationId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Anna.Brown"))
                .andExpect(jsonPath("$.password").value("secret12345"));

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerService).create(captor.capture());
        assertEquals("Anna", captor.getValue().user().firstName());
        assertEquals("Brown", captor.getValue().user().lastName());
        assertEquals("YOGA", captor.getValue().specialization().name());
    }

    @Test
    void unknownSpecializationIs400() throws Exception {
        when(trainingTypeService.requireById(99L))
                .thenThrow(new ValidationException("Unknown training type id: 99"));

        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Anna\",\"lastName\":\"Brown\",\"specializationId\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0]").value("Unknown training type id: 99"));

        verifyNoInteractions(trainerService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"firstName\":\"Anna\",\"lastName\":\"Brown\"}",
            "{\"firstName\":\"Anna\",\"specializationId\":2}",
            "{\"firstName\":\" \",\"lastName\":\"Brown\",\"specializationId\":2}"
    })
    void registerRejectsMissingRequiredFields(String body) throws Exception {
        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService, trainingTypeService);
    }
}
