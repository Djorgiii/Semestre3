// Classe principal da aplicação
class App {
    constructor() {
        this.audioProcessor = new AudioProcessor();
        this.visualizationEngine = new VisualizationEngine('audioCanvas');
        this.uiManager = new UIManager(this);
        this.exportManager = new ExportManager(this.visualizationEngine);
        
        // Inicialização
        this.init();
    }
    
    init() {
        // TODO: inicializar a aplicação
        console.log('App inicializada');
    }
    
    startMicrophone() {
        // TODO: iniciar captura do microfone
        console.log('Iniciando microfone...');
    }
    
    loadAudioFile(file) {
        // TODO: carregar ficheiro de áudio
        console.log('Carregando ficheiro de áudio...');
    }
    
    stopAudio() {
        // TODO: parar áudio
        console.log('Parando áudio...');
    }
    
    setVisualization(type) {
        // TODO: definir tipo de visualização
        console.log(`Definindo visualização: ${type}`);
    }
    
    exportFrame() {
        // TODO: exportar frame atual
        console.log('Exportando frame...');
    }
    
    destroy() {
        // TODO: limpar recursos
        console.log('Destruindo aplicação...');
    }
}

// Processamento de Áudio
class AudioProcessor {
    constructor() {
        this.audioContext = null;
        this.analyser = null;
        this.mediaStream = null;
        this.frequencyData = new Uint8Array();
        this.waveformData = new Uint8Array();
        this.isPlaying = false;
    }
    
    async startMicrophone() {
        // TODO: iniciar captura do microfone
        console.log('Iniciando captura do microfone...');
        // Devolver Promise
    }
    
    async loadAudioFile(file) {
        // TODO: carregar ficheiro de áudio
        console.log('Carregando ficheiro de áudio...');
        // Devolver Promise
    }
    
    stop() {
        // TODO: parar processamento de áudio
        console.log('Parando processamento de áudio...');
    }
    
    update() {
        // TODO: atualizar dados de áudio
    }
    
    getFrequencyData() {
        // TODO: obter dados de frequência
        return this.frequencyData;
    }
    
    getWaveformData() {
        // TODO: obter dados de forma de onda
        return this.waveformData;
    }
    
    calculateAudioLevel() {
        // TODO: calcular nível de áudio
        return 0;
    }
}

// Motor de Visualização
class VisualizationEngine {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');
        this.visualizations = new Map();
        this.currentVisualization = null;
        this.animationId = null;
        this.isRunning = false;
        
        // Inicializar visualizações
        this.initVisualizations();
    }
    
    initVisualizations() {
        // TODO: inicializar tipos de visualização
        this.visualizations.set('spectrum', new SpectrumVisualization(this.canvas, null));
        this.visualizations.set('waveform', new WaveformVisualization(this.canvas, null));
        this.visualizations.set('particles', new ParticleVisualization(this.canvas, null));
    }
    
    setVisualization(type) {
        // TODO: definir visualização atual
        console.log(`Definindo visualização: ${type}`);
        return false; // Devolver boolean indicando sucesso
    }
    
    start() {
        // TODO: iniciar animação
        console.log('Iniciando motor de visualização...');
    }
    
    stop() {
        // TODO: parar animação
        console.log('Parando motor de visualização...');
    }
    
    resize() {
        // TODO: redimensionar canvas
    }
    
    getVisualizationProperties() {
        // TODO: obter propriedades da visualização atual
        return {};
    }
    
    updateVisualizationProperty(property, value) {
        // TODO: atualizar propriedade da visualização
        console.log(`Atualizando propriedade: ${property} = ${value}`);
    }
}

