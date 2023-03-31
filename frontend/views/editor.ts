import { css, html, LitElement, PropertyValues } from "lit";
import { customElement, property, query, state } from "lit/decorators.js";
import hljs from "highlight.js/lib/core";
import cssLanguage from "highlight.js/lib/languages/css";
// @ts-ignore
import highlightStyles from "highlight.js/styles/github.css?inline";
import { ThemingEndpoint } from "Frontend/generated/endpoints";
import ThemeHistory from "Frontend/generated/com/example/application/model/ThemeHistory";
import { TextFieldChangeEvent } from "@vaadin/text-field";
import ThemeTask from "Frontend/generated/com/example/application/model/ThemeTask";
import "@vaadin/text-field";
import "@vaadin/text-area";
import "@vaadin/progress-bar";

console.log(highlightStyles);

hljs.registerLanguage("css", cssLanguage);
hljs.configure({ languages: ["css"] });

@customElement("theme-editor")
export class Editor extends LitElement {
  static get styles() {
    return css`
      :host {
        display: block;
        max-width: 1280px;
        height: 100%;
        margin-left: auto;
        margin-right: auto;
        background: white;
      }

      .editor {
        height: 100%;
        display: flex;
      }

      .output {
        width: 50%;
        display: flex;
        flex-direction: column;
        border-right: solid 1px var(--lumo-contrast-20pct);
      }

      .preview {
        height: 50%;
      }

      .styles {
        height: 50%;
        padding: var(--lumo-space-m);
        overflow: auto;
        border-top: solid 1px var(--lumo-contrast-20pct);
        background: var(--lumo-contrast-5pct);
        box-sizing: border-box;
        font-size: 13px;
      }

      .history {
        width: 50%;
        display: flex;
        flex-direction: column;
        background: var(--lumo-contrast-5pct);
      }
      
      .history > .prompt {
        flex: 0 0 auto;
        padding: var(--lumo-space-m);
        border-bottom: solid 1px var(--lumo-contrast-20pct);
      }

      .tasks {
        flex: 1 1 0;
        overflow: auto;
        padding: var(--lumo-space-m);
      }

      .history vaadin-text-area {
        width: 100%;
        background: white;
        padding: var(--lumo-space-m);
        border-radius: var(--lumo-border-radius-l);
        box-sizing: border-box;
      }

      .theme-task {
        background: white;
        padding: var(--lumo-space-m);
        margin-bottom: var(--lumo-space-m);
        border-radius: var(--lumo-border-radius-l);
        cursor: pointer;
      }

      .theme-task vaadin-progress-bar {
        width: 100%;
        margin-top: var(--lumo-space-s);
      }

      .theme-task theme-code-view {
        margin-top: var(--lumo-space-s);
        font-size: 12px;
        overflow: auto;
      }

      .theme-task.selected {
        outline: solid 2px var(--lumo-primary-color);
      }

      .theme-task.ignored {
        opacity: 0.5;
      }

      .theme-task.pending {
        opacity: 0.8;
      }
    `;
  }

  @state()
  private history?: ThemeHistory;
  @state()
  private prompt: string = "";
  @state()
  private loading: boolean = false;
  @state()
  private previewCss: string = "";
  @state()
  private selectedTask: number = -1;
  @state()
  private pendingTask: ThemeTask | null = null;
  @query("#tasks")
  private tasksElement?: HTMLDivElement;

  get currentCss(): string {
    if (!this.history) {
      return "";
    }

    const currentTask = this.history.tasks[this.selectedTask];

    return currentTask ? currentTask.fullCss : this.history.initialCss;
  }

  protected async firstUpdated() {
    await this.loadHistory();
    this.updatePreviewStyles();
  }

  protected render(): unknown {
    const tasks =
      this.history &&
      this.history.tasks.map((task, index) => this.renderTask(task, index));

    return html`
      <div class="editor">
        <div class="output">
          <theme-preview
            class="preview"
            .css="${this.previewCss}"
          ></theme-preview>
          <div class="styles">
            <theme-code-view .css="${this.previewCss}"></theme-code-view>
          </div>
        </div>
        <div class="history">
          <div class="prompt">
            <vaadin-text-area
              label="Prompt"
              .value="${this.prompt}"
              ?readonly="${this.loading}"
              @input="${this.handlePromptInput}"
              @keydown="${this.handlePromptKeyDown}"
            ></vaadin-text-area>
          </div>
          <div id="tasks" class="tasks">
            ${tasks}
            ${this.history &&
            this.pendingTask &&
            this.renderTask(this.pendingTask, this.history.tasks.length)}
          </div>
        </div>
      </div>
    `;
  }

