// Classe principal da aplicação
class App {
  constructor() {
    // 1) Instanciar módulos (Inicialização)
    this.audioProcessor = new AudioProcessor();
    this.visualizationEngine = new VisualizationEngine("audioCanvas");
    this.uiManager = new UIManager(this);
    this.exportManager = new ExportManager(this.visualizationEngine);

    // ligar audioProcessor às visualizações (importante para “dados reais”)
    this.visualizationEngine.setAudioProcessor(this.audioProcessor);

    this.init();
  }

  init() {
    // escolher a visualização inicial a partir do <select> (ou 'spectrum')
    const initial =
      document.getElementById("visualizationType")?.value || "spectrum";

    // aplica na engine (isto também faz resize da viz)
    this.visualizationEngine.setVisualization(initial);

    // preencher o painel de propriedades já no arranque
    this.uiManager.updatePropertiesPanel();

    // estado inicial
    this.uiManager.updateAudioInfo({ status: "Parado", level: 0 });
    this.uiManager.setButtonStates(false);
    console.log("App inicializada");
  }

  // --- Fluxo: Carregamento de Ficheiro ---
  async loadAudioFile(file) {
    try {
      this.uiManager.setButtonStates(true);
      // podes escolher "element" (simples) ou "buffer" (com decodeAudioData)
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

  // --- Parar Áudio ---
  stopAudio() {
    this.visualizationEngine.stop();
    this.audioProcessor.stop();
    this.uiManager.updateAudioInfo({ status: "Parado", level: 0 });
    this.uiManager.setButtonStates(false);
  }

  // --- Mudança de Visualização ---
  setVisualization(type) {
    const ok = this.visualizationEngine.setVisualization(type);
    if (!ok) {
      this.uiManager.showError(`Visualização "${type}" indisponível`);
      return;
    }
    // Atualizar painel de propriedades (diagramas: “Atualizar Properties Panel”)
    if (typeof this.uiManager.updatePropertiesPanel === "function") {
      this.uiManager.updatePropertiesPanel();
    }
    // Reiniciar animation loop (diagramas)
    this.visualizationEngine.start();
  }

  // --- Export ---
  exportFrame() {
    this.exportManager.exportAsPNG();
  }

  // --- Tratamento centralizado de erros (diagrama “Tratamento de Erros”)
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
