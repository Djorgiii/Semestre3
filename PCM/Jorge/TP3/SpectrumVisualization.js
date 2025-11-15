class SpectrumVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);
    this.name = "Espectro de Frequências";

    this.properties = {
      ...this.properties,
      barSpacing:   1,
      useGradient:  true,
      color:        "#4aa3ff",
      dynamicColor: true,
    };
  }

  draw() {
    this.update();
    this.clearCanvas();
    if (this.properties.showGrid) this.drawGrid();

    const { freq, amount, level } = this.normalizeData();
    if (!freq?.length) return;

    const ctx = this.ctx;
    const w   = this.canvas.clientWidth;
    const h   = this.canvas.clientHeight;

    const bars = Math.min(amount, freq.length);
    const step = Math.max(1, Math.floor(freq.length / bars));
    const barW = Math.max(1, (w / bars) - this.properties.barSpacing);

    const lvl = Math.min(1, Math.pow(level, 0.5) * 1.8);

    for (let i = 0; i < bars; i++) {
      const v = freq[i * step] / 255;
      const barH = v * (h - 5);
      const x = i * (barW + this.properties.barSpacing);
      const y = h - barH;

      let baseHue, baseLight;
      if (this.properties.dynamicColor) {
        baseHue   = 200 + lvl * 120 + (i / bars) * 30;
        baseLight = 40 + v * 40;
      }

      if (this.properties.useGradient) {
        const grad = ctx.createLinearGradient(0, y, 0, y + barH);

        if (this.properties.dynamicColor) {
          const topLight    = Math.min(100, baseLight + 15);
          const bottomLight = Math.max(0,   baseLight - 10);
          grad.addColorStop(0, `hsl(${baseHue}, 90%, ${topLight}%)`);
          grad.addColorStop(1, `hsl(${baseHue}, 90%, ${bottomLight}%)`);
        } else {
          grad.addColorStop(0, this.properties.color);
          grad.addColorStop(1, "#ffffff");
        }

        ctx.fillStyle = grad;
      } else {
        ctx.fillStyle = this.properties.dynamicColor
          ? `hsl(${baseHue}, 90%, ${baseLight}%)`
          : this.properties.color;
      }

      ctx.fillRect(x, y, barW, barH);
    }
  }
}
