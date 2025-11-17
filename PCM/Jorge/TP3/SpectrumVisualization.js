class SpectrumVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);
    this.name = "Espectro de Frequências";

    this.properties = {
      ...this.properties,
      barSpacing: 1,
      useGradient: true,
      color: "#4aa3ff",
      dynamicColor: true,
    };
  }

  draw() {
    this.update();
    this.clearCanvas();

    if (this.properties.showGrid) {
      this.drawGrid();
    }

    const { freq: frequencyData, amount, level } = this.normalizeData();
    if (!frequencyData?.length) return;

    const ctx = this.ctx;
    const canvasWidth  = this.canvas.clientWidth;
    const canvasHeight = this.canvas.clientHeight;

    const barCount = Math.min(amount, frequencyData.length);
    const step = Math.max(1, Math.floor(frequencyData.length / barCount));
    const barWidth = Math.max(
      1,
      canvasWidth / barCount - this.properties.barSpacing
    );

    // realça níveis baixos: sqrt(level) * 1.8
    const audioIntensity = Math.min(1, Math.pow(level, 0.5) * 1.8);

    for (let i = 0; i < barCount; i++) {
      const normalizedValue = frequencyData[i * step] / 255; // 0..1
      const barHeight = normalizedValue * (canvasHeight - 5);

      const x = i * (barWidth + this.properties.barSpacing);
      const y = canvasHeight - barHeight;

      let baseHue;
      let baseLightness;

      if (this.properties.dynamicColor) {
        baseHue =
          200 + audioIntensity * 120 + (i / barCount) * 30; // posição + nível global
        baseLightness = 40 + normalizedValue * 40; // barras mais altas = mais claras
      }

      if (this.properties.useGradient) {
        // Gradiente vertical por barra
        const gradient = ctx.createLinearGradient(0, y, 0, y + barHeight);

        if (this.properties.dynamicColor) {
          const topLightness = Math.min(100, baseLightness + 15);
          const bottomLightness = Math.max(0, baseLightness - 10);

          gradient.addColorStop(
            0,
            `hsl(${baseHue}, 90%, ${topLightness}%)`
          );
          gradient.addColorStop(
            1,
            `hsl(${baseHue}, 90%, ${bottomLightness}%)`
          );
        } else {
          gradient.addColorStop(0, this.properties.color);
          gradient.addColorStop(1, "#ffffff");
        }

        ctx.fillStyle = gradient;
      } else {
        ctx.fillStyle = this.properties.dynamicColor
          ? `hsl(${baseHue}, 90%, ${baseLightness}%)`
          : this.properties.color;
      }

      ctx.fillRect(x, y, barWidth, barHeight);
    }
  }
}
