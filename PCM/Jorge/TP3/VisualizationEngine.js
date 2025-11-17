class VisualizationEngine {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    this.ctx = this.canvas.getContext("2d");

    this.visualizations = new Map();
    this.currentVisualization = null;
    this.animationId = null;
    this.isRunning = false;
    this.audioProcessor = null;

    this.handleResize = this.resize.bind(this);
    window.addEventListener("resize", this.handleResize, { passive: true });
    this.resize();

    this.initVisualizations();
  }

  setAudioProcessor(audioProcessor) {
    this.audioProcessor = audioProcessor;

    for (const visualization of this.visualizations.values()) {
      visualization.audioProcessor = audioProcessor;
    }
  }

  initVisualizations() {
    this.visualizations.set(
      "spectrum",
      new SpectrumVisualization(this.canvas, this.audioProcessor)
    );

    this.visualizations.set(
      "waveform",
      new WaveformVisualization(this.canvas, this.audioProcessor)
    );

    this.visualizations.set(
      "oscilloscope",
      new OscilloscopeVisualization(this.canvas, this.audioProcessor)
    );

    this.visualizations.set(
      "particles",
      new ParticleVisualization(this.canvas, this.audioProcessor)
    );

    this.setVisualization("spectrum");
  }

  setVisualization(type) {
    const visualization = this.visualizations.get(type);
    if (!visualization) return false;

    this.currentVisualization = visualization;
    visualization.resize(this.canvas.clientWidth, this.canvas.clientHeight);
    return true;
  }

  start() {
    if (this.isRunning) return;
    this.isRunning = true;

    const renderLoop = () => {
      if (!this.isRunning) return;

      if (this.currentVisualization) {
        // cada visualização chama super.update() dentro do draw()
        this.currentVisualization.draw();
      } else {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
      }

      this.animationId = requestAnimationFrame(renderLoop);
    };

    this.animationId = requestAnimationFrame(renderLoop);
  }

  stop() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
    }

    this.animationId = null;
    this.isRunning = false;
  }

  resize() {
    const devicePixelRatio = Math.min(window.devicePixelRatio || 1, 2);
    const cssWidth = this.canvas.clientWidth || 300;
    const cssHeight = this.canvas.clientHeight || 150;

    this.canvas.width = Math.floor(cssWidth * devicePixelRatio);
    this.canvas.height = Math.floor(cssHeight * devicePixelRatio);

    this.ctx.setTransform(devicePixelRatio, 0, 0, devicePixelRatio, 0, 0);

    if (this.currentVisualization) {
      this.currentVisualization.resize(cssWidth, cssHeight);
    }
  }

  getVisualizationProperties() {
    return this.currentVisualization?.getProperties?.() || {};
  }

  updateVisualizationProperty(propertyName, value) {
    this.currentVisualization?.updateProperty?.(propertyName, value);
  }

  getCurrentVisualization() {
    return this.currentVisualization;
  }

  destroy() {
    this.stop();
    window.removeEventListener("resize", this.handleResize);
  }
}
