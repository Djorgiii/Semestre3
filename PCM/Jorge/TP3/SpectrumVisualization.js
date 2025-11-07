class SpectrumVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor){
    super(canvas, audioProcessor);
    this.name="Espectro de Frequências";
    this.properties = { ...this.properties, barSpacing:1, useGradient:true, color:"#4aa3ff" };
  }
  draw(){
    this.update(); this.clearCanvas(); if (this.properties.showGrid) this.drawGrid();
    const { freq, amount } = this.normalizeData(); if (!freq?.length) return;

    const ctx=this.ctx, w=this.canvas.clientWidth, h=this.canvas.clientHeight;
    const bars=Math.min(amount, freq.length), step=Math.max(1, Math.floor(freq.length/bars));
    const barW=Math.max(1, (w/bars)-this.properties.barSpacing);

    let fillStyle;
    if (this.properties.useGradient){
      const g=ctx.createLinearGradient(0,0,0,h); g.addColorStop(0,this.properties.color); g.addColorStop(1,"#ffffff");
      fillStyle=g;
    }
    for (let i=0;i<bars;i++){
      const v=freq[i*step]/255, barH=v*(h-5), x=i*(barW+this.properties.barSpacing), y=h-barH;
      ctx.fillStyle = this.properties.useGradient ? fillStyle : `hsl(${(i/bars)*360},100%,50%)`;
      ctx.fillRect(x,y,barW,barH);
    }
  }
}