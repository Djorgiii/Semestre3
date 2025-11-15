class WaveformVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);
    this.name = "Forma de Onda";
    this.properties = {
      ...this.properties,
      thickness: 2,
      scale:     1.0,
      fadeTrail: 0,
      color:     "#e8f0ff",
    };
  }

  draw() {
    this.update();

    const w = this.canvas.clientWidth;
    const h = this.canvas.clientHeight;
    const f = Math.max(0, Math.min(0.2, this.properties.fadeTrail || 0));

    if (f > 0) {
      this.ctx.fillStyle = `rgba(5, 20, 50, ${f})`;
      this.ctx.fillRect(0, 0, w, h);
    } else {
      this.clearCanvas();
    }

    if (this.properties.showGrid) this.drawGrid();

    const { time } = this.normalizeData();
    if (!time?.length) return;

    const ctx   = this.ctx;
    const mid   = h / 2;
    const slice = w / time.length;
    const scale = Math.max(0, Math.min(1, this.properties.scale || 1));

    ctx.beginPath();
    ctx.lineWidth   = Math.max(1, this.properties.thickness | 0);
    ctx.strokeStyle = this.properties.color;

    for (let i = 0; i < time.length; i++) {
      const v = (time[i] - 128) / 128;
      const y = mid + v * (h / 2 - 10) * scale;
      const x = i * slice;
      if (i === 0) ctx.moveTo(x, y);
      else         ctx.lineTo(x, y);
    }

    ctx.stroke();
  }
}
