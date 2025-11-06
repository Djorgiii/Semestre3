// visualizations/SpectrumVisualization.js
class SpectrumVisualization extends AudioVisualization {
  constructor(canvas) {
    super(canvas);
    this.properties = { ...this.properties, c1: 200, c2: 170, dynamicColor: 1, barWidth: 6, gap: 2, scale: 1.0, radial: 0, rotationSpeed: 0.4 };
    this._angle = 0;
  }
  getProperties() { return { ...this.properties }; }
  updateProperty(k, v) { if (k in this.properties) this.properties[k] = v; }

  render({ freq, dt, ctx, w, h }) {
    if (!freq) return;

    // modo radial (espectro circular)
    if (this.properties.radial) {
      this._angle += (this.properties.rotationSpeed || 0) * dt;
      const L = this.level();
      const h1 = (this.properties.c1 + (this.properties.dynamicColor ? 120 * L : 0)) % 360;
      const h2 = (this.properties.c2 + (this.properties.dynamicColor ? 160 * L : 0)) % 360;

      ctx.save(); ctx.translate(w/2, h/2); ctx.rotate(this._angle);
      const R = Math.min(w, h) * 0.34, T = Math.min(w, h) * 0.22;

      for (let i = 0; i < freq.length; i++) {
        const t = i / freq.length, v = freq[i] / 255, a = t * Math.PI * 2;
        const r0 = R, r1 = R + T * (0.15 + 0.85 * v);
        const hue = h1 + (h2 - h1) * t;
        ctx.strokeStyle = `hsl(${hue} 100% ${45 + 20 * v}%)`;
        ctx.lineWidth = 2 + 2 * v;
        ctx.beginPath(); ctx.arc(0, 0, (r0 + r1) * 0.5, a, a + 0.012 + 0.01 * v); ctx.stroke();
      }
      ctx.restore();
      const g = ctx.createRadialGradient(w/2, h/2, 0, w/2, h/2, Math.min(w, h) * 0.5);
      g.addColorStop(0, `hsla(${h2} 100% 60% / ${0.18 + 0.25 * this.level()})`);
      g.addColorStop(1, 'transparent');
      ctx.fillStyle = g; ctx.fillRect(0,0,w,h);
      return;
    }

    // barras verticais
    ctx.strokeStyle = 'rgba(255,255,255,.07)'; ctx.lineWidth = 1;
    ctx.beginPath(); for (let i = 1; i < 4; i++) { const y = (h*i)/4; ctx.moveTo(0,y); ctx.lineTo(w,y); } ctx.stroke();

    const L = this.level();
    const h1 = (this.properties.c1 + (this.properties.dynamicColor ? 120 * L : 0)) % 360;
    const h2 = (this.properties.c2 + (this.properties.dynamicColor ? 180 * L : 0)) % 360;
    const grad = ctx.createLinearGradient(0, 0, 0, h);
    grad.addColorStop(0, `hsl(${h1} 100% ${55 + 10 * L}%)`);
    grad.addColorStop(1, `hsl(${h2} 100% ${45 + 5 * L}%)`);
    ctx.fillStyle = grad;

    const barW = this.properties.barWidth, gap = this.properties.gap;
    let x = 0;
    for (let i = 0; i < freq.length; i++) {
      const v = freq[i] / 255, hh = v * h * this.properties.scale;
      ctx.fillRect(x, h - hh, barW, hh);
      x += barW + gap; if (x > w) break;
    }
  }
}
window.SpectrumVisualization = SpectrumVisualization;
