package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.infrastructure.web.controller.LoginController;
import com.epam.gymcrm.infrastructure.web.controller.TraineeController;
import com.epam.gymcrm.infrastructure.web.controller.TrainerController;
import com.epam.gymcrm.infrastructure.web.controller.TrainingController;
import com.epam.gymcrm.infrastructure.web.controller.TrainingTypeController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Guards the API documentation: no controller or endpoint may be left without a description. */
class ApiDocumentationTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            TraineeController.class, TrainerController.class, LoginController.class,
            TrainingController.class, TrainingTypeController.class);

    private static List<Method> endpoints() {
        return CONTROLLERS.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods()))
                .filter(method -> AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class))
                .toList();
    }

    @Test
    void everyControllerHasATag() {
        for (Class<?> controller : CONTROLLERS) {
            Tag tag = controller.getAnnotation(Tag.class);
            assertNotNull(tag, controller.getSimpleName() + " has no @Tag");
            assertFalse(tag.name().isBlank());
        }
    }

    @Test
    void everyEndpointHasAnOperationWithASummary() {
        for (Method endpoint : endpoints()) {
            Operation operation = endpoint.getAnnotation(Operation.class);
            String where = endpoint.getDeclaringClass().getSimpleName() + "." + endpoint.getName();
            assertNotNull(operation, where + " has no @Operation");
            assertFalse(operation.summary().isBlank(), where + " has an empty summary");
        }
    }

    @Test
    void theApiHasSeventeenEndpoints() {
        assertEquals(17, endpoints().size());
    }
}
