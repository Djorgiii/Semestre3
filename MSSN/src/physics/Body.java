package physics;
import processing.core.PVector;
import processing.core.PApplet;

public class Body extends Mover {
    private int color;
    private PApplet p;

    public Body(PApplet p, PVector pos, PVector vel, float mass, float radius, int color) {
        super(pos, vel, mass, radius);
        this.p = p;
        this.color = color;
    }

    public void display(){
        p.pushStyle();
        p.fill(color);
        p.ellipse(pos.x,pos.y, 2*radius,2*radius);
        p.popStyle();
    }
}