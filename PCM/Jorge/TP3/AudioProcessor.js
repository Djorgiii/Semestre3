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

    this.mediaElementSource = null;
    this._currentObjectUrl = null;
  }

  _ensureCtx() {
    if (!this.audioContext) {
      this.audioContext = new (window.AudioContext ||
        window.webkitAudioContext)({ latencyHint: "interactive" });
      this.analyserNode = this.audioContext.createAnalyser();
      this.analyserNode.fftSize = 2048;
      this.analyserNode.smoothingTimeConstant = 0.7;

      this.frequencyData = new Uint8Array(this.analyserNode.frequencyBinCount);
      this.waveformData = new Uint8Array(this.analyserNode.fftSize);
    }
  }

  setSmoothing(v) {
    if (this.analyserNode)
      this.analyserNode.smoothingTimeConstant = Math.max(0, Math.min(0.95, v));
  }

  // --- Start Microphone (diagrama Captura de Microfone) ---
  async startMicrophone() {
    this._ensureCtx();
    await this.audioContext.resume();

    this.stop({ keepMediaElementSource: true }); // limpar anterior

    this.mediaStream = await navigator.mediaDevices.getUserMedia({
      audio: {
        echoCancellation: false,
        noiseSuppression: false,
        autoGainControl: false,
        channelCount: 1,
      },
    });
    const node = this.audioContext.createMediaStreamSource(this.mediaStream);
    node.connect(this.analyserNode);
    this.sourceNode = node;
  }

  // --- Load File (diagrama Carregamento de Ficheiro) ---
  async loadAudioFile(file, mode = "element") {
    this._ensureCtx();
    await this.audioContext.resume();

    this.stop({ keepMediaElementSource: true });

    if (mode === "element") {
      if (this._currentObjectUrl) {
        try {
          URL.revokeObjectURL(this._currentObjectUrl);
        } catch {}
        this._currentObjectUrl = null;
      }
      this._currentObjectUrl = URL.createObjectURL(file);
      this._audioEl.src = this._currentObjectUrl;

      if (!this.mediaElementSource) {
        this.mediaElementSource = this.audioContext.createMediaElementSource(
          this._audioEl
        );
      } else {
        try {
          this.mediaElementSource.disconnect();
        } catch {}
      }

      this.mediaElementSource.connect(this.analyserNode);
      this.mediaElementSource.connect(this.audioContext.destination);

      await this._audioEl.play();
      this.sourceNode = this.mediaElementSource;
      return;
    }

    // modo “buffer/decoder” (se quiseres seguir literalmente o losango do diagrama)
    const arrayBuffer = await file.arrayBuffer();
    const audioBuffer = await this.audioContext.decodeAudioData(arrayBuffer);
    const bufferSource = this.audioContext.createBufferSource();
    bufferSource.buffer = audioBuffer;
    bufferSource.loop = false;
    bufferSource.connect(this.analyserNode);
    bufferSource.connect(this.audioContext.destination); // se quiseres ouvir
    bufferSource.start();
    this.sourceNode = bufferSource;
  }

  stop(opts = {}) {
    const { keepMediaElementSource = true } = opts;

    try {
      if (this._audioEl) {
        this._audioEl.pause();
        this._audioEl.removeAttribute("src");
        this._audioEl.load();
      }

      if (this._currentObjectUrl) {
        try {
          URL.revokeObjectURL(this._currentObjectUrl);
        } catch {}
        this._currentObjectUrl = null;
      }

      if (this.mediaStream) {
        this.mediaStream.getTracks().forEach((t) => t.stop());
      }

      if (this.sourceNode) {
        try {
          this.sourceNode.disconnect();
        } catch {}
      }
      if (this.analyserNode) {
        try {
          this.analyserNode.disconnect();
        } catch {}
      }

      if (!keepMediaElementSource && this.mediaElementSource) {
        try {
          this.mediaElementSource.disconnect();
        } catch {}
      }
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
  getWaveformData() {
    return this.getTimeData();
  }

  getLevel() {
    const arr = this.getWaveformData();
    if (!arr || arr.length === 0) return 0;

    let sum = 0;
    for (let i = 0; i < arr.length; i++) {
      const v = (arr[i] - 128) / 128; // converte 0..255 → -1..1
      sum += v * v;
    }

    const rms = Math.sqrt(sum / arr.length); // 0..1

    // Escala perceptual (mais realista)
    return Math.min(1, rms * 1.4);
  }
}
