// Classe base "abstrata" para visualizações
export default class AudioVisualization {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx2d = canvas.getContext('2d');
    this.processor = null;
    this._running = false;
    this._raf = 0;
    this._lastTs = 0;
    this.bg = '#0b0d10';
    this.fg = '#e6edf3';
    this.grid = '#18202a';
  }

  init(processor) {
    this.processor = processor;
    this.resize();
  }

  start() {
    if (this._running) return;
    this._running = true;
    this._lastTs = performance.now();
    const tick = (ts) => {
      if (!this._running) return;
      const dt = (ts - this._lastTs) / 1000;
      this._lastTs = ts;
      this._frame(dt);
      this._raf = requestAnimationFrame(tick);
    };
    this._raf = requestAnimationFrame(tick);
  }

  stop() {
    this._running = false;
    cancelAnimationFrame(this._raf);
  }

  destroy() {
    this.stop();
  }

  resize() {
    const dpr = window.devicePixelRatio || 1;
    const { clientWidth: w, clientHeight: h } = this.canvas;
    this.canvas.width = Math.max(1, Math.floor(w * dpr));
    this.canvas.height = Math.max(1, Math.floor(h * dpr));
    this.ctx2d.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  _clear() {
    const c = this.ctx2d;
    c.fillStyle = this.bg;
    c.fillRect(0, 0, this.canvas.clientWidth, this.canvas.clientHeight);
  }

  _frame(dt) {
    const { time, freq } = this.processor?.readFrame() ?? {};
    this._clear();
    this.render({ time, freq, dt, ctx: this.ctx2d, w: this.canvas.clientWidth, h: this.canvas.clientHeight });
  }

  /** Subclasses devem implementar este método */
  // eslint-disable-next-line no-unused-vars
  render({ time, freq, dt, ctx, w, h }) {}
}
