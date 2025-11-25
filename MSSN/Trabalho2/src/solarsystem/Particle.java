package solarsystem;

import processing.core.PApplet;
import processing.core.PVector;

public class Particle extends Mover {

    private int color;
    private PApplet app;
    private float lifespan;

    public Particle(PApplet app, PVector position, PVector velocity, float mass, float radius, int color, float lifespan) {
        super(position, velocity, mass, radius); 
        this.app = app;
        this.color = color;
        this.lifespan = lifespan;
    }

    @Override
    public void move(float secondsElapsed) {
        super.move(secondsElapsed);
        this.lifespan -= 0.5f;
    }

    public boolean isDead() {
        return this.lifespan < 0.0f;
    }

    public void display() {
        app.pushStyle();
        
        float alpha = PApplet.map(this.lifespan, 0, 255, 0, 255);
        app.fill(this.color, alpha);
        app.noStroke();
        
        app.ellipse(position.x, position.y, 2 * radius, 2 * radius);
        
        app.popStyle();
    }
}