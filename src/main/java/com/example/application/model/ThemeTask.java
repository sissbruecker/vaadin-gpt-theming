package com.example.application.model;

import dev.hilla.Nonnull;

public class ThemeTask {
    private String prompt;
    private String reply;
    private String css;

    @Nonnull
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Nonnull
    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    @Nonnull
    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }
}
