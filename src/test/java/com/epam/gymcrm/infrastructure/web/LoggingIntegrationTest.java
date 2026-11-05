package com.epam.gymcrm.infrastructure.web;

import ch.qos.logback.classic.Level;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.infrastructure.web.logging.RestCallLoggingFilter;
import com.epam.gymcrm.infrastructure.web.logging.TransactionIdFilter;
import com.epam.gymcrm.support.LogCapture;
import com.epam.gymcrm.application.service.RegisterTrainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The two filters together with the real controllers and the error handler. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebConfig.class, LoggingIntegrationTest.MocksConfig.class})
class LoggingIntegrationTest {

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
    private TraineeService traineeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(traineeService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext)
                .addFilters(new TransactionIdFilter(), new RestCallLoggingFilter())
                .build();
    }

    @Test
    void everyResponseCarriesATransactionId() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(TransactionIdFilter.HEADER));
    }

    @Test
    void anIncomingTransactionIdIsReturned() throws Exception {
        mockMvc.perform(get("/login").header(TransactionIdFilter.HEADER, "tx-42"))
                .andExpect(header().string(TransactionIdFilter.HEADER, "tx-42"));
    }

    @Test
    void registrationIsLoggedWithoutThePassword() throws Exception {
        when(traineeService.register(any(RegisterTrainee.class))).thenAnswer(invocation -> {
            RegisterTrainee command = invocation.getArgument(0);
            User user = User.of(command.firstName(), command.lastName())
                    .withCredentials("John.Smith", "secret12345");
            return new Trainee(1L, user, command.dateOfBirth(), command.address());
        });

        try (LogCapture logs = new LogCapture(RestCallLoggingFilter.class)) {
            mockMvc.perform(post("/trainees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"firstName\":\"John\",\"lastName\":\"Smith\"}"))
                    .andExpect(status().isOk());

            List<String> lines = logs.messages(Level.INFO);
            assertEquals(1, lines.size());
            assertTrue(lines.get(0).contains("POST /trainees -> 200"));
            assertTrue(lines.get(0).contains("\"password\":\"***\""));
            assertFalse(lines.get(0).contains("secret12345"));
        }
    }

    @Test
    void failedCallIsLoggedAsWarningWithTheErrorMessage() throws Exception {
        try (LogCapture logs = new LogCapture(RestCallLoggingFilter.class)) {
            mockMvc.perform(get("/login")).andExpect(status().isUnauthorized());

            List<String> lines = logs.messages(Level.WARN);
            assertEquals(1, lines.size());
            assertTrue(lines.get(0).contains("GET /login -> 401"));
            assertTrue(lines.get(0).contains("UNAUTHORIZED"));
        }
    }
}
