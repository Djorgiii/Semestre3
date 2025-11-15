class OscilloscopeVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);
    this.name = "Osciloscópio";

    this.properties = {
      ...this.properties,
      thickness: 1.5,
      scale:     1.0,
      showGrid:  true,
    };
  }

  // remove "amount" do painel (não é usado aqui)
  getProperties() {
    const props = { ...super.getProperties(), ...this.properties };
    delete props.amount;
    return props;
  }

  draw() {
    this.update();

    const w = this.canvas.clientWidth;
    const h = this.canvas.clientHeight;
    this.clearCanvas();
    if (this.properties.showGrid) this.drawGrid();

    const { time } = this.normalizeData();
    if (!time?.length) return;

    const ctx     = this.ctx;
    const mid     = h / 2;
    const samples = time.length;
    const slice   = w / samples;
    const scale   = Math.max(0, Math.min(1, this.properties.scale || 1));

    // linha central
    ctx.strokeStyle = "rgba(0, 255, 100, 0.4)";
    ctx.lineWidth   = 1;
    ctx.beginPath();
    ctx.moveTo(0, mid);
    ctx.lineTo(w, mid);
    ctx.stroke();

    // waveform
    ctx.strokeStyle = "#00ff99";
    ctx.lineWidth   = this.properties.thickness || 1.5;
    ctx.beginPath();

    for (let i = 0; i < samples; i++) {
      const v = (time[i] - 128) / 128;
      const x = i * slice;
      const y = mid + v * (h / 2 - 10) * scale;
      if (i === 0) ctx.moveTo(x, y);
      else         ctx.lineTo(x, y);
    }

    ctx.stroke();
  }
}
