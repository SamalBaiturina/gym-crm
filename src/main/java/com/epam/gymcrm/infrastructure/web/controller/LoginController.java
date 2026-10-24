package com.epam.gymcrm.infrastructure.web.controller;

import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.domain.model.PasswordChange;
import com.epam.gymcrm.infrastructure.web.dto.ChangePasswordRequest;
import com.epam.gymcrm.infrastructure.web.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.epam.gymcrm.infrastructure.web.security.BasicAuthInterceptor.AUTHENTICATED_USER;

/** Login check and password change. Credentials come from the Authorization header. */
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
    @GetMapping
    public void login() {
    }

    /** Replaces the password of the caller; the old password is the one from the Authorization header. */
    @PutMapping
    public void changePassword(@RequestAttribute(AUTHENTICATED_USER) AuthenticatedUser user,
                               @Valid @RequestBody ChangePasswordRequest request) {
        PasswordChange change = new PasswordChange(user.credentials(), request.newPassword());
        switch (user.role()) {
            case TRAINEE -> traineeService.changePassword(change);
            case TRAINER -> trainerService.changePassword(change);
        }
    }
}
