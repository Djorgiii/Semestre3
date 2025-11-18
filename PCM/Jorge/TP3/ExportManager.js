class ExportManager {

  constructor(visualizationEngine) {
    this.engine = visualizationEngine;
  }

  _exportImage(format = "image/png", quality = 0.92) {
    const canvas = this.engine.canvas;
    let finalDataURL;

    if (format === "image/jpeg") {
      const width  = canvas.width;
      const height = canvas.height;

      const tempCanvas = document.createElement("canvas");
      const tempCtx    = tempCanvas.getContext("2d");

      tempCanvas.width  = width;
      tempCanvas.height = height;

      tempCtx.fillStyle = "#ffffff"; 
      tempCtx.fillRect(0, 0, width, height);
      tempCtx.drawImage(canvas, 0, 0);

      finalDataURL = tempCanvas.toDataURL("image/jpeg", quality);

    } else {
      finalDataURL = canvas.toDataURL(format, quality);
    }

    const link = document.createElement("a");
    const timestamp = new Date().toISOString().replace(/[:.]/g, "-");

    link.download = `audio-visualizer-${timestamp}.${format === "image/jpeg" ? "jpg" : "png"}`;
    link.href = finalDataURL;
    link.click();
  }

  exportAsPNG() {
    this._exportImage("image/png");
  }

  exportAsJPEG(quality = 0.9) {
    this._exportImage("image/jpeg", quality);
  }
}
