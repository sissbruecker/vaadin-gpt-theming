package de.sissbruecker.gpttheming.model;

public class ThemeSession {
    private final ThemeHistory history = new ThemeHistory();

    public ThemeHistory getHistory() {
        return history;
    }
}
