package de.sissbruecker.gpttheming.model;

import dev.hilla.Nonnull;

public class ThemeTask {
    private String prompt;
    private String reply;
    private String replyCss;
    private String fullCss;

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
    public String getReplyCss() {
        return replyCss;
    }

    public void setReplyCss(String replyCss) {
        this.replyCss = replyCss;
    }

    @Nonnull
    public String getFullCss() {
        return fullCss;
    }

    public void setFullCss(String fullCss) {
        this.fullCss = fullCss;
    }
}
