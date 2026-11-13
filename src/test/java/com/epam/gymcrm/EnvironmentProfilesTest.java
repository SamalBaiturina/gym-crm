package com.epam.gymcrm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that every Spring profile points to its own database. Only the configuration files are loaded,
 * so no database is needed.
 */
class EnvironmentProfilesTest {

    @Configuration
    @EnableConfigurationProperties(DataSourceProperties.class)
    static class DataSourcePropertiesOnly {
    }

    /** The environment variables are fixed here, so the result does not depend on the machine. */
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(DataSourcePropertiesOnly.class)
            .withPropertyValues("DB_HOST=localhost", "DB_USER=postgres", "DB_PASSWORD=secret");

    @ParameterizedTest
    @CsvSource({
            "local, jdbc:postgresql://localhost:5432/gymcrm",
            "dev, jdbc:postgresql://localhost:5432/gymcrm_dev",
            "stg, jdbc:postgresql://localhost:5432/gymcrm_stg",
            "prod, jdbc:postgresql://localhost:5432/gymcrm_prod"
    })
    void eachProfileUsesItsOwnDatabase(String profile, String expectedUrl) {
        runner.withPropertyValues("spring.profiles.active=" + profile)
                .run(context -> assertThat(context.getBean(DataSourceProperties.class).getUrl())
                        .isEqualTo(expectedUrl));
    }

    @Test
    void localIsTheDefaultProfile() {
        runner.run(context -> assertThat(context.getBean(DataSourceProperties.class).getUrl())
                .isEqualTo("jdbc:postgresql://localhost:5432/gymcrm"));
    }
}
