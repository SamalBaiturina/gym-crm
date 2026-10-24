package com.epam.gymcrm.infrastructure.web;

import com.epam.gymcrm.application.service.AuthenticationService;
import com.epam.gymcrm.application.service.TraineeService;
import com.epam.gymcrm.application.service.TrainerService;
import com.epam.gymcrm.application.service.TrainingTypeService;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

import static org.mockito.Mockito.mock;

/**
 * Registers mocks as ready objects, so Spring does not try to inject the @Autowired setters
 * of the real services into them.
 */
final class ServiceMocks {

    private ServiceMocks() {
    }

    /** Every service the web layer needs. Add a service here when a new controller starts using it. */
    static BeanFactoryPostProcessor forWebLayer() {
        return of(AuthenticationService.class, TraineeService.class, TrainerService.class,
                TrainingTypeService.class);
    }

    static BeanFactoryPostProcessor of(Class<?>... types) {
        return beanFactory -> {
            for (Class<?> type : types) {
                String simpleName = type.getSimpleName();
                String beanName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
                beanFactory.registerSingleton(beanName, mock(type));
            }
        };
    }
}
