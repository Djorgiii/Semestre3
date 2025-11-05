// Web Audio wrapper para input do microfone + AnalyserNode
export default class AudioProcessor {
  constructor({ fftSize = 2048 } = {}) {
    this.fftSize = fftSize;
    this.ctx = null;
    this.stream = null;
    this.source = null;
    this.gainNode = null;
    this.analyser = null;

    this.timeData = null;
    this.freqData = null;
  }

  async start() {
    if (this.ctx) return; // já iniciado
    this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    this.ctx = new (window.AudioContext || window.webkitAudioContext)();
    this.source = this.ctx.createMediaStreamSource(this.stream);
    this.gainNode = this.ctx.createGain();

    this.analyser = this.ctx.createAnalyser();
    this.analyser.fftSize = this.fftSize;
    this.analyser.smoothingTimeConstant = 0.8;

    this.timeData = new Float32Array(this.analyser.fftSize);
    this.freqData = new Uint8Array(this.analyser.frequencyBinCount);

    // grafo: mic -> gain -> analyser -> destino (silencioso)
    this.source.connect(this.gainNode);
    this.gainNode.connect(this.analyser);
    // opcional: enviar para colunas comentado para não ecoar
    // this.analyser.connect(this.ctx.destination);
  }

  async stop() {
    if (!this.ctx) return;
    this.stream.getTracks().forEach(t => t.stop());
    this.stream = null;
    this.source?.disconnect();
    this.gainNode?.disconnect();
    this.analyser?.disconnect();
    await this.ctx.close();
    this.ctx = this.source = this.gainNode = this.analyser = null;
  }

  setGain(value) {
    if (this.gainNode) this.gainNode.gain.value = value;
  }

  getSampleRate() {
    return this.ctx?.sampleRate ?? 0;
  }

  /** Enche buffers internos e devolve referências para uso na frame */
  readFrame() {
    if (!this.analyser) return { time: null, freq: null };
    this.analyser.getFloatTimeDomainData(this.timeData);
    this.analyser.getByteFrequencyData(this.freqData);
    return { time: this.timeData, freq: this.freqData };
  }
}
