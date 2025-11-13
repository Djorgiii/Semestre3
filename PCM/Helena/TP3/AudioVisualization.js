// visualizations/AudioVisualization.js
class AudioVisualization {
  constructor(canvas) {
    if (new.target === AudioVisualization) throw new Error('AudioVisualization é abstrata.');
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d', { alpha: false });
    this.processor = null;
    this.properties = { bg: '#0a2f59', grid: 1, smoothing: 0.8 };
    this._running = false; this._raf = 0; this._last = 0;
  }

  init(processor) { this.processor = processor; this.resize(); }
  start() {
    if (this._running) return; this._running = true; this._last = performance.now();
    const loop = (ts) => {
      if (!this._running) return;
      this._raf = requestAnimationFrame(loop);
      const dt = (ts - this._last) / 1000; this._last = ts;
      const { time, freq } = this.processor?.readFrame() ?? {};
      this.processor?.setSmoothing?.(this.properties.smoothing);
      this.clear(); this.render({ time, freq, dt, ctx: this.ctx, w: this.canvas.clientWidth, h: this.canvas.clientHeight });
    };
    this._raf = requestAnimationFrame(loop);
  }
  stop() { this._running = false; cancelAnimationFrame(this._raf); }
  destroy() { this.stop(); }

  resize() {
    const dpr = Math.max(1, Math.min(devicePixelRatio || 1, 2));
    const w = this.canvas.clientWidth, h = this.canvas.clientHeight;
    if (!w || !h) return;
    this.canvas.width = Math.floor(w * dpr); this.canvas.height = Math.floor(h * dpr);
    this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  clear() {
    this.ctx.fillStyle = this.properties.bg;
    this.ctx.fillRect(0, 0, this.canvas.clientWidth, this.canvas.clientHeight);
    if (this.properties.grid) this.grid();
  }

  grid(step = 40) {
    const { ctx } = this, w = this.canvas.clientWidth, h = this.canvas.clientHeight;
    ctx.save(); ctx.strokeStyle = 'rgba(255,255,255,.06)'; ctx.lineWidth = 1;
    for (let x = 0; x < w; x += step) { ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, h); ctx.stroke(); }
    for (let y = 0; y < h; y += step) { ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(w, y); ctx.stroke(); }
    ctx.restore();
  }

  level() { return this.processor?.calculateAudioLevel?.() ?? 0; }
  getProperties() { return { ...this.properties }; }
  updateProperty(k, v) { if (k in this.properties) this.properties[k] = v; }
  render(_args) {}
}
window.AudioVisualization = AudioVisualization;