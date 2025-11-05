class AudioProcessor {
  constructor() {
    this.audioContext = null;
    this.analyserNode = null;
    this.sourceNode = null;
    this.mediaStream = null;

    this.frequencyData = null;
    this.waveformData = null;

    this._audioEl = new Audio(); // para ficheiros (modo "element")
    this._audioEl.crossOrigin = "anonymous";
  }

  _ensureCtx() {
    if (!this.audioContext) {
      this.audioContext = new (window.AudioContext || window.webkitAudioContext)({ latencyHint: "interactive" });
      this.analyserNode = this.audioContext.createAnalyser();
      this.analyserNode.fftSize = 2048;
      this.analyserNode.smoothingTimeConstant = 0.7;

      this.frequencyData = new Uint8Array(this.analyserNode.frequencyBinCount);
      this.waveformData  = new Uint8Array(this.analyserNode.fftSize);
    }
  }

  setSmoothing(v) {
    if (this.analyserNode) this.analyserNode.smoothingTimeConstant = Math.max(0, Math.min(0.95, v));
  }

  // --- Start Microphone (diagrama Captura de Microfone) ---
  async startMicrophone() {
    try {
      this._ensureCtx();
      await this.audioContext.resume();

      this.stop(); // limpar anterior

      this.mediaStream = await navigator.mediaDevices.getUserMedia({
        audio: { echoCancellation:false, noiseSuppression:false, autoGainControl:false, channelCount:1 }
      });
      const track = this.mediaStream.getAudioTracks()[0];
      track?.applyConstraints?.({ echoCancellation:false, noiseSuppression:false, autoGainControl:false }).catch(()=>{});

      this.sourceNode = this.audioContext.createMediaStreamSource(this.mediaStream);
      this.sourceNode.connect(this.analyserNode);
      // não ligar ao destination para evitar eco
    } catch (e) {
      throw e;
    }
  }

  // --- Load File (diagrama Carregamento de Ficheiro) ---
  async loadAudioFile(file, mode = "element") {
    this._ensureCtx();
    await this.audioContext.resume();
    this.stop();

    if (mode === "element") {
      this._audioEl.src = URL.createObjectURL(file);
      await this._audioEl.play();
      this.sourceNode = this.audioContext.createMediaElementSource(this._audioEl);
      this.sourceNode.connect(this.analyserNode);
      // opcional: ouvir -> this.analyserNode.connect(this.audioContext.destination);
    } else {
      // modo “buffer/decoder” (se quiseres seguir literalmente o losango do diagrama)
      const arrayBuffer = await file.arrayBuffer();
      const audioBuffer = await this.audioContext.decodeAudioData(arrayBuffer);
      const bufferSource = this.audioContext.createBufferSource();
      bufferSource.buffer = audioBuffer;
      bufferSource.loop = false;
      bufferSource.connect(this.analyserNode);
      // bufferSource.connect(this.audioContext.destination); // se quiseres ouvir
      bufferSource.start();
      this.sourceNode = bufferSource;
    }
  }

  stop() {
    try {
      if (this._audioEl) { this._audioEl.pause(); this._audioEl.src = ""; }
      if (this.mediaStream) { this.mediaStream.getTracks().forEach(t => t.stop()); }
      if (this.sourceNode) { try { this.sourceNode.disconnect(); } catch {} }
      if (this.analyserNode) { try { this.analyserNode.disconnect(); } catch {} }
    } finally {
      this.sourceNode = null;
      this.mediaStream = null;
    }
  }

  // Dados para o loop (diagramas “Update Audio Data”)
  getFrequencyData() {
    if (!this.analyserNode) return null;
    this.analyserNode.getByteFrequencyData(this.frequencyData);
    return this.frequencyData;
  }
  getTimeData() {
    if (!this.analyserNode) return null;
    this.analyserNode.getByteTimeDomainData(this.waveformData);
    return this.waveformData;
  }
  getWaveformData() { return this.getTimeData(); }

  getLevel() {
    const arr = this.getFrequencyData();
    if (!arr) return 0;
    let s = 0; for (let i = 0; i < arr.length; i++) s += arr[i];
    return s / (arr.length * 255); // 0..1
  }
}
