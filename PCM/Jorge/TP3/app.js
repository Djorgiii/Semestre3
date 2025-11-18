// Classe principal da aplicação
class App {
  constructor() {
    this.audioProcessor     = new AudioProcessor();
    this.visualizationEngine = new VisualizationEngine("audioCanvas");
    this.uiManager          = new UIManager(this);
    this.exportManager      = new ExportManager(this.visualizationEngine);

    this.visualizationEngine.setAudioProcessor(this.audioProcessor);

    this.init();
  }

  init() {
    const initial =
      document.getElementById("visualizationType")?.value || "spectrum";

    this.visualizationEngine.setVisualization(initial);
    this.uiManager.updatePropertiesPanel();
    this.uiManager.updateAudioInfo({ status: "Parado", level: 0 });
    this.uiManager.setButtonStates(false);

    console.log("App inicializada");
  }

  async startMicrophone() {
    try {
      this.uiManager.setButtonStates(true);
      await this.audioProcessor.startMicrophone();
      this.visualizationEngine.start();
      this.uiManager.updateAudioInfo({ status: "Microfone", level: 0 });
    } catch (e) {
      this.handleError("AudioProcessor", e);
      this.uiManager.setButtonStates(false);
    }
  }

  async loadAudioFile(file) {
    try {
      this.uiManager.setButtonStates(true);
      await this.audioProcessor.loadAudioFile(file, "element");
      this.visualizationEngine.start();
      this.uiManager.updateAudioInfo({
        status: `Ficheiro: ${file.name}`,
        level: 0,
      });
    } catch (e) {
      this.uiManager.showError("Erro no ficheiro: " + (e?.message || e));
      this.uiManager.setButtonStates(false);
    }
  }

  stopAudio() {
    this.visualizationEngine.stop();
    this.audioProcessor.stop();
    this.uiManager.updateAudioInfo({ status: "Parado", level: 0 });
    this.uiManager.setButtonStates(false);
  }

  setVisualization(type) {
    const ok = this.visualizationEngine.setVisualization(type);
    if (!ok) {
      this.uiManager.showError(`Visualização "${type}" indisponível`);
      return;
    }
    this.uiManager.updatePropertiesPanel?.();
    this.visualizationEngine.start();
  }

  exportFrame() {
    this.exportManager.exportAsPNG();
  }

  handleError(origin, error) {
    console.error(`[${origin}]`, error);
    this.uiManager.showError(`${origin}: ${error?.message || error}`);
    if (origin === "AudioProcessor") this.stopAudio();
  }
}

document.addEventListener("DOMContentLoaded", () => {
  window.app = new App();
});
