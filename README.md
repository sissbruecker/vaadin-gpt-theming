# vaadin-gtp-theming

This is a proof of concept of using the ChatGTP API to theme a Vaadin component.
It only supports one component, `vaadin-text-field`, and then only a limited set of its parts.

In order to prepare GTP for this task, it is initialized with a prompt that provides the general context, some restrictions regarding the desired output, as well as the initial CSS code that explains the default styles for the component.
For each prompt ("task"), GPT returns a set of changed CSS rules and properties, which are then programmatically merged into the previous CSS.
As is common with chat completions, the message history is kept to provide GPT with context about the previous modifications.
All interactions with GTP as well as the used prompts can be found in `ThemeService.java`.

**Screenshot:**

![Screenshot](/docs/screenshot.png?raw=true "Screenshot")

## Running the application

The project is a standard Maven project, using Spring Boot, Vaadin Hilla, and Lit for the frontend.
To run it from the command line, type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open http://localhost:8080 in your browser.

**You need an OpenAI API token to run this app.**
It needs to be defined as the `openai.token` Spring property.
For example, using Maven:
```
mvn -Dspring-boot.run.arguments="--openai.token=..."
```
