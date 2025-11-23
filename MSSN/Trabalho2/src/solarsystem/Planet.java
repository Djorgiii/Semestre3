package solarsystem;

import processing.core.PApplet;
import processing.core.PVector;

public class Planet extends Body {
    
    private ParticleSystem trail; 

    public Planet(PApplet app, PVector position, PVector velocity, float mass, float radius, int color) {
        super(app, position, velocity, mass, radius, color);
        
        this.trail = new ParticleSystem();
    }

    @Override
    public void move(float secondsElapsed) {
        super.move(secondsElapsed);
        
        trail.addTrailParticle(app, this.position, this.velocity, this.color);
        
        trail.update(secondsElapsed);
    }

    @Override
    public void display() {
        trail.display(app);
        
        super.display(); 
    }
}