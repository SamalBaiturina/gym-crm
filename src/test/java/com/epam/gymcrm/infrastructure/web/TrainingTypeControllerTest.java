package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.domain.model.AuthenticatedUser;
import com.epam.gymcrm.domain.model.Credentials;
import com.epam.gymcrm.domain.model.Role;
import com.epam.gymcrm.domain.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainingTypeControllerTest extends ControllerTestBase {

    private static final Credentials JOHN = new Credentials("John.Smith", "secret12345");
    private static final String JOHN_HEADER = "Basic " + Base64.getEncoder()
            .encodeToString("John.Smith:secret12345".getBytes(StandardCharsets.UTF_8));

    @Test
    void listReturnsTypesWithIdAndName() throws Exception {
        when(authenticationService.authenticateAndLoad(JOHN)).thenReturn(new AuthenticatedUser(JOHN, Role.TRAINEE, true));
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
