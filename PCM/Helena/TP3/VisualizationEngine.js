// core/VisualizationEngine.js

class VisualizationEngine {
  constructor(canvasOrId, processor) {
    this.canvas = typeof canvasOrId === "string" ? document.getElementById(canvasOrId) : canvasOrId;
    this.processor = processor;
    this.registry = {
      spectrum: SpectrumVisualization,
      waveform: WaveformVisualization,
      particles: ParticleVisualization,
    };
    this.instance = null;
    this._onResize = () => this.resize();
    window.addEventListener("resize", this._onResize);
  }

  setVisualization(type) {
    const Viz = this.registry[type];
    if (!Viz) return false;
    this.instance?.destroy();
    this.instance = new Viz(this.canvas);
    if (this.processor) this.instance.init(this.processor);
    this.resize();
    this.instance.start();
    return true;
  }

  start() { this.instance?.start(); }
  stop() { this.instance?.stop(); }
  resize() { this.instance?.resize(); }

  getVisualizationProperties() { return this.instance?.getProperties?.() ?? {}; }
  updateVisualizationProperty(prop, val) { this.instance?.updateProperty?.(prop, val); }

  dispose() {
    window.removeEventListener("resize", this._onResize);
    this.instance?.destroy(); this.instance = null;
  }
}
