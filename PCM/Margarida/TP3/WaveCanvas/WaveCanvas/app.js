

class App {
  constructor() {
    this.audioProcessor = new AudioProcessor();
    this.visualizationEngine = new VisualizationEngine("audioCanvas", this.audioProcessor);
    this.uiManager = new UIManager(this);
    this.exportManager = new ExportManager(this.visualizationEngine);

    this.init();
  }

  init() {
    console.log("App inicializada");
  }

  async startMicrophone() {
    try {
      await this.audioProcessor.startMicrophone();
      this.visualizationEngine.start();
      this.uiManager.setButtonStates(true);
      console.log("Microfone iniciado");
    } catch (e) {
      this.uiManager.showError("Falha ao iniciar microfone");
    }
  }

  async loadAudioFile(file) {
    try {
      await this.audioProcessor.loadAudioFile(file);
      this.visualizationEngine.start();
      this.uiManager.setButtonStates(true);
      console.log("Áudio carregado");
    } catch (e) {
      this.uiManager.showError("Falha ao carregar o ficheiro");
    }
  }

  stopAudio() {
    this.audioProcessor.stop();
    this.visualizationEngine.stop();
    this.uiManager.setButtonStates(false);
    console.log("Áudio parado");
  }

  setVisualization(type) {
    const success = this.visualizationEngine.setVisualization(type);
    if (!success) {
      this.uiManager.showError("Visualização inválida!");
    }
    this.uiManager.updatePropertiesPanel();
  }

  exportFrame() {
    this.exportManager.exportAsPNG();
  }

  destroy() {
    this.stopAudio();
    console.log("Aplicação destruída.");
  }
}


class AudioProcessor {
  constructor() {
    this.audioContext = null;
    this.analyser = null;
    this.source = null;
    this.mediaStream = null;

    this.frequencyData = new Uint8Array(512);
    this.waveformData = new Uint8Array(512);
  }

  async startMicrophone() {
    this.audioContext = new AudioContext();
    this.mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });

    this.source = this.audioContext.createMediaStreamSource(this.mediaStream);
    this.setupAnalyser();
  }

  async loadAudioFile(file) {
    this.audioContext = new AudioContext();
    const buffer = await file.arrayBuffer();
    const decoded = await this.audioContext.decodeAudioData(buffer);

    this.source = this.audioContext.createBufferSource();
    this.source.buffer = decoded;
    this.source.start();

    this.setupAnalyser();
  }

  setupAnalyser() {
    this.analyser = this.audioContext.createAnalyser();
    this.analyser.fftSize = 1024;
    this.frequencyData = new Uint8Array(this.analyser.frequencyBinCount);
    this.waveformData = new Uint8Array(this.analyser.fftSize);

    this.source.connect(this.analyser);
    this.analyser.connect(this.audioContext.destination);
  }

  stop() {
    if (this.source?.stop) this.source.stop();
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach(track => track.stop());
    }
  }

  update() {
    if (!this.analyser) return;
    this.analyser.getByteFrequencyData(this.frequencyData);
    this.analyser.getByteTimeDomainData(this.waveformData);
  }

  getFrequencyData() { return this.frequencyData; }
  getWaveformData() { return this.waveformData; }

  calculateAudioLevel() {
    let sum = 0;
    for (let i = 0; i < this.frequencyData.length; i++) {
      sum += this.frequencyData[i];
    }
    return (sum / this.frequencyData.length) / 255;
  }
}


class VisualizationEngine {
  constructor(canvasId, audioProcessor) {
    this.canvas = document.getElementById(canvasId);
    this.ctx = this.canvas.getContext("2d");

    this.audioProcessor = audioProcessor;
    this.visualizations = new Map();
    this.currentVisualization = null;
    this.animationId = null;

    this.initVisualizations();
  }

  initVisualizations() {
    this.visualizations.set("spectrum", new SpectrumVisualization(this.canvas, this.audioProcessor));
    this.visualizations.set("waveform", new WaveformVisualization(this.canvas, this.audioProcessor));
    this.visualizations.set("particles", new ParticleVisualization(this.canvas, this.audioProcessor));
  }

  setVisualization(type) {
    const selected = this.visualizations.get(type);
    if (!selected) return false;

    this.currentVisualization = selected;
    return true;
  }

  start() {
    const loop = () => {
      this.audioProcessor.update();

      if (this.currentVisualization) {
        this.currentVisualization.update();
        this.currentVisualization.draw();
      }

      this.animationId = requestAnimationFrame(loop);
    };
    loop();
  }

  stop() {
    cancelAnimationFrame(this.animationId);
  }

