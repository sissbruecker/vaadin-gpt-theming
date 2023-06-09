package de.sissbruecker.gpttheming.model;

import de.sissbruecker.gpttheming.services.ThemingService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.hilla.Nonnull;

import java.util.ArrayList;
import java.util.List;

public class ThemeHistory {
    private List<ThemeTask> tasks = new ArrayList<>();
    private final String initialCss;

    public ThemeHistory() {
        this.initialCss = ThemingService.INITIAL_CSS;
    }

    public void addTask(ThemeTask task) {
        tasks.add(task);
    }

    public void cut(int taskIndex) {
        tasks = tasks.subList(0, taskIndex);
    }

    @Nonnull
    public List<@Nonnull ThemeTask> getTasks() {
        return tasks;
    }

    @Nonnull
    public String getInitialCss() {
        return initialCss;
    }

    @JsonIgnore
    public String getCurrentCss() {
        if (tasks.size() == 0) {
            return initialCss;
        }

        return tasks.get(tasks.size() - 1).getFullCss();
    }
}
