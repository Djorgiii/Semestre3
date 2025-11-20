package flock;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

public class ParticleSystem {

    private ArrayList<Particle> particleList; // Antes: particles

    public ParticleSystem() {
        this.particleList = new ArrayList<Particle>();
    }

    // --- MÉTODO DE EXPLOSÃO (Para os Boids) ---
    public void explode(PApplet app, PVector originPosition) {
        for (int i = 0; i < 50; i++) { // Cria 50 partículas de uma vez
            PVector velocity = PVector.random2D();
            velocity.mult(app.random(50, 400)); // Velocidade muito alta
            
            int particleColor = app.color(255, app.random(0, 100), 0); // Laranja/Vermelho
            float radius = app.random(2, 5);
            float lifespan = 255f; 
            
            // Adiciona à lista (Particle já usa nomes novos)
            particleList.add(new Particle(app, originPosition.copy(), velocity, 1f, radius, particleColor, lifespan));
        }
    }

    // --- MÉTODOS ANTIGOS (Compatibilidade com Sistema Solar) ---
    
    public void addSunParticle(PApplet app, PVector originPosition) {
        PVector velocity = PVector.random2D();
        velocity.mult(app.random(50, 150));
        
        int particleColor = app.color(255, app.random(100, 255), 0);
        float radius = app.random(1, 3);
        
        particleList.add(new Particle(app, originPosition.copy(), velocity, 1f, radius, particleColor, 255f));
    }

    public void addTrailParticle(PApplet app, PVector originPosition, PVector planetVelocity, int planetColor) {
        PVector velocity = planetVelocity.copy();
        velocity.mult(-1);      
        velocity.normalize();   
        velocity.mult(app.random(10, 25));
        velocity.add(PVector.random2D().mult(app.random(5, 10))); 
        
        float radius = app.random(1, 2); 
        
        particleList.add(new Particle(app, originPosition.copy(), velocity, 1f, radius, planetColor, 255f));
    }

    // --- MÉTODOS PADRÃO ---

    public void update(float secondsElapsed) { // Antes: dt
        for (int i = particleList.size() - 1; i >= 0; i--) {
            Particle particle = particleList.get(i);
            
            particle.move(secondsElapsed);
            
            if (particle.isDead()) {
                particleList.remove(i);
            }
        }
    }

    public void display(PApplet app) {
        for (Particle particle : particleList) {
            particle.display();
        }
    }
}