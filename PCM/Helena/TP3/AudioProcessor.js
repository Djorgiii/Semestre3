// core/AudioProcessor.js
class AudioProcessor {
  constructor({ fftSize = 2048 } = {}) {
    this.fftSize = fftSize;
    this.ctx = null;
    this.stream = null;
    this.mediaSource = null;
    this.mediaElement = null;
    this.elementSource = null;
    this.analyser = null;
    this.gain = null;
    this.timeData = null;    // Float32
    this.freqData = null;    // Uint8
    this._level = 0;
    this._smooth = 0.8;
  }

  _ensureCtx() {
    if (!this.ctx) {
      const AC = window.AudioContext || window.webkitAudioContext;
      this.ctx = new AC();
    }
    if (!this.analyser) {
      this.analyser = this.ctx.createAnalyser();
      this.analyser.fftSize = this.fftSize;
      this.analyser.smoothingTimeConstant = this._smooth;
      this.timeData = new Float32Array(this.analyser.fftSize);
      this.freqData = new Uint8Array(this.analyser.frequencyBinCount);
    }
    if (!this.gain) { this.gain = this.ctx.createGain(); this.gain.gain.value = 1; }
  }

  async startMicrophone() {
    await this.stop();
    this._ensureCtx();
    this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    this.mediaSource = this.ctx.createMediaStreamSource(this.stream);
    this.mediaSource.connect(this.gain);
    this.gain.connect(this.analyser);
    return true;
  }

  async loadAudioFile(file) {
    await this.stop();
    this._ensureCtx();
    const el = new Audio();
    el.src = URL.createObjectURL(file);
    el.loop = true;
    await el.play();
    this.mediaElement = el;
    this.elementSource = this.ctx.createMediaElementSource(el);
    this.elementSource.connect(this.gain);
    this.gain.connect(this.analyser);
    this.analyser.connect(this.ctx.destination); // ouvir
    return true;
  }

  async stop() {
    if (this.stream) { this.stream.getTracks().forEach(t => t.stop()); this.stream = null; }
    try { this.mediaSource?.disconnect(); } catch {}
    try { this.elementSource?.disconnect(); } catch {}
    try { this.gain?.disconnect(); } catch {}
    try { this.analyser?.disconnect(this.ctx?.destination); } catch {}
    this.mediaSource = this.elementSource = null;
    if (this.mediaElement) { try { this.mediaElement.pause(); URL.revokeObjectURL(this.mediaElement.src); } catch {} this.mediaElement = null; }
  }

  setSmoothing(v) { this._smooth = Math.max(0, Math.min(0.99, v)); if (this.analyser) this.analyser.smoothingTimeConstant = this._smooth; }

  readFrame() {
    if (!this.analyser) return { time: null, freq: null };
    this.analyser.getFloatTimeDomainData(this.timeData);
    this.analyser.getByteFrequencyData(this.freqData);

    // nível RMS suavizado
    let sum = 0; for (let i = 0; i < this.timeData.length; i++) sum += this.timeData[i] * this.timeData[i];
    const rms = Math.sqrt(sum / this.timeData.length);
    this._level += (rms - this._level) * 0.15;

    return { time: this.timeData, freq: this.freqData };
  }

  calculateAudioLevel() { return this._level; }
  getFrequencyData() { return this.freqData; }
  getWaveformData() { return this.timeData; }
}
window.AudioProcessor = AudioProcessor;