// Classe Abstrata Base para Visualizações
class AudioVisualization {
    constructor(canvas, audioProcessor) {
        if (this.constructor === AudioVisualization) {
            throw new Error('AudioVisualization é uma classe abstrata e não pode ser instanciada diretamente.');
        }
        
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.audioProcessor = audioProcessor;
        this.name = 'Visualização';
        this.properties = {};
        this.testData = new Uint8Array(256);
        this.frameCount = 0;
        
        // Inicializar dados de teste
        for (let i = 0; i < this.testData.length; i++) {
            this.testData[i] = Math.sin(i / 10) * 128 + 128;
        }
    }
    
    draw() {
        throw new Error('Método draw() deve ser implementado pela subclasse.');
    }
    
    update() {
        // TODO: atualizar estado da visualização
        this.frameCount++;
    }
    
    resize(width, height) {
        // TODO: redimensionar visualização
        this.canvas.width = width;
        this.canvas.height = height;
    }
    
    getProperties() {
        // TODO: obter propriedades da visualização
        return this.properties;
    }
    
    updateProperty(property, value) {
        // TODO: atualizar propriedade
        if (this.properties.hasOwnProperty(property)) {
            this.properties[property] = value;
        }
    }
    
    clearCanvas() {
        // TODO: limpar canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    }
    
    drawGrid() {
        // TODO: desenhar grelha de fundo
    }
    
    createGradient() {
        // TODO: criar gradiente de cores
        return this.ctx.createLinearGradient(0, 0, this.canvas.width, this.canvas.height);
    }
    
    normalizeData() {
        // TODO: normalizar dados de áudio
        return [];
    }
}

// Visualizações Concretas
class SpectrumVisualization extends AudioVisualization {
    constructor(canvas, audioProcessor) {
        super(canvas, audioProcessor);
        this.name = 'Espectro de Frequências';
        // Inicializar propriedades específicas
    }
    
    draw() {
        // TODO: desenhar espectro de frequências
        this.clearCanvas();
        
        // Implementação básica para teste
        const data = this.audioProcessor ? this.audioProcessor.getFrequencyData() : this.testData;
        const barWidth = this.canvas.width / data.length;
        
        for (let i = 0; i < data.length; i++) {
            const barHeight = (data[i] / 255) * this.canvas.height;
            const x = i * barWidth;
            const y = this.canvas.height - barHeight;
            
            this.ctx.fillStyle = `hsl(${i / data.length * 360}, 100%, 50%)`;
            this.ctx.fillRect(x, y, barWidth - 1, barHeight);
        }
    }
    
    getProperties() {
        // TODO: obter propriedades específicas
        return super.getProperties();
    }
}

class WaveformVisualization extends AudioVisualization {
    constructor(canvas, audioProcessor) {
        super(canvas, audioProcessor);
        this.name = 'Forma de Onda';
        // Inicializar propriedades específicas
    }
    
    draw() {
        // TODO: desenhar forma de onda
        this.clearCanvas();
        
        // Implementação básica para teste
        const data = this.audioProcessor ? this.audioProcessor.getWaveformData() : this.testData;
        const sliceWidth = this.canvas.width / data.length;
        
        this.ctx.beginPath();
        this.ctx.moveTo(0, this.canvas.height / 2);
        
        for (let i = 0; i < data.length; i++) {
            const v = data[i] / 128.0;
            const y = v * this.canvas.height / 2;
            const x = i * sliceWidth;
            
            if (i === 0) {
                this.ctx.moveTo(x, y);
            } else {
                this.ctx.lineTo(x, y);
            }
        }
        
        this.ctx.strokeStyle = '#4cc9f0';
        this.ctx.lineWidth = 2;
        this.ctx.stroke();
    }
    
    getProperties() {
        // TODO: obter propriedades específicas
        return super.getProperties();
    }
}

class ParticleVisualization extends AudioVisualization {
    constructor(canvas, audioProcessor) {
        super(canvas, audioProcessor);
        this.name = 'Partículas';
        this.particles = [];
        this.lastTime = 0;
        
        // Inicializar partículas
        this.initParticles();
    }
    
    draw() {
        // TODO: desenhar partículas
        this.clearCanvas();
        this.drawParticles();
        this.drawConnections();
    }
    
