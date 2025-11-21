class AudioProcessor {
  constructor() {
    this.audioContext = null;
    this.analyserNode = null;
    this.sourceNode = null;
    this.mediaStream = null;

    this.frequencyData = null;
    this.waveformData = null;

    this.audioElement = new Audio();
    this.audioElement.crossOrigin = "anonymous";

    this.mediaElementSource = null;
    this.objectUrl = null;
  }

  _ensureContext() {
    if (this.audioContext) return;

    this.audioContext = new (window.AudioContext)({
      latencyHint: "interactive",
    });

    this.analyserNode = this.audioContext.createAnalyser();
    this.analyserNode.fftSize = 2048;
    this.analyserNode.smoothingTimeConstant = 0.7;

    this.frequencyData = new Uint8Array(this.analyserNode.frequencyBinCount);
    this.waveformData  = new Uint8Array(this.analyserNode.fftSize);
  }

  async startMicrophone() {
    this._ensureContext();
    await this.audioContext.resume();

    this.stop({ keepMediaElementSource: true });

    this.mediaStream = await navigator.mediaDevices.getUserMedia({
      audio: {
        echoCancellation: false,
        noiseSuppression: false,
        autoGainControl: false,
        channelCount: 1,
      },
    });

    const source = this.audioContext.createMediaStreamSource(this.mediaStream);

    source.connect(this.analyserNode);
    
    this.sourceNode = source;
  }

  async loadAudioFile(file) {
    this._ensureContext();
    await this.audioContext.resume();

    this.stop({ keepMediaElementSource: true });

    // Libertar URL antiga
    if (this.objectUrl) {
      try { URL.revokeObjectURL(this.objectUrl); } catch {}
      this.objectUrl = null;
    }

    this.objectUrl = URL.createObjectURL(file);
    this.audioElement.src = this.objectUrl;

    if (!this.mediaElementSource) {
      this.mediaElementSource = this.audioContext.createMediaElementSource(
        this.audioElement
      );
    } else {
      try { this.mediaElementSource.disconnect(); } catch {}
    }

    this.mediaElementSource.connect(this.analyserNode);
    this.mediaElementSource.connect(this.audioContext.destination);

    await this.audioElement.play();

    this.sourceNode = this.mediaElementSource;
  }

  stop(opts = {}) {
    const { keepMediaElementSource = true } = opts;

    try {
      if (this.audioElement) {
        this.audioElement.pause();
        this.audioElement.removeAttribute("src");
        this.audioElement.load();
      }

      // Libertar URL
      if (this.objectUrl) {
        try { URL.revokeObjectURL(this.objectUrl); } catch {}
        this.objectUrl = null;
      }

      // Parar microfone
      if (this.mediaStream) {
        this.mediaStream.getTracks().forEach(t => t.stop());
      }

      // Desligar source
      if (this.sourceNode) {
        try { this.sourceNode.disconnect(); } catch {}
      }

      if (this.analyserNode) {
        try { this.analyserNode.disconnect(); } catch {}
      }

      if (!keepMediaElementSource && this.mediaElementSource) {
        try { this.mediaElementSource.disconnect(); } catch {}
      }

    } finally {
      this.sourceNode  = null;
      this.mediaStream = null;
    }
  }

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

  // Volume RMS normalizado entre 0 e 1
  getLevel() {
    const data = this.getWaveformData();
    if (!data?.length) return 0;

    let sum = 0;
    for (let i = 0; i < data.length; i++) {
      const normalized = (data[i] - 128) / 128;
      sum += normalized * normalized;
    }
    const rms = Math.sqrt(sum / data.length);

    return Math.min(1, rms * 1.4);
  }
}
