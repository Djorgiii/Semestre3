import AudioProcessor from './core/AudioProcessor.js';
import VisualizationEngine from './core/VisualizationEngine.js';
import ExportManager from './core/ExportManager.js';

import WaveformVisualization from './visualizations/WaveformVisualization.js';

const $ = (sel) => document.querySelector(sel);

const canvas = $('#viz');
const btnStart = $('#btnStart');
const btnStop = $('#btnStop');
const gain = $('#gain');
const vizSelect = $('#vizSelect');
const btnSnap = $('#btnSnap');
const sr = $('#sr');

const processor = new AudioProcessor({ fftSize: 2048 });
const engine = new VisualizationEngine(canvas, {
  waveform: WaveformVisualization,
});

function updateButtons(running) {
  btnStart.disabled = running;
  btnStop.disabled = !running;
}

async function start() {
  await processor.start();
  sr.textContent = processor.getSampleRate().toFixed(0);
  engine.mount(vizSelect.value, processor);
  updateButtons(true);
}

async function stop() {
  engine.unmount();
  await processor.stop();
  sr.textContent = '—';
  updateButtons(false);
}

btnStart.addEventListener('click', () => start().catch(console.error));
btnStop.addEventListener('click', () => stop().catch(console.error));
gain.addEventListener('input', (e) => processor.setGain(parseFloat(e.target.value)));
vizSelect.addEventListener('change', () => {
  if (processor.ctx) engine.mount(vizSelect.value, processor);
});

btnSnap.addEventListener('click', () => {
  ExportManager.exportCanvasPNG(canvas, `snapshot-${vizSelect.value}.png`);
});

// Ajuste inicial do canvas ao layout
const resizeCanvasToFill = () => {
  const parent = canvas.parentElement;
  canvas.style.width = '100%';
  canvas.style.height = '100%';
  // o método resize da visualização vai ajustar o buffer
};
window.addEventListener('resize', resizeCanvasToFill);
resizeCanvasToFill();

// Boa prática: parar áudio ao sair
window.addEventListener('beforeunload', () => {
  engine.dispose();
  processor.stop();
});
