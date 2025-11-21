class UIManager {
  constructor(app) {
    this.app = app;

    this.cacheElements();
    this.setupEventListeners();
    this.setupAudioLevelLoop();
  }

  cacheElements() {
    this.statusElement = document.getElementById("audioStatus");
    this.levelElement = document.getElementById("audioLevel");
    this.propsContainer = document.getElementById("properties-container");

    this.buttonMic = document.getElementById("startMic");
    this.buttonStop = document.getElementById("stopAudio");
    this.fileInput = document.getElementById("audioFile");
    this.visualSelect = document.getElementById("visualizationType");

    this.buttonExportPng = document.getElementById("exportPNG");
    this.buttonExportJpg = document.getElementById("exportJPEG");
  }

  updateAudioInfo(info, isError = false) {
    if (!this.statusElement || !this.levelElement) return;

    if (isError) {
      this.statusElement.textContent = `Erro: ${info}`;
      this.statusElement.style.color = "#f72585";
      return;
    }

    this.statusElement.textContent = `Áudio: ${info.status || "Ativo"}`;
    this.statusElement.style.color = "#e6e6e6";

    const level = typeof info.level === "number" ? info.level : 0;
    this.levelElement.textContent = `Nível: ${Math.round(level)}%`;
  }

  setButtonStates(isPlaying) {
    if (this.buttonMic)  this.buttonMic.disabled  = isPlaying;
    if (this.buttonStop) this.buttonStop.disabled = !isPlaying;
  }

  showError(message) {
    const modalElement = document.getElementById("errorModal");
    const messageElement = document.getElementById("errorMessage");
    const closeButton = document.querySelector(".close");

    if (!modalElement || !messageElement || !closeButton) {
      alert(message);
      return;
    }

    messageElement.textContent = message;
    modalElement.classList.remove("hidden");

    closeButton.onclick = () => modalElement.classList.add("hidden");

    const handleWindowClick = event => {
      if (event.target === modalElement) {
        modalElement.classList.add("hidden");
        window.removeEventListener("click", handleWindowClick);
      }
    };

    window.addEventListener("click", handleWindowClick);
  }

  setupEventListeners() {
    this.buttonMic?.addEventListener("click", () =>
      this.app.startMicrophone()
    );

    this.buttonStop?.addEventListener("click", () =>
      this.app.stopAudio()
    );

    this.fileInput?.addEventListener("change", event => {
      const file = event.target.files?.[0];
      if (file) this.app.loadAudioFile(file);
    });

    this.visualSelect?.addEventListener("change", event =>
      this.app.setVisualization(event.target.value)
    );

    this.buttonExportPng?.addEventListener("click", () =>
      this.app.exportManager.exportAsPNG()
    );

    this.buttonExportJpg?.addEventListener("click", () =>
      this.app.exportManager.exportAsJPEG(0.9)
    );
  }

  // Loop contínuo para mostrar o nível de áudio
  setupAudioLevelLoop() {
    const updateLoop = () => {
      const level01 = this.app.audioProcessor?.getLevel?.() || 0;

      this.updateAudioInfo(
        {
          status: "Ativo",
          level: Math.round(level01 * 100),
        },
        false
      );

      requestAnimationFrame(updateLoop);
    };

    requestAnimationFrame(updateLoop);
  }

  updatePropertiesPanel() {
    const engine = this.app.visualizationEngine;
    const properties = engine.getVisualizationProperties();

    if (!this.propsContainer) return;

    this.propsContainer.innerHTML = "";

    for (const [key, value] of Object.entries(properties)) {
      const row = document.createElement("div");
      row.className = "property-control";

      const label = document.createElement("label");
      label.textContent = key;

      // Boolean → checkbox
      if (typeof value === "boolean") {
        const input = document.createElement("input");
        input.type = "checkbox";
        input.checked = value;

        input.oninput = event =>
          engine.updateVisualizationProperty(key, !!event.target.checked);

        row.append(label, input);
        this.propsContainer.append(row);
        continue;
      }

      if (typeof value === "number") {
        const input = document.createElement("input");
        input.type = "range";

        const meta = (() => {
          if (key === "smoothing")  return { min: 0,   max: 0.95, step: 0.01 };
          if (key === "amount")     return { min: 1,   max: 512,  step: 1 };
          if (key === "barSpacing") return { min: 0,   max: 6,    step: 1 };
          if (key === "fadeTrail")  return { min: 0,   max: 0.2,  step: 0.005 };
          if (key.toLowerCase().includes("boost"))
                                    return { min: 0,   max: 5,    step: 0.1 };
          return { min: 0, max: Math.max(value * 2 || 1, 1), step: 0.01 };
        })();

        input.min   = meta.min;
        input.max   = meta.max;
        input.step  = meta.step;
        input.value = value;

        input.oninput = event =>
          engine.updateVisualizationProperty(key, parseFloat(event.target.value));

        row.append(label, input);
        this.propsContainer.append(row);
        continue;
      }

      if (typeof value === "string") {
        const input = document.createElement("input");

        const looksLikeColor =
          value.startsWith("#") ||
          /^rgb\(/i.test(value) ||
          /^hsl\(/i.test(value);

        if (looksLikeColor) {
          input.type = "color";

          try {
            const temp = document.createElement("div");
            temp.style.color = value;
            document.body.appendChild(temp);

            const rgb = getComputedStyle(temp).color;
            document.body.removeChild(temp);

            const match = rgb.match(/\d+/g);
            if (match) {
              const [r, g, b] = match.map(Number);
              input.value =
                "#" +
                ((1 << 24) | (r << 16) | (g << 8) | b)
                  .toString(16)
                  .slice(1);
            }
          } catch {
            input.value = value;
          }
        } else {
          input.type = "text";
          input.value = value;
        }

        input.oninput = event =>
          engine.updateVisualizationProperty(key, event.target.value);

        row.append(label, input);
        this.propsContainer.append(row);
        continue;
      }
    }
  }
}
