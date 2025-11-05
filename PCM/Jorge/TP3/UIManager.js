// Gestão de UI
class UIManager {
  constructor(app) {
    this.app = app;
    this.visualizationEngine = app.visualizationEngine;
    this.audioProcessor = app.audioProcessor;

    // Inicializar interface
    this.setupEventListeners();
  }

  updatePropertiesPanel() {
    // TODO: atualizar painel de propriedades
    console.log("Atualizando painel de propriedades...");
  }

  updateAudioInfo(info, isError = false) {
    // TODO: atualizar informações de áudio
    const audioStatus = document.getElementById("audioStatus");
    const audioLevel = document.getElementById("audioLevel");

    if (isError) {
      audioStatus.textContent = `Erro: ${info}`;
      audioStatus.style.color = "#f72585";
    } else {
      audioStatus.textContent = `Áudio: ${info.status || "Ativo"}`;
      audioStatus.style.color = "#e6e6e6";
      audioLevel.textContent = `Nível: ${info.level || 0}%`;
    }
  }

  setButtonStates(playing) {
    // TODO: atualizar estados dos botões
    const startMicBtn = document.getElementById("startMic");
    const stopAudioBtn = document.getElementById("stopAudio");

    startMicBtn.disabled = playing;
    stopAudioBtn.disabled = !playing;
  }

  showError(message) {
    // TODO: mostrar mensagem de erro
    const errorModal = document.getElementById("errorModal");
    const errorMessage = document.getElementById("errorMessage");

    errorMessage.textContent = message;
    errorModal.classList.remove("hidden");

    // Fechar modal ao clicar no X
    document.querySelector(".close").onclick = () => {
      errorModal.classList.add("hidden");
    };

    // Fechar modal ao clicar fora
    window.onclick = (event) => {
      if (event.target === errorModal) {
        errorModal.classList.add("hidden");
      }
    };
  }

  setupEventListeners() {
    // TODO: configurar event listeners
    document.getElementById("startMic").addEventListener("click", () => {
      this.app.startMicrophone();
    });

    document.getElementById("stopAudio").addEventListener("click", () => {
      this.app.stopAudio();
    });

    document.getElementById("audioFile").addEventListener("change", (e) => {
      if (e.target.files.length > 0) {
        this.app.loadAudioFile(e.target.files[0]);
      }
    });

    document
      .getElementById("visualizationType")
      .addEventListener("change", (e) => {
        this.app.setVisualization(e.target.value);
      });

    document.getElementById("exportPNG").addEventListener("click", () => {
      this.app.exportManager.exportAsPNG();
    });

    document.getElementById("exportJPEG").addEventListener("click", () => {
      this.app.exportManager.exportAsJPEG(0.9);
    });
  }

  setupAudioLevels() {
    // TODO: configurar monitorização de níveis de áudio
  }

  createPropertyControl(property, value, min, max, step) {
    // TODO: criar controlo de propriedade
    const container = document.createElement("div");
    container.className = "property-control";

    const label = document.createElement("label");
    label.textContent = property;
    label.htmlFor = `prop-${property}`;

    const input = document.createElement("input");
    input.type = "range";
    input.id = `prop-${property}`;
    input.min = min;
    input.max = max;
    input.step = step;
    input.value = value;

    input.addEventListener("input", (e) => {
      this.visualizationEngine.updateVisualizationProperty(
        property,
        parseFloat(e.target.value)
      );
    });

    container.appendChild(label);
    container.appendChild(input);

    return container;
  }
}
