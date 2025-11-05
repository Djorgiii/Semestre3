// Classe Abstrata Base para Visualizações
class AudioVisualization {
  constructor(canvas, audioProcessor) {
    if (this.constructor === AudioVisualization) {
      throw new Error(
        "AudioVisualization é uma classe abstrata e não pode ser instanciada diretamente."
      );
    }

    this.canvas = canvas;
    this.ctx = canvas.getContext("2d");
    this.audioProcessor = audioProcessor;
    this.name = "Visualização";
    this.properties = {};
    this.testData = new Uint8Array(256);
    this.frameCount = 0;

    // Inicializar dados de teste
    for (let i = 0; i < this.testData.length; i++) {
      this.testData[i] = Math.sin(i / 10) * 128 + 128;
    }
  }

  draw() {
    throw new Error("Método draw() deve ser implementado pela subclasse.");
  }

  update() {
    // TODO: atualizar estado da visualização
    this.frameCount++;
  }

  resize(width, height) {
    // TODO: redimensionar visualização
    this.canvas.width = width;
    this.canvas.height = height;
  }

  getProperties() {
    // TODO: obter propriedades da visualização
    return this.properties;
  }

  updateProperty(property, value) {
    // TODO: atualizar propriedade
    if (this.properties.hasOwnProperty(property)) {
      this.properties[property] = value;
    }
  }

  clearCanvas() {
    // TODO: limpar canvas
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
  }

  drawGrid() {
    // TODO: desenhar grelha de fundo
  }

  createGradient() {
    // TODO: criar gradiente de cores
    return this.ctx.createLinearGradient(
      0,
      0,
      this.canvas.width,
      this.canvas.height
    );
  }

  normalizeData() {
    // TODO: normalizar dados de áudio
    return [];
  }
}
