class ParticleVisualization extends AudioVisualization {
  constructor(canvas, audioProcessor) {
    super(canvas, audioProcessor);
    this.name = "Partículas";

    this.particles = [];
    this.audioLevel = 0;

    this.properties = {
      ...this.properties,
      amount: 120,              
      lineDistance: 100,        
      maxSpeedBase: 2,          
      audioSpeedBoost: 3,       
      sizeMin: 1.5,
      sizeMax: 3.5,
      fadeTrail: 0.06,          
      color: "#4aa3ff",
      activeThreshold: 0.02,    
      idleShowConnections: false,
    };

    this.initParticles();
  }

  draw() {
    this.update();

    const canvasWidth  = this.canvas.clientWidth;
    const canvasHeight = this.canvas.clientHeight;

    const isIdle = (this.audioLevel || 0) < this.properties.activeThreshold;
    const fadeAmount = isIdle
      ? 0
      : Math.max(0, Math.min(0.2, this.properties.fadeTrail));

    if (fadeAmount > 0) {
      this.ctx.fillStyle = `rgba(5, 20, 50, ${fadeAmount})`;
      this.ctx.fillRect(0, 0, canvasWidth, canvasHeight);
    } else {
      this.clearCanvas();
    }

    if (this.properties.showGrid) {
      this.drawGrid();
    }

    this.drawParticles();

    if (!isIdle || this.properties.idleShowConnections) {
      this.drawConnections();
    }
  }


  update() {
    super.update();

    const { level } = this.normalizeData();
    this.audioLevel = level || 0;

    let targetCount = parseInt(this.properties.amount ?? 120, 10);
    if (!Number.isFinite(targetCount) || targetCount < 1) targetCount = 1;
    if (targetCount > 200) targetCount = 200;

    if (this.particles.length !== targetCount) {
      this.resizeParticleCount(targetCount);
    }

    if (this.audioLevel < this.properties.activeThreshold) {
      return;
    }

    this.updateParticles();
  }

  getProperties() {
    return { ...super.getProperties(), ...this.properties };
  }

  initParticles() {
    const canvasWidth  = this.canvas.clientWidth;
    const canvasHeight = this.canvas.clientHeight;

    const amountRaw = parseInt(this.properties.amount || 120, 10) || 120;
    const particleCount = Math.max(1, Math.min(200, amountRaw));

    this.particles.length = 0;

    for (let i = 0; i < particleCount; i++) {
      this.particles.push(this.createRandomParticle(canvasWidth, canvasHeight));
    }
  }

  createRandomParticle(canvasWidth, canvasHeight) {
    const { sizeMin, sizeMax, maxSpeedBase } = this.properties;

    const radius = sizeMin + Math.random() * (sizeMax - sizeMin);
    const angle = Math.random() * Math.PI * 2;
    const speed = (Math.random() * 0.7 + 0.3) * maxSpeedBase;

    return {
      x: Math.random() * canvasWidth,
      y: Math.random() * canvasHeight,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      radius,
    };
  }

  resizeParticleCount(targetCount) {
    targetCount |= 0;
    if (!Number.isFinite(targetCount) || targetCount < 1) targetCount = 1;
    if (targetCount > 200) targetCount = 200;

    const canvasWidth  = this.canvas.clientWidth;
    const canvasHeight = this.canvas.clientHeight;

    if (this.particles.length < targetCount) {
      const particlesToAdd = targetCount - this.particles.length;
      for (let i = 0; i < particlesToAdd; i++) {
        this.particles.push(this.createRandomParticle(canvasWidth, canvasHeight));
      }
    } else {
      this.particles.length = targetCount;
    }
  }

  updateParticles() {
    const { freq: frequencyData, level } = this.normalizeData();
    const audioLevel = level || 0;

    const canvasWidth  = this.canvas.clientWidth;
    const canvasHeight = this.canvas.clientHeight;

    const maxSpeed =
      (this.properties.maxSpeedBase || 2) +
      audioLevel * (this.properties.audioSpeedBoost || 3);

    for (let i = 0; i < this.particles.length; i++) {
      const particle = this.particles[i];

      if (frequencyData?.length) {
        const bandIndex = Math.floor((i / this.particles.length) * frequencyData.length);
        const bandValue = frequencyData[bandIndex] || 0;
        const intensity = bandValue / 255;

        particle.vx += (Math.random() - 0.5) * intensity * 0.5;
        particle.vy += (Math.random() - 0.5) * intensity * 0.5;
      }

      const speed = Math.hypot(particle.vx, particle.vy) || 0.0001;
      if (speed > maxSpeed) {
        const scale = maxSpeed / speed;
        particle.vx *= scale;
        particle.vy *= scale;
      }

      particle.x += particle.vx;
      particle.y += particle.vy;

      if (particle.x < 0)          particle.x = canvasWidth;
      else if (particle.x > canvasWidth) particle.x = 0;

      if (particle.y < 0)          particle.y = canvasHeight;
      else if (particle.y > canvasHeight) particle.y = 0;
    }
  }

  drawParticles() {
    const fillColor = this.properties.color || "#4aa3ff";
    this.ctx.fillStyle = fillColor;

    for (const particle of this.particles) {
      this.ctx.beginPath();
      this.ctx.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2);
      this.ctx.fill();
    }
  }

  drawConnections() {
    const totalParticles = this.particles.length;
    if (!totalParticles) return;

    const maxDistance = this.properties.lineDistance || 100;
    const maxDistanceSquared = maxDistance * maxDistance;
    const alphaScale = 0.5;

    const MAX_LINKS = 6000;
    const maxNeighborsPerParticle = Math.max(
      1,
      Math.floor(MAX_LINKS / totalParticles)
    );

    this.ctx.lineWidth = 1;

    for (let i = 0; i < totalParticles; i++) {
      const a = this.particles[i];
      const endIndex = Math.min(totalParticles, i + 1 + maxNeighborsPerParticle);

      for (let j = i + 1; j < endIndex; j++) {
        const b = this.particles[j];

        const dx = a.x - b.x;
        const dy = a.y - b.y;
        const distanceSquared = dx * dx + dy * dy;

        if (distanceSquared < maxDistanceSquared) {
          const distance = Math.sqrt(distanceSquared);
          const opacity = (1 - distance / maxDistance) * alphaScale;

          this.ctx.strokeStyle = `rgba(74,163,255,${opacity})`;
          this.ctx.beginPath();
          this.ctx.moveTo(a.x, a.y);
          this.ctx.lineTo(b.x, b.y);
          this.ctx.stroke();
        }
      }
    }
  }
}