    update() {
        // TODO: atualizar partículas
        super.update();
        this.updateParticles();
    }
    
    getProperties() {
        // TODO: obter propriedades específicas
        return super.getProperties();
    }
    
    initParticles() {
        // TODO: inicializar partículas
        for (let i = 0; i < 50; i++) {
            this.particles.push({
                x: Math.random() * this.canvas.width,
                y: Math.random() * this.canvas.height,
                vx: (Math.random() - 0.5) * 2,
                vy: (Math.random() - 0.5) * 2,
                radius: Math.random() * 3 + 1,
                color: `hsl(${Math.random() * 360}, 100%, 50%)`
            });
        }
    }
    
    updateParticles() {
        // TODO: atualizar estado das partículas
        const data = this.audioProcessor ? this.audioProcessor.getFrequencyData() : this.testData;
        const audioLevel = this.audioProcessor ? this.audioProcessor.calculateAudioLevel() : 0.5;
        
        for (let i = 0; i < this.particles.length; i++) {
            const p = this.particles[i];
            
            // Mover partícula
            p.x += p.vx;
            p.y += p.vy;
            
            // Rebater nas bordas
            if (p.x < 0 || p.x > this.canvas.width) p.vx *= -1;
            if (p.y < 0 || p.y > this.canvas.height) p.vy *= -1;
            
            // Aplicar influência do áudio
            if (data.length > 0) {
                const freqIndex = Math.floor(i / this.particles.length * data.length);
                const intensity = data[freqIndex] / 255;
                
                p.vx += (Math.random() - 0.5) * intensity * 0.5;
                p.vy += (Math.random() - 0.5) * intensity * 0.5;
                
                // Limitar velocidade
                const speed = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
                const maxSpeed = 2 + audioLevel * 3;
                if (speed > maxSpeed) {
                    p.vx = (p.vx / speed) * maxSpeed;
                    p.vy = (p.vy / speed) * maxSpeed;
                }
            }
        }
    }
    
    drawParticles() {
        // TODO: desenhar partículas
        for (const p of this.particles) {
            this.ctx.beginPath();
            this.ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
            this.ctx.fillStyle = p.color;
            this.ctx.fill();
        }
    }
    
    drawConnections() {
        // TODO: desenhar conexões entre partículas
        const maxDistance = 100;
        
        for (let i = 0; i < this.particles.length; i++) {
            for (let j = i + 1; j < this.particles.length; j++) {
                const p1 = this.particles[i];
                const p2 = this.particles[j];
                
                const dx = p1.x - p2.x;
                const dy = p1.y - p2.y;
                const distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance < maxDistance) {
                    const opacity = 1 - distance / maxDistance;
                    this.ctx.beginPath();
                    this.ctx.moveTo(p1.x, p1.y);
                    this.ctx.lineTo(p2.x, p2.y);
                    this.ctx.strokeStyle = `rgba(76, 201, 240, ${opacity * 0.5})`;
                    this.ctx.lineWidth = 1;
                    this.ctx.stroke();
                }
            }
        }
    }
}

// Gestão de UI
class UIManager {
    constructor(app) {
        this.app = app;
        this.visualizationEngine = app.visualizationEngine;
        this.audioProcessor = app.audioProcessor;
        
        // Inicializar interface
        this.setupEventListeners();
    }
    
    updatePropertiesPanel() {
        // TODO: atualizar painel de propriedades
        console.log('Atualizando painel de propriedades...');
    }
    
    updateAudioInfo(info, isError = false) {
        // TODO: atualizar informações de áudio
        const audioStatus = document.getElementById('audioStatus');
        const audioLevel = document.getElementById('audioLevel');
        
        if (isError) {
            audioStatus.textContent = `Erro: ${info}`;
            audioStatus.style.color = '#f72585';
        } else {
            audioStatus.textContent = `Áudio: ${info.status || 'Ativo'}`;
            audioStatus.style.color = '#e6e6e6';
            audioLevel.textContent = `Nível: ${info.level || 0}%`;
        }
    }
    
