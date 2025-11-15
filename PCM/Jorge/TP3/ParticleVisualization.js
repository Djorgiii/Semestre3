class ParticleVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);
    this.name = "Partículas";

    this.particles = [];
    this._level    = 0;

    this.properties = {
      ...this.properties,
      amount:            120,
      lineDistance:      100,
      maxSpeedBase:      2,
      audioSpeedBoost:   3,
      sizeMin:           1.5,
      sizeMax:           3.5,
      fadeTrail:         0.06,
      color:             "#4aa3ff",
      activeThreshold:   0.02,
      idleShowConnections: false,
    };

    this.initParticles();
  }

  draw() {
    const w = this.canvas.clientWidth;
    const h = this.canvas.clientHeight;

    const isIdle = (this._level || 0) < this.properties.activeThreshold;
    const f = isIdle ? 0 : Math.max(0, Math.min(0.2, this.properties.fadeTrail));

    if (f > 0) {
      this.ctx.fillStyle = `rgba(5, 20, 50, ${f})`;
      this.ctx.fillRect(0, 0, w, h);
    } else {
      this.clearCanvas();
    }

    if (this.properties.showGrid) this.drawGrid();

    this.drawParticles();
    if (!isIdle || this.properties.idleShowConnections) {
      this.drawConnections();
    }
  }

  update() {
    super.update();

    const { level } = this.normalizeData();
    this._level = level || 0;

    let target = parseInt(this.properties.amount ?? 120, 10);
    if (!Number.isFinite(target) || target < 1) target = 1;
    if (target > 200) target = 200;

    if (this.particles.length !== target) this._resizeAmount(target);
    if (this._level < this.properties.activeThreshold) return;

    this.updateParticles();
  }

  getProperties() {
    return { ...super.getProperties(), ...this.properties };
  }

  initParticles() {
    const w = this.canvas.clientWidth;
    const h = this.canvas.clientHeight;

    const count = Math.max(
      1,
      Math.min(200, parseInt(this.properties.amount || 120, 10) || 120)
    );

    this.particles.length = 0;
    for (let i = 0; i < count; i++) {
      this.particles.push(this._makeParticle(w, h));
    }
  }

  _makeParticle(w, h) {
    const { sizeMin, sizeMax, maxSpeedBase } = this.properties;
    const r   = sizeMin + Math.random() * (sizeMax - sizeMin);
    const ang = Math.random() * Math.PI * 2;
    const sp  = (Math.random() * 0.7 + 0.3) * maxSpeedBase;

    return {
      x: Math.random() * w,
      y: Math.random() * h,
      vx: Math.cos(ang) * sp,
      vy: Math.sin(ang) * sp,
      radius: r,
    };
  }

  _resizeAmount(target) {
    target |= 0;
    if (!Number.isFinite(target) || target < 1) target = 1;
    if (target > 200) target = 200;

    const w = this.canvas.clientWidth;
    const h = this.canvas.clientHeight;

    if (this.particles.length < target) {
      const need = target - this.particles.length;
      for (let k = 0; k < need; k++) {
        this.particles.push(this._makeParticle(w, h));
      }
    } else {
      this.particles.length = target;
    }
  }

  updateParticles() {
    const { freq, level } = this.normalizeData();
    const L  = level || 0;
    const w  = this.canvas.clientWidth;
    const h  = this.canvas.clientHeight;
    const maxSpeed =
      (this.properties.maxSpeedBase || 2) +
      L * (this.properties.audioSpeedBoost || 3);

    for (let i = 0; i < this.particles.length; i++) {
      const p = this.particles[i];

      if (freq?.length) {
        const idx = Math.floor((i / this.particles.length) * freq.length);
        const intensity = (freq[idx] || 0) / 255;
        p.vx += (Math.random() - 0.5) * intensity * 0.5;
        p.vy += (Math.random() - 0.5) * intensity * 0.5;
      }

      const speed = Math.hypot(p.vx, p.vy) || 0.0001;
      if (speed > maxSpeed) {
        p.vx = (p.vx / speed) * maxSpeed;
        p.vy = (p.vy / speed) * maxSpeed;
      }

      p.x += p.vx;
      p.y += p.vy;

      if (p.x < 0)      p.x = w;
      else if (p.x > w) p.x = 0;
      if (p.y < 0)      p.y = h;
      else if (p.y > h) p.y = 0;
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
    const n = this.particles.length;
    if (!n) return;

    const maxD   = this.properties.lineDistance || 100;
    const maxD2  = maxD * maxD;
    const alphaScale = 0.5;

    const MAX_LINKS = 6000;
    const K = Math.max(1, Math.floor(MAX_LINKS / n));

    this.ctx.lineWidth = 1;

    for (let i = 0; i < n; i++) {
      const a   = this.particles[i];
      const end = Math.min(n, i + 1 + K);

      for (let j = i + 1; j < end; j++) {
        const b = this.particles[j];
        const dx = a.x - b.x;
        const dy = a.y - b.y;
        const d2 = dx * dx + dy * dy;

        if (d2 < maxD2) {
          const d  = Math.sqrt(d2);
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

  resize(w, h) {
    super.resize(w, h);
  }
}
