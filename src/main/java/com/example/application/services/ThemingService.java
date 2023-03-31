package com.example.application.services;

import com.example.application.model.ThemeHistory;
import com.example.application.model.ThemeTask;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThemingService {
    private static final Logger logger = LoggerFactory.getLogger(ThemingService.class);

    public static final String INITIAL_CSS = """
            vaadin-text-field {
            }

            vaadin-text-field::part(input-field) {
              background-color: rgba(26, 57, 96, 0.1);
              border-color: rgba(24, 39, 57, 0.94);
              border-style: none;
              border-width: 0;
              border-radius: 4px;
              color: rgba(24, 39, 57, 0.94);
              font-size: 16px;
            }

            vaadin-text-field::part(label) {
              color: rgba(27, 43, 65, 0.69);
              font-size: 14px;
            }

            vaadin-text-field::part(helper-text) {
              color: rgba(27, 43, 65, 0.69);
              font-size: 13px;
            }
            """.trim();

    private final OpenAiService openAiService;

    public ThemingService(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    public void executeTask(ThemeHistory history, String prompt) {
        logger.debug("Execute prompt: {}", prompt);
        ChatCompletionRequest request = createRequest(history, prompt);
        ChatCompletionResult result = openAiService.createChatCompletion(request);

        ChatCompletionChoice chatCompletionChoice = result.getChoices().get(0);
        String reply = chatCompletionChoice.getMessage().getContent();
        String replyCss = extractCss(reply);
        logger.debug("Got reply:\n{}", reply);
        logger.debug("Extracted CSS:\n{}", replyCss);

        CssModifier cssModifier = new CssModifier(history.getCurrentCss());
        cssModifier.updateCss(replyCss);
        String fullCss = cssModifier.getCss();

        ThemeTask task = new ThemeTask();
        task.setPrompt(prompt);
        task.setReply(reply);
        task.setReplyCss(replyCss);
        task.setFullCss(fullCss);
        history.addTask(task);
    }

    private ChatCompletionRequest createRequest(ThemeHistory history, String prompt) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("gpt-3.5-turbo");
        request.setTemperature(0.2);

        // Init with system messages
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(createSystemMessage());
        // Add previous conversation
        history.getTasks().forEach(task -> {
            messages.add(createMessage("user", task.getPrompt()));
            messages.add(createMessage("assistant", task.getReply()));
        });
        // Add next task
        messages.add(createMessage("user", prompt));

        request.setMessages(messages);

        return request;
    }

    private ChatMessage createMessage(String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setRole(role);
        message.setContent(content);

        return message;
    }

    private ChatMessage createSystemMessage() {
        String prompt = """
                Your task is to modify the CSS code for a web component.

                For the first prompt, I'll provide the initial CSS that contains multiple rules.
                Only return the CSS properties that you need to modify for the current task, list each selector separately and do not use selector lists.
                Only modify the existing rules.
                Do not use custom CSS properties.
                For future prompts, keep in mind the initial CSS and the modifications you have made so far.
                If you are asked to do something again, then apply the same modification to the current state of the CSS.
                For example, if you are asked to increase the font size twice, then continue increasing the font size from the previous value.
                Only return the CSS code and do not provide explanations.

                The initial CSS code for the component is:
                ```
                %s
                ```
                """.formatted(INITIAL_CSS);

        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole("system");
        systemMessage.setContent(prompt);

        return systemMessage;
    }

    private String extractCss(String reply) {
        Pattern pattern = Pattern.compile("(?<=```)(.*?)(?=```)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(reply);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}