  private renderTask(task: ThemeTask, index: number) {
    const isSelected = index === this.selectedTask;
    const isIgnored = index > this.selectedTask;
    const isPending = task === this.pendingTask;
    const classes = `theme-task ${isSelected ? "selected" : ""} ${
      isIgnored ? "ignored" : ""
    } ${isPending ? "pending" : ""}`;
    return html`
      <div class="${classes}" @click="${() => this.selectTask(index)}">
        <div class="prompt">${index + 1}. ${task.prompt}</div>
        ${isPending
          ? html` <vaadin-progress-bar indeterminate></vaadin-progress-bar> `
          : null}
        ${task.replyCss
          ? html` <theme-code-view .css="${task.replyCss}"></theme-code-view>`
          : null}
      </div>
    `;
  }

  private handlePromptInput(e: TextFieldChangeEvent) {
    this.prompt = e.target.value;
  }

  private handlePromptKeyDown(e: KeyboardEvent) {
    if (e.key === "Enter") {
      e.preventDefault();
      this.executeTask();
    }
  }

  private selectTask(index: number) {
    this.selectedTask = index;
    this.updatePreviewStyles();
  }

  private async loadHistory() {
    this.history = await ThemingEndpoint.getHistory();
    this.selectedTask = this.history.tasks.length - 1;
    this.scrollToLastTask();
  }

  private async executeTask() {
    const prompt = this.prompt.trim();
    if (!this.history || this.loading || !prompt) {
      return;
    }

    this.loading = true;

    const isLastTask = this.selectedTask === this.history.tasks.length - 1;
    if (!isLastTask) {
      this.history = await ThemingEndpoint.cut(this.selectedTask + 1);
      this.selectedTask = this.history.tasks.length - 1;
    }
    this.pendingTask = {
      prompt,
      reply: "",
      replyCss: "",
      fullCss: "",
    };
    this.prompt = "";
    this.scrollToLastTask();
    this.history = await ThemingEndpoint.executeTask(prompt);
    this.pendingTask = null;
    this.selectedTask = this.history.tasks.length - 1;
    this.loading = false;
    this.updatePreviewStyles();
    this.scrollToLastTask();
  }

  private updatePreviewStyles() {
    this.previewCss = this.currentCss;
  }

  private scrollToLastTask() {
    setTimeout(() => {
      const lastChild = this.tasksElement?.lastElementChild;
      if (lastChild) {
        lastChild.scrollIntoView(true);
      }
    }, 100);
  }
}

@customElement("theme-preview")
class Preview extends LitElement {
  static get styles() {
    return css`
      :host {
        display: flex;
        align-items: center;
        justify-content: center;
      }
    `;
  }

  @property({})
  public css: string = "";
  private previewStylesheet: CSSStyleSheet;

  constructor() {
    super();
    this.previewStylesheet = new CSSStyleSheet();
  }

  protected async firstUpdated() {
    this.shadowRoot!.adoptedStyleSheets = [
      ...this.shadowRoot!.adoptedStyleSheets,
      this.previewStylesheet,
    ];
  }

  protected update(changedProperties: PropertyValues) {
    super.update(changedProperties);

    if (changedProperties.has("css")) {
      this.previewStylesheet.replaceSync(this.css);
    }
  }

  protected render(): unknown {
    return html`
      <vaadin-text-field
        label="Some label"
        helper-text="Some helper text"
        value="Some text"
      ></vaadin-text-field>
    `;
  }
}

@customElement("theme-code-view")
class CodeView extends LitElement {
  static get styles() {
    return [
      highlightStyles,
      css`
        :host {
          display: block;
          font-size: inherit;
        }

        #code {
          white-space: pre;
          font-family: monospace;
          background: none !important;
        }
      `,
    ];
  }

  @property({})
  public css: string = "";
  @query("#code")
  private codeElement?: HTMLDivElement;

  protected update(changedProperties: PropertyValues) {
    super.update(changedProperties);

    if (changedProperties.has("css") && this.codeElement) {
      this.codeElement.textContent = this.css;
      hljs.highlightElement(this.codeElement);
    }
  }

  render() {
    return html` <div id="code" class="css"></div> `;
  }
}
