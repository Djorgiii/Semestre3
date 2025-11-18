// Em physics/Planet.java (ou no seu package SolarSystem)

package SolarSystem; // (use o nome do seu package)
import processing.core.PVector;
import processing.core.PApplet;

/**
 * A classe "Planet" herda de "Body" e ADICIONA um rasto.
 * Esta classe tem SEMPRE um rasto.
 */
public class Planet extends Body {
    
    private ParticleSystem trail; 

    public Planet(PApplet p, PVector pos, PVector vel, float mass, float radius, int color) {
        // 1. Chama o construtor da classe-pai (Body)
        super(p, pos, vel, mass, radius, color);
        
        // 2. Inicializa o rasto. Sem 'if' ou booleans.
        this.trail = new ParticleSystem();
    }

    /**
     * Override: Pega no 'move' básico e adiciona a lógica do rasto
     */
    @Override
    public void move(float dt) {
        super.move(dt); // 1. Chama o move() da classe Mover
        
        // 2. Atualiza o rasto. Sem 'if'.
        // (Usa 'p', 'pos', 'vel', 'color' que herdou da classe Body)
        trail.addTrailParticle(p, this.pos, this.vel, this.color);
        trail.update(dt);
    }

    /**
     * Override: Pega no 'display' básico e adiciona o rasto
     */
    @Override
    public void display() {
        // 1. Desenha o rasto. Sem 'if'.
        trail.display(p);
        
        // 2. Desenha o planeta (chamando o display() da classe-pai)
        super.display(); 
    }
}