package com.example.application;

import com.example.application.model.ThemeSession;
import com.example.application.services.ThemingService;
import com.theokanning.openai.service.OpenAiService;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.annotation.SessionScope;

import java.time.Duration;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "vaadin-gpt-theming")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @SessionScope
    public ThemeSession themeSession() {
        return new ThemeSession();
    }

    @Bean
    public OpenAiService openAiService() {
        String apiKey = System.getProperty("openai.token");
        return new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    @Bean
    public ThemingService themingService() {
        return new ThemingService(openAiService());
    }
}
