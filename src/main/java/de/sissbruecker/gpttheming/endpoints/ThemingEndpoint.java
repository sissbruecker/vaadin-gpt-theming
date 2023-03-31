package de.sissbruecker.gpttheming.endpoints;

import de.sissbruecker.gpttheming.model.ThemeHistory;
import de.sissbruecker.gpttheming.model.ThemeSession;
import de.sissbruecker.gpttheming.services.ThemingService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import dev.hilla.Nonnull;

@Endpoint
@AnonymousAllowed
public class ThemingEndpoint {

    private final ThemeSession themeSession;
    private final ThemingService themingService;

    public ThemingEndpoint(ThemeSession themeSession, ThemingService themingService) {
        this.themeSession = themeSession;
        this.themingService = themingService;
    }

    @Nonnull
    public ThemeHistory getHistory() {
        return themeSession.getHistory();
    }

    @Nonnull
    public ThemeHistory executeTask(@Nonnull String prompt) {
        ThemeHistory history = themeSession.getHistory();
        themingService.executeTask(history, prompt);

        return history;
    }

    @Nonnull
    public ThemeHistory cut(@Nonnull int taskIndex) {
        ThemeHistory history = themeSession.getHistory();
        history.cut(taskIndex);

        return history;
    }
}