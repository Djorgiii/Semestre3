class ExportManager {
  constructor(visualizationEngine){ 
    this.visualizationEngine = visualizationEngine; 
  }

  _export(type="image/png", quality=0.92){
    const canvas = this.visualizationEngine.canvas;
    let dataURL;
    if (type==="image/jpeg"){
      const w=canvas.width,h=canvas.height, tmp=document.createElement("canvas"), t=tmp.getContext("2d");
      tmp.width=w; tmp.height=h; t.fillStyle="#fff"; t.fillRect(0,0,w,h); t.drawImage(canvas,0,0);
      dataURL = tmp.toDataURL("image/jpeg", quality);
    } else {
      dataURL = canvas.toDataURL(type, quality);
    }
    const a=document.createElement("a");
    const ts = new Date().toISOString().replace(/[:.]/g,"-");
    a.download = `audio-visualization-${ts}.${type==="image/jpeg"?"jpg":"png"}`;
    a.href = dataURL; a.click();
  }

  exportAsPNG(){ 
    this._export("image/png"); 
  }
  exportAsJPEG(q=0.9){ 
    this._export("image/jpeg", q); 
  }
}
