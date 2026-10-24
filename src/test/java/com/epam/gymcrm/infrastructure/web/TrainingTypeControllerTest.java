package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TrainingTypeService;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, TrainingTypeControllerTest.MocksConfig.class})
class TrainingTypeControllerTest {

    @Configuration
    static class MocksConfig {
        @Bean
        static BeanFactoryPostProcessor serviceMocks() {
            return ServiceMocks.forWebLayer();
        }
    }

    private static final Credentials JOHN = new Credentials("John.Smith", "secret12345");
    private static final String JOHN_HEADER = "Basic " + Base64.getEncoder()
            .encodeToString("John.Smith:secret12345".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private WebApplicationContext webContext;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private TrainingTypeService trainingTypeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(authenticationService, trainingTypeService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    }

    @Test
    void listReturnsTypesWithIdAndName() throws Exception {
        when(authenticationService.authenticate(JOHN)).thenReturn(Role.TRAINEE);
        when(trainingTypeService.getAll())
                .thenReturn(List.of(new TrainingType(1L, "FITNESS"), new TrainingType(2L, "YOGA")));

        mockMvc.perform(get("/training-types").header(HttpHeaders.AUTHORIZATION, JOHN_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("FITNESS"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("YOGA"));
    }

    @Test
    void listWithoutCredentialsIs401() throws Exception {
        mockMvc.perform(get("/training-types"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(trainingTypeService);
    }
}
