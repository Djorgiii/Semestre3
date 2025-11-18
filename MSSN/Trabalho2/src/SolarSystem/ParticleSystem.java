package SolarSystem;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

public class ParticleSystem {

    private ArrayList<Particle> particles;

    public ParticleSystem() {
        this.particles = new ArrayList<Particle>();
    }

    /**
     * MÉTODO 1: Para a "corona" do Sol (explosão)
     * (Com a velocidade corrigida)
     */
    public void addSunParticle(PApplet p, PVector origin) {
        PVector velocity = PVector.random2D();
        velocity.mult(p.random(50, 150)); // Velocidade alta
        int color = p.color(255, p.random(100, 255), 0);
        float radius = p.random(1, 3);
        float lifespan = 255f; // Vida longa
        particles.add(new Particle(p, origin.copy(), velocity, 1f, radius, color, lifespan));
    }
    
    /**
     * MÉTODO 2: Para o "rasto" do planeta
     */
    public void addTrailParticle(PApplet p, PVector origin, PVector planetVelocity, int planetColor) {
        // A velocidade da partícula é OPOSTA à do planeta, e mais lenta
        PVector velocity = planetVelocity.copy();
        velocity.mult(-1);      // Inverte a direção
        velocity.normalize();   // Apenas a direção
        velocity.mult(p.random(10, 25)); // Velocidade de "escape" lenta

        // Adicionar um pouco de "spray" aleatório
        velocity.add(PVector.random2D().mult(p.random(5, 10))); 
        
        float radius = p.random(1, 2); // Partículas pequenas
        float lifespan = 255f; // Tempo de vida curto para um rasto

        particles.add(new Particle(p, origin.copy(), velocity, 1f, radius, planetColor, lifespan));
    }

    /**
     * Atualiza a física de todas as partículas
     */
    public void update(float dt) {
        // Iterar ao contrário para poder remover items
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle particle = particles.get(i);
            particle.move(dt); // Atualiza movimento e lifespan
            if (particle.isDead()) {
                particles.remove(i);
            }
        }
    }

    /**
     * Desenha todas as partículas
     */
    public void display(PApplet p) {
        for (Particle particle : particles) {
            particle.display();
        }
    }
}