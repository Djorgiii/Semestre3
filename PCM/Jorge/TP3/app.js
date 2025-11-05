// Classe principal da aplicação
class App {
  constructor() {
    this.audioProcessor = new AudioProcessor();
    this.visualizationEngine = new VisualizationEngine("audioCanvas");
    this.uiManager = new UIManager(this);
    this.exportManager = new ExportManager(this.visualizationEngine);

    // Inicialização
    this.init();
  }

  init() {
    // TODO: inicializar a aplicação
    console.log("App inicializada");
  }

  startMicrophone() {
    // TODO: iniciar captura do microfone
    console.log("Iniciando microfone...");
  }

  loadAudioFile(file) {
    // TODO: carregar ficheiro de áudio
    console.log("Carregando ficheiro de áudio...");
  }

  stopAudio() {
    // TODO: parar áudio
    console.log("Parando áudio...");
  }

  setVisualization(type) {
    // TODO: definir tipo de visualização
    console.log(`Definindo visualização: ${type}`);
  }

  exportFrame() {
    // TODO: exportar frame atual
    console.log("Exportando frame...");
  }

  destroy() {
    // TODO: limpar recursos
    console.log("Destruindo aplicação...");
  }
}

// Inicialização da aplicação quando o DOM estiver carregado
document.addEventListener("DOMContentLoaded", () => {
  const app = new App();

  // Expor app globalmente para debugging (remover em produção)
  window.app = app;
});
