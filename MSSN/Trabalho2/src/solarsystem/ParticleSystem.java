package solarsystem;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

public class ParticleSystem {

    private ArrayList<Particle> particleList; // Antes: particles

    public ParticleSystem() {
        this.particleList = new ArrayList<Particle>();
    }

    /**
     * MÉTODO 1: Para a "corona" do Sol (explosão)
     */
    public void addSunParticle(PApplet app, PVector originPosition) {
        PVector velocity = PVector.random2D();
        velocity.mult(app.random(50, 150)); // Velocidade alta
        
        int particleColor = app.color(255, app.random(100, 255), 0);
        float radius = app.random(1, 3);
        float lifespan = 255f; // Vida longa
        
        particleList.add(new Particle(app, originPosition.copy(), velocity, 1f, radius, particleColor, lifespan));
    }
    
    /**
     * MÉTODO 2: Para o "rasto" do planeta
     */
    public void addTrailParticle(PApplet app, PVector originPosition, PVector planetVelocity, int planetColor) {
        // A velocidade da partícula é OPOSTA à do planeta, e mais lenta
        PVector velocity = planetVelocity.copy();
        velocity.mult(-1);      // Inverte a direção
        velocity.normalize();   // Apenas a direção
        velocity.mult(app.random(10, 25)); // Velocidade de "escape" lenta

        // Adicionar um pouco de "spray" aleatório
        velocity.add(PVector.random2D().mult(app.random(5, 10))); 
        
        float radius = app.random(1, 2); // Partículas pequenas
        float lifespan = 255f; // Tempo de vida curto para um rasto

        particleList.add(new Particle(app, originPosition.copy(), velocity, 1f, radius, planetColor, lifespan));
    }

    /**
     * Atualiza a física de todas as partículas
     */
    public void update(float secondsElapsed) { // Antes: dt
        // Iterar ao contrário para poder remover items
        for (int i = particleList.size() - 1; i >= 0; i--) {
            Particle particle = particleList.get(i);
            
            particle.move(secondsElapsed); // Atualiza movimento e lifespan
            
            if (particle.isDead()) {
                particleList.remove(i);
            }
        }
    }

    /**
     * Desenha todas as partículas
     */
    public void display(PApplet app) {
        for (Particle particle : particleList) {
            particle.display();
        }
    }
}