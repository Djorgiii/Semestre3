// Classe principal da aplicação
class App {
  constructor() {
    this.audioProcessor     = new AudioProcessor();
    this.visualizationEngine = new VisualizationEngine("audioCanvas");
    this.uiManager          = new UIManager(this);
    this.exportManager      = new ExportManager(this.visualizationEngine);

    // ligar audioProcessor às visualizações
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

  // Microfone
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

  // Carregamento de ficheiro
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

  // Parar áudio
  stopAudio() {
    this.visualizationEngine.stop();
    this.audioProcessor.stop();
    this.uiManager.updateAudioInfo({ status: "Parado", level: 0 });
    this.uiManager.setButtonStates(false);
  }

  // Mudança de visualização
  setVisualization(type) {
    const ok = this.visualizationEngine.setVisualization(type);
    if (!ok) {
      this.uiManager.showError(`Visualização "${type}" indisponível`);
      return;
    }
    this.uiManager.updatePropertiesPanel?.();
    this.visualizationEngine.start();
  }

  // Export
  exportFrame() {
    this.exportManager.exportAsPNG();
  }

  // Tratamento de erros
  handleError(origin, error) {
    console.error(`[${origin}]`, error);
    this.uiManager.showError(`${origin}: ${error?.message || error}`);
    if (origin === "AudioProcessor") this.stopAudio();
  }
}

// Inicialização
document.addEventListener("DOMContentLoaded", () => {
  window.app = new App();
});
