package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.domain.exception.ValidationException;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.TrainingType;
import com.epam.gymcrm.application.service.RegisterTrainer;
import com.epam.gymcrm.domain.model.User;
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

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(trainerService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
        when(trainerService.register(any(RegisterTrainer.class))).thenAnswer(invocation -> {
            RegisterTrainer command = invocation.getArgument(0);
            User user = User.of(command.firstName(), command.lastName())
                    .withCredentials("Anna.Brown", "secret12345");
            return new Trainer(5L, user, new TrainingType(command.specializationId(), "YOGA"));
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

        ArgumentCaptor<RegisterTrainer> captor = ArgumentCaptor.forClass(RegisterTrainer.class);
        verify(trainerService).register(captor.capture());
        assertEquals("Anna", captor.getValue().firstName());
        assertEquals("Brown", captor.getValue().lastName());
        assertEquals(2L, captor.getValue().specializationId());
    }

    @Test
    void unknownSpecializationIs400() throws Exception {
        when(trainerService.register(new RegisterTrainer("Anna", "Brown", 99L)))
                .thenThrow(new ValidationException("Unknown training type id: 99"));

        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Anna\",\"lastName\":\"Brown\",\"specializationId\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0]").value("Unknown training type id: 99"));
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

        verifyNoInteractions(trainerService);
    }
}
