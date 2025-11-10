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

  getVisualizationProperties() {
  const vis = this.instance;
  const props = vis?.getProperties ? vis.getProperties() : {};
  const sens = (this.processor && this.processor._smooth != null) ? (1 - this.processor._smooth) : 0.2;
  return { ...props, sensitivity: sens }; // junta sensibilidade global
}

updateVisualizationProperty(prop, val) {
  if (prop === 'sensitivity' && this.processor && this.processor.setSmoothing) {
    this.processor.setSmoothing(val); // 0..1
    return;
  }
  // resto vai para a visualização atual
  if (this.instance && this.instance.updateProperty) this.instance.updateProperty(prop, val);
}

  dispose() {
    window.removeEventListener("resize", this._onResize);
    this.instance?.destroy(); this.instance = null;
  }
}
