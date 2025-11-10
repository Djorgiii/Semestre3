// visualizations/WaveformVisualization.js


class WaveformVisualization extends AudioVisualization {
  constructor(canvas) {
    super(canvas);
    this.properties = { ...this.properties, colorBase: 200, dynamicColor: 1, thickness: 2, glow: 1 };
  }
  getProperties() { return { ...this.properties }; }
  updateProperty(k, v) { if (k in this.properties) this.properties[k] = v; }

  render({ time, dt, ctx, w, h }) {
    // linha média
    ctx.strokeStyle = 'rgba(255,255,255,.07)'; ctx.lineWidth = 1;
    ctx.beginPath(); ctx.moveTo(0, h/2); ctx.lineTo(w, h/2); ctx.stroke();

    // cor dinâmica
    const L = this.level();
    const hue = (this.properties.colorBase + (this.properties.dynamicColor ? 160 * L : 0)) % 360;
    const lum = 45 + 20 * L;
    const stroke = `hsl(${hue} 100% ${lum}%)`;
    ctx.lineWidth = this.properties.thickness + L * 2;
    ctx.strokeStyle = stroke;
    ctx.shadowBlur = this.properties.glow ? 8 + 12 * L : 0;
    ctx.shadowColor = stroke;

    // fallback demo se não houver dados
    if (!time) {
      this._phi = (this._phi || 0) + (dt || 0) * 2.0;
      const A = h * 0.18, f = 2 * Math.PI / 180;
      ctx.beginPath();
      for (let x = 0; x < w; x++) {
        const y = h/2 + A * Math.sin(this._phi + x * f);
        x ? ctx.lineTo(x, y) : ctx.moveTo(x, y);
      }
      ctx.stroke(); return;
    }

    // dados reais
    const N = time.length, step = Math.max(1, Math.floor(N / w));
    ctx.beginPath();
    for (let x = 0, i = 0; x < w && i < N; x++, i += step) {
      const y = (0.5 - time[i] / 2) * h;
      x ? ctx.lineTo(x, y) : ctx.moveTo(x, y);
    }
    ctx.stroke();
  }
}
window.WaveformVisualization = WaveformVisualization;
