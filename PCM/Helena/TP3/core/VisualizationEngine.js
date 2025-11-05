// Controla instância de visualização atual e o ciclo de vida
export default class VisualizationEngine {
  constructor(canvas, registry) {
    this.canvas = canvas;
    this.registry = registry; // { key: Class }
    this.instance = null;
    this._onResize = () => this.instance?.resize();
    window.addEventListener('resize', this._onResize);
  }

  mount(key, processor) {
    this.unmount();
    const VizClass = this.registry[key];
    if (!VizClass) throw new Error(`Visualização desconhecida: ${key}`);
    this.instance = new VizClass(this.canvas);
    this.instance.init(processor);
    this.instance.start();
  }

  unmount() {
    if (this.instance) {
      this.instance.destroy();
      this.instance = null;
    }
  }

  dispose() {
    window.removeEventListener('resize', this._onResize);
    this.unmount();
  }
}
