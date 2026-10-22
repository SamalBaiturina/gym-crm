package com.epam.gymcrm.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Services, DAOs and persistence. The web layer is excluded from this scan: it is added
 * separately by {@code WebConfig}, so the console demo ({@code Main}) starts without Spring MVC.
 */
@Configuration
@ComponentScan(basePackages = "com.epam.gymcrm",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = "com\\.epam\\.gymcrm\\.infrastructure\\.web\\..*"))
@PropertySource("classpath:application.properties")
public class AppConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
