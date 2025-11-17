class OscilloscopeVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);
    this.name = "Osciloscópio";

    this.properties = {
      ...this.properties,
      thickness: 1.5,
      scale: 1.0,
      showGrid: true,
    };
  }

  getProperties() {
    const mergedProps = { ...super.getProperties(), ...this.properties };
    delete mergedProps.amount;
    return mergedProps;
  }

  draw() {
    this.update();

    const canvasWidth  = this.canvas.clientWidth;
    const canvasHeight = this.canvas.clientHeight;

    this.clearCanvas();
    if (this.properties.showGrid) {
      this.drawGrid();
    }

    const { time: timeDomainData } = this.normalizeData();
    if (!timeDomainData?.length) return;

    const ctx = this.ctx;
    const centerY = canvasHeight / 2;
    const sampleCount = timeDomainData.length;
    const stepX = canvasWidth / sampleCount;
    const scale = Math.max(0, Math.min(1, this.properties.scale || 1));

    ctx.strokeStyle = "rgba(0, 255, 100, 0.4)";
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(0, centerY);
    ctx.lineTo(canvasWidth, centerY);
    ctx.stroke();

    ctx.strokeStyle = "#00ff99";
    ctx.lineWidth = this.properties.thickness || 1.5;
    ctx.beginPath();

    for (let i = 0; i < sampleCount; i++) {
      const normalizedSample = (timeDomainData[i] - 128) / 128; // -1..1
      const x = i * stepX;
      const y = centerY + normalizedSample * (canvasHeight / 2 - 10) * scale;

      if (i === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    }

    ctx.stroke();
  }
}
