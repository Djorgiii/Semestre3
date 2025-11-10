// visualizations/ParticleVisualization.js
class ParticleVisualization extends AudioVisualization {
  constructor(canvas) {
    super(canvas);
    this.properties = { ...this.properties, count: 70, linkDistance: 110, linkAlpha: 0.5, colorBase: 210, dynamicColor: 1 };
    this.particles = [];
    this._init();
    
  }
  getProperties() { return { ...this.properties }; }
  updateProperty(k, v) { if (k in this.properties) { this.properties[k] = v; if (k === 'count') this._init(); } }

  _init() {
    const w = this.canvas.clientWidth, h = this.canvas.clientHeight;
    this.particles = Array.from({ length: this.properties.count }, () => ({
      x: Math.random() * w, y: Math.random() * h,
      vx: (Math.random() - 0.5) * 1.5, vy: (Math.random() - 0.5) * 1.5,
      r: Math.random() * 2 + 1
    }));
  }
  resize() { super.resize(); this._init(); }

  render({ freq, ctx, w, h }) {
    const L = this.level();
    const hue = (this.properties.colorBase + (this.properties.dynamicColor ? 160 * L : 0)) % 360;

    ctx.fillStyle = `hsl(${hue} 100% ${65 + 10 * L}%)`;
    for (const p of this.particles) {
      const boost = freq && freq.length ? (freq[(Math.random() * freq.length) | 0] / 255) * 0.6 : 0;
      p.vx += (Math.random() - 0.5) * boost; p.vy += (Math.random() - 0.5) * boost;
      const s = Math.hypot(p.vx, p.vy), maxS = 3 + 2 * L;
      if (s > maxS) { p.vx = (p.vx / s) * maxS; p.vy = (p.vy / s) * maxS; }
      p.x += p.vx; p.y += p.vy;
      if (p.x < 0 || p.x > w) p.vx *= -1; if (p.y < 0 || p.y > h) p.vy *= -1;
      ctx.beginPath(); ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2); ctx.fill();
    }

    const maxD = this.properties.linkDistance;
    ctx.strokeStyle = `hsla(${hue} 100% 60% / ${0.35 + 0.35 * L})`;
    ctx.lineWidth = 1;
    for (let i = 0; i < this.particles.length; i++) {
      for (let j = i + 1; j < this.particles.length; j++) {
        const a = this.particles[i], b = this.particles[j];
        const d = Math.hypot(a.x - b.x, a.y - b.y);
        if (d < maxD) {
          ctx.globalAlpha = (1 - d / maxD) * this.properties.linkAlpha;
          ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.lineTo(b.x, b.y); ctx.stroke();
        }
      }
    }
    ctx.globalAlpha = 1;
  }
}
window.ParticleVisualization = ParticleVisualization;