  getVisualizationProperties() {
    return this.currentVisualization ? this.currentVisualization.getProperties() : {};
  }

  updateVisualizationProperty(property, value) {
    if (this.currentVisualization) {
      this.currentVisualization.updateProperty(property, value);
    }
  }
}


class AudioVisualization {
  constructor(canvas, audioProcessor) {
    this.canvas = canvas;
    this.ctx = canvas.getContext("2d");
    this.audioProcessor = audioProcessor;

    this.properties = {
      lineWidth: 2,
      color: "#4cc9f0",
    };
  }

  clearCanvas() {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
  }

  update() {}

  getProperties() {
    return this.properties;
  }

  updateProperty(property, value) {
    this.properties[property] = value;
  }

  draw() {
    throw new Error("Método draw() deve ser implementado.");
  }
}

class SpectrumVisualization extends AudioVisualization {
  draw() {
    this.clearCanvas();
    const data = this.audioProcessor.getFrequencyData();
    const barWidth = this.canvas.width / data.length;

    for (let i = 0; i < data.length; i++) {
      const h = (data[i] / 255) * this.canvas.height;
      this.ctx.fillStyle = `hsl(${i / 2}, 100%, 50%)`;
      this.ctx.fillRect(i * barWidth, this.canvas.height - h, barWidth - 1, h);
    }
  }
}

class WaveformVisualization extends AudioVisualization {
  draw() {
    this.clearCanvas();

    const data = this.audioProcessor.getWaveformData();
    const sliceWidth = this.canvas.width / data.length;

    this.ctx.beginPath();
    this.ctx.lineWidth = this.properties.lineWidth;
    this.ctx.strokeStyle = this.properties.color;

    for (let i = 0; i < data.length; i++) {
      const x = i * sliceWidth;
      const y = (data[i] / 255) * this.canvas.height;

      if (i === 0) this.ctx.moveTo(x, y);
      else this.ctx.lineTo(x, y);
    }

    this.ctx.stroke();
  }
}

class ParticleVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);

    this.particles = [];
    for (let i = 0; i < 60; i++) {
      this.particles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        vx: (Math.random() - .5) * 2,
        vy: (Math.random() - .5) * 2,
        r: 2 + Math.random() * 3
      });
    }
  }

  update() {
    const level = this.audioProcessor.calculateAudioLevel();

    this.particles.forEach(p => {
      p.x += p.vx * (1 + level);
      p.y += p.vy * (1 + level);

      if (p.x < 0 || p.x > this.canvas.width) p.vx *= -1;
      if (p.y < 0 || p.y > this.canvas.height) p.vy *= -1;
    });
  }

  draw() {
    this.clearCanvas();

    this.ctx.fillStyle = "#4cc9f0";
    this.particles.forEach(p => {
      this.ctx.beginPath();
      this.ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
      this.ctx.fill();
    });
  }
}


class UIManager {
  constructor(app) {
    this.app = app;

    this.setupEventListeners();
  }

  updatePropertiesPanel() {
    console.log("Painel de propriedades atualizado");
  }

  updateAudioInfo(info, isError = false) {
    // Placeholder — UI real depende do teu HTML
  }

  setButtonStates(playing) {
    const startMicBtn = document.getElementById("startMic");
    const stopAudioBtn = document.getElementById("stopAudio");

    startMicBtn.disabled = playing;
    stopAudioBtn.disabled = !playing;
  }

  showError(message) {
    alert(message);
  }

  setupEventListeners() {
    document.getElementById("startMic").addEventListener("click", () => {
      this.app.startMicrophone();
    });

    document.getElementById("stopAudio").addEventListener("click", () => {
      this.app.stopAudio();
    });

    document.getElementById("audioFile").addEventListener("change", (e) => {
      if (e.target.files.length) {
        this.app.loadAudioFile(e.target.files[0]);
      }
    });

    document
      .getElementById("visualizationType")
      .addEventListener("change", (e) => {
        this.app.setVisualization(e.target.value);
      });
  }
}


class ExportManager {
  constructor(engine) {
    this.engine = engine;
  }

  exportAsPNG() {
    const canvas = this.engine.canvas;
    const link = document.createElement("a");
    link.download = "visualization.png";
    link.href = canvas.toDataURL("image/png");
    link.click();
  }

  exportAsJPEG(quality = 0.9) {
    const canvas = this.engine.canvas;
    const link = document.createElement("a");
    link.download = "visualization.jpg";
    link.href = canvas.toDataURL("image/jpeg", quality);
    link.click();
  }
}

// ======================================================
// ==================== INICIALIZAÇÃO ===================
// ======================================================

document.addEventListener("DOMContentLoaded", () => {
  const app = new App();
  window.app = app;
});