    setButtonStates(playing) {
        // TODO: atualizar estados dos botões
        const startMicBtn = document.getElementById('startMic');
        const stopAudioBtn = document.getElementById('stopAudio');
        
        startMicBtn.disabled = playing;
        stopAudioBtn.disabled = !playing;
    }
    
    showError(message) {
        // TODO: mostrar mensagem de erro
        const errorModal = document.getElementById('errorModal');
        const errorMessage = document.getElementById('errorMessage');
        
        errorMessage.textContent = message;
        errorModal.classList.remove('hidden');
        
        // Fechar modal ao clicar no X
        document.querySelector('.close').onclick = () => {
            errorModal.classList.add('hidden');
        };
        
        // Fechar modal ao clicar fora
        window.onclick = (event) => {
            if (event.target === errorModal) {
                errorModal.classList.add('hidden');
            }
        };
    }
    
    setupEventListeners() {
        // TODO: configurar event listeners
        document.getElementById('startMic').addEventListener('click', () => {
            this.app.startMicrophone();
        });
        
        document.getElementById('stopAudio').addEventListener('click', () => {
            this.app.stopAudio();
        });
        
        document.getElementById('audioFile').addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                this.app.loadAudioFile(e.target.files[0]);
            }
        });
        
        document.getElementById('visualizationType').addEventListener('change', (e) => {
            this.app.setVisualization(e.target.value);
        });
        
        document.getElementById('exportPNG').addEventListener('click', () => {
            this.app.exportManager.exportAsPNG();
        });
        
        document.getElementById('exportJPEG').addEventListener('click', () => {
            this.app.exportManager.exportAsJPEG(0.9);
        });
    }
    
    setupAudioLevels() {
        // TODO: configurar monitorização de níveis de áudio
    }
    
    createPropertyControl(property, value, min, max, step) {
        // TODO: criar controlo de propriedade
        const container = document.createElement('div');
        container.className = 'property-control';
        
        const label = document.createElement('label');
        label.textContent = property;
        label.htmlFor = `prop-${property}`;
        
        const input = document.createElement('input');
        input.type = 'range';
        input.id = `prop-${property}`;
        input.min = min;
        input.max = max;
        input.step = step;
        input.value = value;
        
        input.addEventListener('input', (e) => {
            this.visualizationEngine.updateVisualizationProperty(property, parseFloat(e.target.value));
        });
        
        container.appendChild(label);
        container.appendChild(input);
        
        return container;
    }
}

// Gestão de Exportação
class ExportManager {
    constructor(visualizationEngine) {
        this.visualizationEngine = visualizationEngine;
    }
    
    exportAsPNG() {
        // TODO: exportar como PNG
        console.log('Exportando como PNG...');
        
        try {
            const canvas = this.visualizationEngine.canvas;
            const link = document.createElement('a');
            link.download = `audio-visualization-${new Date().getTime()}.png`;
            link.href = canvas.toDataURL('image/png');
            link.click();
        } catch (error) {
            console.error('Erro ao exportar PNG:', error);
        }
    }
    
    exportAsJPEG(quality = 0.9) {
        // TODO: exportar como JPEG
        console.log(`Exportando como JPEG com qualidade ${quality}...`);
        
        try {
            const canvas = this.visualizationEngine.canvas;
            const link = document.createElement('a');
            link.download = `audio-visualization-${new Date().getTime()}.jpg`;
            link.href = canvas.toDataURL('image/jpeg', quality);
            link.click();
        } catch (error) {
            console.error('Erro ao exportar JPEG:', error);
        }
    }
}

// Inicialização da aplicação quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', () => {
    const app = new App();
    
    // Expor app globalmente para debugging (remover em produção)
    window.app = app;
});