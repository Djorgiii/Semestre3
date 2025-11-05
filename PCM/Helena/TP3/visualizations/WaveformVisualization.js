import AudioVisualization from './AudioVisualization.js';

export default class WaveformVisualization extends AudioVisualization {
  render({ time, ctx, w, h }) {
    // grelha horizontal
    ctx.strokeStyle = this.grid;
    ctx.lineWidth = 1;
    ctx.beginPath();
    for (let i = 1; i < 4; i++) {
      const y = (h * i) / 4;
      ctx.moveTo(0, y); ctx.lineTo(w, y);
    }
    ctx.stroke();

    // sem dados ainda
    if (!time) return;

    // waveform
    ctx.strokeStyle = this.fg;
    ctx.lineWidth = 2;
    ctx.beginPath();

    const N = time.length;
    const step = Math.max(1, Math.floor(N / w)); // amostras por pixel
    const mid = h / 2;

    for (let x = 0, i = 0; x < w && i < N; x++, i += step) {
      const y = mid + (time[i] * h) / 2;
      if (x === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.stroke();
  }
}
