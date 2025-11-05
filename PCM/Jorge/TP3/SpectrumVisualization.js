// Visualizações Concretas
class SpectrumVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);
    this.name = "Espectro de Frequências";
    // Inicializar propriedades específicas
  }

  draw() {
    // TODO: desenhar espectro de frequências
    this.clearCanvas();

    // Implementação básica para teste
    const data = this.audioProcessor
      ? this.audioProcessor.getFrequencyData()
      : this.testData;
    const barWidth = this.canvas.width / data.length;

    for (let i = 0; i < data.length; i++) {
      const barHeight = (data[i] / 255) * this.canvas.height;
      const x = i * barWidth;
      const y = this.canvas.height - barHeight;

      this.ctx.fillStyle = `hsl(${(i / data.length) * 360}, 100%, 50%)`;
      this.ctx.fillRect(x, y, barWidth - 1, barHeight);
    }
  }

  getProperties() {
    // TODO: obter propriedades específicas
    return super.getProperties();
  }
}
