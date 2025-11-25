package solarsystem;

import processing.core.PApplet;
import processing.core.PVector;

public class Body extends Mover {
    
    protected int color;
    protected PApplet app;

    public Body(PApplet app, PVector position, PVector velocity, float mass, float radius, int color) {
        super(position, velocity, mass, radius);
        this.app = app;
        this.color = color;
    }

    public void display() {
        app.pushStyle();
        app.noStroke();
        app.fill(color);
        
        app.ellipse(position.x, position.y, radius * 2, radius * 2);
        
        app.popStyle();
    }
    
    public int getColor() { 
        return color; 
    }
}