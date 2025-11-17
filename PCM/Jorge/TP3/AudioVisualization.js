class AudioVisualization {
  constructor(canvas, audioProcessor) {
    if (new.target === AudioVisualization) {
      throw new Error("AudioVisualization é abstrata.");
    }

    this.canvas = canvas;
    this.ctx = canvas.getContext("2d");
    this.audioProcessor = audioProcessor;
    this.name = "Visualização";

    this.properties = {
      amount: 128,
      smoothing: 0.7,
      showGrid: false,
    };

    this.testData = new Uint8Array(256);
    for (let i = 0; i < this.testData.length; i++) {
      this.testData[i] = Math.sin(i / 10) * 128 + 128;
    }

    this.frameCount = 0;
    this._applyDPR();
  }

  update() {
    const smoothing = this.properties.smoothing ?? 0.7;

    if (this.audioProcessor?.analyserNode) {
      this.audioProcessor.analyserNode.smoothingTimeConstant =
        Math.min(0.95, Math.max(0, smoothing));
    }

    this.frameCount++;
  }

  draw() {
    throw new Error("draw() deve ser implementado nas subclasses");
  }

  resize(width, height) {
    this._applyDPR(width, height);
  }

  getProperties() {
    return { ...this.properties };
  }

  updateProperty(propertyName, value) {
    if (propertyName in this.properties) {
      this.properties[propertyName] = value;
    }
  }

  clearCanvas() {
    this.ctx.clearRect(
      0,
      0,
      this.canvas.clientWidth,
      this.canvas.clientHeight
    );
  }

  drawGrid() {
    if (!this.properties.showGrid) return;

    const { ctx, canvas } = this;
    const canvasWidth = canvas.clientWidth;
    const canvasHeight = canvas.clientHeight;

    ctx.save();
    ctx.strokeStyle = "rgba(255,255,255,0.06)";
    ctx.lineWidth = 1;

    const step = Math.max(
      16,
      Math.floor(Math.min(canvasWidth, canvasHeight) / 16)
    );

    for (let x = 0; x <= canvasWidth; x += step) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, canvasHeight);
      ctx.stroke();
    }

    for (let y = 0; y <= canvasHeight; y += step) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(canvasWidth, y);
      ctx.stroke();
    }

    ctx.restore();
  }

  normalizeData() {
    const frequencyData =
      this.audioProcessor?.getFrequencyData?.() || this.testData;
    const timeDomainData =
      this.audioProcessor?.getTimeData?.() || this.testData;

    let level = 0;

    if (frequencyData?.length) {
      let sum = 0;
      for (let i = 0; i < frequencyData.length; i++) {
        sum += frequencyData[i];
      }
      level = sum / (frequencyData.length * 255);
    }

    return {
      freq: frequencyData,
      time: timeDomainData,
      level,
      amount: this.properties.amount | 0,
    };
  }

  _applyDPR(width, height) {
    const devicePixelRatio = Math.min(window.devicePixelRatio || 1, 2);
    const cssWidth = width ?? this.canvas.clientWidth;
    const cssHeight = height ?? this.canvas.clientHeight;

    this.canvas.width = Math.max(
      1,
      Math.floor(cssWidth * devicePixelRatio)
    );
    this.canvas.height = Math.max(
      1,
      Math.floor(cssHeight * devicePixelRatio)
    );

    this.ctx.setTransform(
      devicePixelRatio,
      0,
      0,
      devicePixelRatio,
      0,
      0
    );
  }
}
