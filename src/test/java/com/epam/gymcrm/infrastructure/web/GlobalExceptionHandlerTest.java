package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.exception.ValidationException;
import com.epam.gymcrm.infrastructure.web.error.GlobalExceptionHandler;
import com.epam.gymcrm.infrastructure.web.security.AccessDeniedException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = GlobalExceptionHandlerTest.TestConfig.class)
class GlobalExceptionHandlerTest {

    /**
     * A small MVC context: only the error handler and a controller that throws.
     * The real controllers are not loaded, so this test does not depend on them.
     */
    @Configuration
    @EnableWebMvc
    static class TestConfig {
        @Bean
        ThrowingController throwingController() {
            return new ThrowingController();
        }

        @Bean
        GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }

    /** A controller whose only job is to throw, so the handler can be checked on its own. */
    @RestController
    @RequestMapping("/test")
    public static class ThrowingController {

        @GetMapping("/validation")
        public void validation() {
            throw new ValidationException("firstName is required");
        }

        @GetMapping("/not-found")
        public void notFound() {
            throw new NotFoundException("Trainee not found: John.Smith");
        }

        @GetMapping("/unauthorized")
        public void unauthorized() {
            throw new AuthenticationException();
        }

        @GetMapping("/boom")
        public void boom() {
            throw new IllegalStateException("secret internal detail");
        }

        @PostMapping("/body")
        public void body(@Valid @RequestBody Payload payload) {
        }

        @GetMapping("/forbidden")
        public void forbidden() {
            throw new AccessDeniedException("Access to another user's data is not allowed");
        }
    }

    public record Payload(@NotBlank String name) {
    }

    @Autowired
    private WebApplicationContext webContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    }

    @Test
    void validationExceptionIs400() throws Exception {
        mockMvc.perform(get("/test/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0]").value("firstName is required"));
    }

    @Test
    void notFoundExceptionIs404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("NOT_FOUND"))
                .andExpect(jsonPath("$.details[0]").value("Trainee not found: John.Smith"));
    }

    @Test
    void authenticationExceptionIs401WithChallengeHeader() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"gym-crm\""))
                .andExpect(jsonPath("$.type").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.details[0]").value("Invalid username or password"));
    }

    @Test
    void unexpectedExceptionIs500AndHidesTheCause() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.details[0]").value("Unexpected error"))
                .andExpect(content().string(not(containsString("secret internal detail"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }

    @Test
    void failedBeanValidationIs400WithFieldName() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0]", startsWith("name: ")));
    }

    @Test
    void malformedJsonIs400() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("MALFORMED_REQUEST"));
    }

    @Test
    void wrongHttpMethodKeepsStatus405() throws Exception {
        mockMvc.perform(post("/test/validation"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.type").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void accessDeniedExceptionIs403() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("FORBIDDEN"))
                .andExpect(jsonPath("$.details[0]").value("Access to another user's data is not allowed"));
    }
}
