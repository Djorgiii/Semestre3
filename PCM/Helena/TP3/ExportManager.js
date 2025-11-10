// core/ExportManager.js
class ExportManager {
  constructor(visualizationEngine) { this.ve = visualizationEngine; }

  exportAsPNG(filename = `audio-visualization-${Date.now()}.png`) {
    const canvas = this.ve.canvas;
    canvas.toBlob((blob) => {
      if (!blob) return;
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob); a.download = filename;
      document.body.appendChild(a); a.click(); a.remove();
      setTimeout(() => URL.revokeObjectURL(a.href), 400);
    }, 'image/png');
  }

  exportAsJPEG(quality = 0.92, filename = `audio-visualization-${Date.now()}.jpg`) {
    const canvas = this.ve.canvas;
    canvas.toBlob((blob) => {
      if (!blob) return;
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob); a.download = filename;
      document.body.appendChild(a); a.click(); a.remove();
      setTimeout(() => URL.revokeObjectURL(a.href), 400);
    }, 'image/jpeg', quality);
  }
}
window.ExportManager = ExportManager;
