class WaveformVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);
    this.name = "Forma de Onda";

    this.properties = {
      ...this.properties,
      thickness: 2,
      scale: 1.0,
      fadeTrail: 0,
      color: "#e8f0ff",
    };
  }

  draw() {
    this.update();
    
    const canvasWidth  = this.canvas.clientWidth;
    const canvasHeight = this.canvas.clientHeight;
    const fadeAmount = Math.max(
      0,
      Math.min(0.2, this.properties.fadeTrail || 0)
    );

    // Fundo com rasto escuro, ou limpa completamente
    if (fadeAmount > 0) {
      this.ctx.fillStyle = `rgba(5, 20, 50, ${fadeAmount})`;
      this.ctx.fillRect(0, 0, canvasWidth, canvasHeight);
    } else {
      this.clearCanvas();
    }

    if (this.properties.showGrid) {
      this.drawGrid();
    }

    const { time: timeDomainData } = this.normalizeData();
    if (!timeDomainData?.length) return;

    const ctx = this.ctx;
    const centerY = canvasHeight / 2;
    const stepX = canvasWidth / timeDomainData.length;
    const scale = Math.max(0, Math.min(1, this.properties.scale || 1));

    ctx.beginPath();
    ctx.lineWidth = Math.max(1, this.properties.thickness | 0);
    ctx.strokeStyle = this.properties.color;

    for (let i = 0; i < timeDomainData.length; i++) {
      const normalizedSample = (timeDomainData[i] - 128) / 128; // -1..1
      const x = i * stepX;
      const y =
        centerY + normalizedSample * (canvasHeight / 2 - 10) * scale;

      if (i === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    }

    ctx.stroke();
  }
}
