class AudioVisualization {
  constructor(canvas, audioProcessor) {
    if (this.constructor === AudioVisualization) {
      throw new Error("AudioVisualization é abstrata.");
    }
    this.canvas = canvas;
    this.ctx = canvas.getContext("2d");
    this.audioProcessor = audioProcessor;
    this.name = "Visualização";

    this.properties = { amount:128, smoothing:0.7, showGrid:false };
    this.testData = new Uint8Array(256);
    for (let i=0;i<this.testData.length;i++) this.testData[i] = Math.sin(i/10)*128+128;
    this.frameCount = 0;
    this._applyDPR();
  }

  update() {
    if (this.audioProcessor?.analyserNode) {
      this.audioProcessor.analyserNode.smoothingTimeConstant =
        Math.max(0, Math.min(0.95, this.properties.smoothing || 0));
    }
    this.frameCount++;
  }

  draw(){ throw new Error("draw() deve ser implementado"); }

  resize(w,h){ this._applyDPR(w,h); }
  getProperties(){ return { ...this.properties }; }
  updateProperty(k,v){ if (k in this.properties) this.properties[k] = v; }

  clearCanvas(){ this.ctx.clearRect(0,0,this.canvas.clientWidth,this.canvas.clientHeight); }
  drawGrid(){
    if (!this.properties.showGrid) return;
    const {ctx,canvas} = this; const w=canvas.clientWidth, h=canvas.clientHeight;
    ctx.save(); ctx.strokeStyle="rgba(255,255,255,0.06)"; ctx.lineWidth=1;
    const step = Math.max(16, Math.floor(Math.min(w,h)/16));
    for (let x=0;x<=w;x+=step){ ctx.beginPath(); ctx.moveTo(x,0); ctx.lineTo(x,h); ctx.stroke(); }
    for (let y=0;y<=h;y+=step){ ctx.beginPath(); ctx.moveTo(0,y); ctx.lineTo(w,y); ctx.stroke(); }
    ctx.restore();
  }

  normalizeData(){
    const freq = this.audioProcessor?.getFrequencyData?.() || this.testData;
    const time = this.audioProcessor?.getTimeData?.() || this.testData;
    let level = 0; if (freq && freq.length){ let s=0; for (let i=0;i<freq.length;i++) s+=freq[i]; level=s/(freq.length*255); }
    return { freq, time, level, amount: this.properties.amount|0 };
  }

  _applyDPR(w,h){
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const cw = w ?? this.canvas.clientWidth, ch = h ?? this.canvas.clientHeight;
    this.canvas.width = Math.max(1, Math.floor(cw * dpr));
    this.canvas.height = Math.max(1, Math.floor(ch * dpr));
    this.ctx.setTransform(dpr,0,0,dpr,0,0);
  }
}
