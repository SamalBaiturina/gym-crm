package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.domain.model.Trainee;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, TraineeControllerTest.MocksConfig.class})
class TraineeControllerTest {

    /**
     * The mock is registered as a ready object, so Spring does not try to inject the
     * @Autowired setters of the real TraineeService into it.
     */
    @Configuration
    static class MocksConfig {
        @Bean
        static BeanFactoryPostProcessor serviceMocks() {
            return beanFactory ->
                    beanFactory.registerSingleton("traineeService", mock(TraineeService.class));
        }
    }

    @Autowired
    private WebApplicationContext webContext;
    @Autowired
    private TraineeService traineeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(traineeService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
        when(traineeService.create(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee trainee = invocation.getArgument(0);
            User user = trainee.user().withCredentials("John.Smith", "secret12345");
            return trainee.withId(1L).withUser(user);
        });
    }

    @Test
    void registerReturnsGeneratedCredentials() throws Exception {
        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"John","lastName":"Smith",
                                 "dateOfBirth":"2000-01-01","address":"Astana"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.password").value("secret12345"));

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeService).create(captor.capture());
        Trainee sent = captor.getValue();
        assertEquals("John", sent.user().firstName());
        assertEquals("Smith", sent.user().lastName());
        assertEquals(LocalDate.of(2000, 1, 1), sent.dateOfBirth());
        assertEquals("Astana", sent.address());
    }

    @Test
    void registerWorksWithoutOptionalFields() throws Exception {
        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Smith\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeService).create(captor.capture());
        assertNull(captor.getValue().dateOfBirth());
        assertNull(captor.getValue().address());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"lastName\":\"Smith\"}",
            "{\"firstName\":\"John\",\"lastName\":\" \"}"
    })
    void registerRejectsMissingRequiredFields(String body) throws Exception {
        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }
}
