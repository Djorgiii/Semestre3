class UIManager {
  constructor(app){
    this.app = app;
    this._cacheEls();
    this.setupEventListeners();
    this.setupAudioLevels();
  }

  _cacheEls(){
    this.$status = document.getElementById("audioStatus");
    this.$level  = document.getElementById("audioLevel");
    this.$props  = document.getElementById("properties-container");
    this.$btnMic = document.getElementById("startMic");
    this.$btnStop= document.getElementById("stopAudio");
    this.$file   = document.getElementById("audioFile");
    this.$sel    = document.getElementById("visualizationType");
    this.$png    = document.getElementById("exportPNG");
    this.$jpg    = document.getElementById("exportJPEG");
  }

  updateAudioInfo(info, isError=false){
    if (!this.$status || !this.$level) return;
    if (isError){
      this.$status.textContent = `Erro: ${info}`;
      this.$status.style.color = "#f72585";
    } else {
      this.$status.textContent = `Áudio: ${info.status || "Ativo"}`;
      this.$status.style.color = "#e6e6e6";
      const lvl = typeof info.level === "number" ? info.level : 0;
      this.$level.textContent = `Nível: ${Math.round(lvl)}%`;
    }
  }

  setButtonStates(playing){
    if (this.$btnMic)  this.$btnMic.disabled  = playing;
    if (this.$btnStop) this.$btnStop.disabled = !playing;
  }

  showError(message){
    const modal = document.getElementById("errorModal");
    const msg   = document.getElementById("errorMessage");
    const close = document.querySelector(".close");
    if (!modal || !msg || !close) return alert(message);
    msg.textContent = message; modal.classList.remove("hidden");
    close.onclick = () => modal.classList.add("hidden");
    const onWin = (e)=>{ if (e.target===modal){ modal.classList.add("hidden"); window.removeEventListener("click", onWin);} };
    window.addEventListener("click", onWin);
  }

  setupEventListeners(){
    this.$btnMic?.addEventListener("click", () => this.app.startMicrophone());
    this.$btnStop?.addEventListener("click", () => this.app.stopAudio());
    this.$file?.addEventListener("change", e => { const f=e.target.files?.[0]; if (f) this.app.loadAudioFile(f); });
    this.$sel?.addEventListener("change", e => this.app.setVisualization(e.target.value));
    this.$png?.addEventListener("click", () => this.app.exportManager.exportAsPNG());
    this.$jpg?.addEventListener("click", () => this.app.exportManager.exportAsJPEG(0.9));
  }

  setupAudioLevels(){
    const loop = () => {
      const lvl01 = this.app.audioProcessor?.getLevel?.() || 0;
      this.updateAudioInfo({ status: "Ativo", level: Math.round(lvl01*100) }, false);
      requestAnimationFrame(loop);
    };
    requestAnimationFrame(loop);
  }

  // painel de propriedades (se precisares na semana 2)
  updatePropertiesPanel(){
    const eng = this.app.visualizationEngine;
    const props = eng.getVisualizationProperties();
    if (!this.$props) return;
    this.$props.innerHTML = "";
    for (const [k,v] of Object.entries(props)){
      if (typeof v === "boolean"){
        const row=document.createElement("div"); const label=document.createElement("label"); const input=document.createElement("input");
        row.className="property-control"; input.type="checkbox"; input.checked=v; label.textContent=k;
        input.oninput = e => eng.updateVisualizationProperty(k, !!e.target.checked);
        row.append(label,input); this.$props.append(row);
      } else if (typeof v === "number"){
        const row=document.createElement("div"); row.className="property-control";
        const label=document.createElement("label"); label.textContent=k;
        const input=document.createElement("input"); input.type="range";
        // heurísticas simples
        const meta = (()=>{
          if (k==="smoothing") return {min:0,max:0.95,step:0.01};
          if (k==="amount") return {min:1,max:512,step:1};
          if (k==="barSpacing") return {min:0,max:6,step:1};
          return {min:0,max:Math.max(v*2||1,1),step:0.01};
        })();
        input.min=meta.min; input.max=meta.max; input.step=meta.step; input.value=v;
        input.oninput = e => eng.updateVisualizationProperty(k, parseFloat(e.target.value));
        row.append(label,input); this.$props.append(row);
      }
    }
  }
}
