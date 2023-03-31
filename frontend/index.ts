import { html, render } from "lit";
import "./views/editor";

render(
  html` <theme-editor></theme-editor>`,
  document.getElementById("outlet")!
);
