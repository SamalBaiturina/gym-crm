package com.epam.gymcrm.infrastructure.web.controller;

import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.domain.model.PasswordChange;
import com.epam.gymcrm.infrastructure.web.dto.ChangePasswordRequest;
import com.epam.gymcrm.infrastructure.web.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.epam.gymcrm.infrastructure.web.security.BasicAuthInterceptor.AUTHENTICATED_USER;

/** Login check and password change. Credentials come from the Authorization header. */
@Tag(name = "Login", description = "Login check and password change for trainees and trainers. "
        + "The credentials are sent in the Authorization: Basic header.")
@RestController
@RequestMapping("/login")
public class LoginController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public LoginController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    /** The credentials are checked by the interceptor, so reaching this method means 200 OK. */
    @Operation(summary = "Check the credentials",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Credentials are valid"),
                    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials"),
                    @ApiResponse(responseCode = "403", description = "The account is deactivated")})
    @GetMapping
    public void login() {
    }

    /** Replaces the password of the caller; the old password is the one from the Authorization header. */
    @Operation(summary = "Change the password",
            description = "The old password is the one from the Authorization header.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password changed"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body"),
                    @ApiResponse(responseCode = "401", description = "Missing or wrong credentials"),
                    @ApiResponse(responseCode = "403", description = "The account is deactivated")})
    @PutMapping
    public void changePassword(
            @Parameter(hidden = true) @RequestAttribute(AUTHENTICATED_USER) AuthenticatedUser user,
            @Valid @RequestBody ChangePasswordRequest request) {
        PasswordChange change = new PasswordChange(user.credentials(), request.newPassword());
        switch (user.role()) {
            case TRAINEE -> traineeService.changePassword(change);
            case TRAINER -> trainerService.changePassword(change);
        }
    }
}
