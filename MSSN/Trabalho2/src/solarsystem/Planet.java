package solarsystem;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * A classe "Planet" herda de "Body" e ADICIONA um rasto.
 * Refatorizada com nomes de variáveis explícitos.
 */
public class Planet extends Body {
    
    private ParticleSystem trail; 

    public Planet(PApplet app, PVector position, PVector velocity, float mass, float radius, int color) {
        // 1. Chama o construtor da classe-pai (Body) com os novos nomes
        super(app, position, velocity, mass, radius, color);
        
        // 2. Inicializa o rasto
        this.trail = new ParticleSystem();
    }

    /**
     * Override: Pega no 'move' básico e adiciona a lógica do rasto
     */
    @Override
    public void move(float secondsElapsed) { // Antes: dt
        super.move(secondsElapsed); // 1. Chama o move() da classe Mover
        
        // 2. Atualiza o rasto
        // Usa 'app', 'position', 'velocity', 'color' que herdou da classe Body/Mover
        trail.addTrailParticle(app, this.position, this.velocity, this.color);
        
        trail.update(secondsElapsed);
    }

    /**
     * Override: Pega no 'display' básico e adiciona o rasto
     */
    @Override
    public void display() {
        // 1. Desenha o rasto
        trail.display(app);
        
        // 2. Desenha o planeta (chamando o display() da classe-pai)
        super.display(); 
    }
}