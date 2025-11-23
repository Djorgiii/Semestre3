package agentesautonomos;

import processing.core.PVector;
import processing.core.PApplet;

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
        
        app.ellipse(position.x, position.y, 2 * radius, 2 * radius);
        
        app.popStyle();
    }
}