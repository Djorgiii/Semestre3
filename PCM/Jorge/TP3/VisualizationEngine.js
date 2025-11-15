class VisualizationEngine {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    this.ctx    = this.canvas.getContext("2d");

    this.visualizations     = new Map();
    this.currentVisualization = null;
    this.animationId        = null;
    this.isRunning          = false;
    this.audioProcessor     = null;

    this._onResize = this.resize.bind(this);
    window.addEventListener("resize", this._onResize, { passive: true });
    this.resize();

    this.initVisualizations();
  }

  setAudioProcessor(p) {
    this.audioProcessor = p;
    for (const viz of this.visualizations.values()) {
      viz.audioProcessor = p;
    }
  }

  initVisualizations() {
    this.visualizations.set("spectrum",
      new SpectrumVisualization(this.canvas, this.audioProcessor));
    this.visualizations.set("waveform",
      new WaveformVisualization(this.canvas, this.audioProcessor));
    this.visualizations.set("oscilloscope",
      new OscilloscopeVisualization(this.canvas, this.audioProcessor));
    this.visualizations.set("particles",
      new ParticleVisualization(this.canvas, this.audioProcessor));

    this.setVisualization("spectrum");
  }

  setVisualization(type) {
    const viz = this.visualizations.get(type);
    if (!viz) return false;
    this.currentVisualization = viz;
    viz.resize(this.canvas.clientWidth, this.canvas.clientHeight);
    return true;
  }

  start() {
    if (this.isRunning) return;
    this.isRunning = true;

    const loop = () => {
      if (!this.isRunning) return;

      if (this.currentVisualization) {
        // cada visualização chama super.update() dentro do draw()
        this.currentVisualization.draw();
      } else {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
      }

      this.animationId = requestAnimationFrame(loop);
    };

    this.animationId = requestAnimationFrame(loop);
  }

  stop() {
    if (this.animationId) cancelAnimationFrame(this.animationId);
    this.animationId = null;
    this.isRunning   = false;
  }

  resize() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const cw  = this.canvas.clientWidth || 300;
    const ch  = this.canvas.clientHeight || 150;

    this.canvas.width  = Math.floor(cw * dpr);
    this.canvas.height = Math.floor(ch * dpr);
    this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    if (this.currentVisualization) this.currentVisualization.resize(cw, ch);
  }

  getVisualizationProperties() {
    return this.currentVisualization?.getProperties?.() || {};
  }

  updateVisualizationProperty(property, value) {
    this.currentVisualization?.updateProperty?.(property, value);
  }

  getCurrentVisualization() {
    return this.currentVisualization;
  }

  destroy() {
    this.stop();
    window.removeEventListener("resize", this._onResize);
  }
}
