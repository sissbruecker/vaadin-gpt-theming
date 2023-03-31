package de.sissbruecker.gpttheming;

import de.sissbruecker.gpttheming.model.ThemeSession;
import de.sissbruecker.gpttheming.services.ThemingService;
import com.theokanning.openai.service.OpenAiService;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${openai.token}")
    public String openaiToken;

    @Bean
    public OpenAiService openAiService() {
        System.out.println("Found token: " + openaiToken);
        return new OpenAiService(openaiToken, Duration.ofSeconds(60));
    }

    @Bean
    public ThemingService themingService() {
        return new ThemingService(openAiService());
    }
}
