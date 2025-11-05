class ParticleVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);
    this.name = "Partículas";
    this.particles = [];
    this.lastTime = 0;

    // propriedades específicas desta visualização (sem quebrar as da base)
    this.properties = {
      ...this.properties,     // (amount, smoothing, showGrid) da base
      count: 120,             // nº de partículas
      lineDistance: 100,      // distância máx. para ligar linhas
      maxSpeedBase: 2,        // velocidade base
      audioSpeedBoost: 3,     // boost máximo pelo nível de áudio
      sizeMin: 1.5,
      sizeMax: 3.5,
      fadeTrail: 0.06,        // 0 = sem rasto; 0.06 ~ suave
      color: "#4aa3ff"
    };

    this.initParticles();
  }

  // desenhar partículas + conexões
  draw() {
    // fundo com rasto suave
    const w = this.canvas.clientWidth, h = this.canvas.clientHeight;
    const f = Math.max(0, Math.min(0.2, this.properties.fadeTrail));
    if (f > 0) {
      this.ctx.fillStyle = `rgba(234,242,255,${f})`;
      this.ctx.fillRect(0, 0, w, h);
    } else {
      this.clearCanvas();
    }

    if (this.properties.showGrid) this.drawGrid();

    this.drawParticles();
    this.drawConnections();
  }

  // atualizar posições/velocidades
  update() {
    super.update();
    // se o utilizador mudou "amount" ou "count" externamente, ajusta
    const target = this.properties.count || this.properties.amount || 120;
    if (this.particles.length !== target) this._resizeCount(target);
    this.updateParticles();
  }

  // propriedades expostas
  getProperties() {
    return { ...super.getProperties(), ...this.properties };
  }

  // criar partículas
  initParticles() {
    const w = this.canvas.clientWidth, h = this.canvas.clientHeight;
    const count = this.properties.count || 120;

    this.particles.length = 0;
    for (let i = 0; i < count; i++) {
      this.particles.push(this._makeParticle(w, h));
    }
  }

  _makeParticle(w, h) {
    const { sizeMin, sizeMax, maxSpeedBase } = this.properties;
    const r = sizeMin + Math.random() * (sizeMax - sizeMin);
    const ang = Math.random() * Math.PI * 2;
    const sp = (Math.random() * 0.7 + 0.3) * maxSpeedBase; // pequena variação
    return {
      x: Math.random() * w,
      y: Math.random() * h,
      vx: Math.cos(ang) * sp,
      vy: Math.sin(ang) * sp,
      radius: r,
      color: this.properties.color
    };
    // se quiseres colorir por HSL aleatório:
    // color: `hsl(${Math.random()*360},100%,60%)`
  }

  _resizeCount(target) {
    const w = this.canvas.clientWidth, h = this.canvas.clientHeight;
    if (this.particles.length < target) {
      while (this.particles.length < target) this.particles.push(this._makeParticle(w, h));
    } else {
      this.particles.length = target;
    }
  }

  updateParticles() {
    const { freq, level } = this.normalizeData(); // da base
    const L = level || 0;                          // 0..1
    const w = this.canvas.clientWidth, h = this.canvas.clientHeight;
    const maxSpeed = (this.properties.maxSpeedBase || 2) + L * (this.properties.audioSpeedBoost || 3);

    for (let i = 0; i < this.particles.length; i++) {
      const p = this.particles[i];

      // influência do áudio por banda de frequência (simples)
      if (freq && freq.length) {
        const idx = Math.floor((i / this.particles.length) * freq.length);
        const intensity = (freq[idx] || 0) / 255; // 0..1
        // jitter leve em função da intensidade
        p.vx += (Math.random() - 0.5) * intensity * 0.5;
        p.vy += (Math.random() - 0.5) * intensity * 0.5;
      }

      // limitar velocidade
      const speed = Math.hypot(p.vx, p.vy) || 0.0001;
      if (speed > maxSpeed) {
        p.vx = (p.vx / speed) * maxSpeed;
        p.vy = (p.vy / speed) * maxSpeed;
      }

      // mover
      p.x += p.vx;
      p.y += p.vy;

      // wrap (entra do outro lado) para fluxo contínuo
      if (p.x < 0) p.x = w; else if (p.x > w) p.x = 0;
      if (p.y < 0) p.y = h; else if (p.y > h) p.y = 0;
    }
  }

  drawParticles() {
    const fill = this.properties.color || "#4aa3ff";
    this.ctx.fillStyle = fill;
    for (const p of this.particles) {
      this.ctx.beginPath();
      this.ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      this.ctx.fill();
    }
  }

  drawConnections() {
    const maxD = this.properties.lineDistance || 100;
    const alphaScale = 0.5; // opacidade máxima das linhas

    this.ctx.lineWidth = 1;
    for (let i = 0; i < this.particles.length; i++) {
      const a = this.particles[i];
      for (let j = i + 1; j < this.particles.length; j++) {
        const b = this.particles[j];
        const dx = a.x - b.x, dy = a.y - b.y;
        const d2 = dx * dx + dy * dy;
        if (d2 < maxD * maxD) {
          const d = Math.sqrt(d2);
          const op = (1 - d / maxD) * alphaScale;
          this.ctx.strokeStyle = `rgba(74,163,255,${op})`;
          this.ctx.beginPath();
          this.ctx.moveTo(a.x, a.y);
          this.ctx.lineTo(b.x, b.y);
          this.ctx.stroke();
        }
      }
    }
  }

  // garante que o redimensionamento usa clientWidth/Height (CSS px) e mantém partículas no ecrã
  resize(width, height) {
    super.resize(width, height);
    // opcional: re-spawn suave mantendo distribuição
    // aqui não mexemos para manter simples
  }
}
