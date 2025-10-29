package physics;
import processing.core.PVector;

public class Mover {
    protected PVector pos;
    protected PVector vel;
    protected PVector acc;
    protected float mass;
    protected float radius;

    protected Mover(PVector pos, PVector vel, float mass, float radius) {
        this.pos = pos.copy();
        this.vel = vel;
        this.mass = mass;
        this.radius = radius;
        this.acc = new PVector();
    }

    public void applyForce(PVector f) {
        acc.add(PVector.div(f, mass));
    }

    public void move(float dt) {
        vel.add(acc.mult(dt));
        pos.add(PVector.mult(vel, dt));
        acc.mult(0);
    }

    public PVector getPos() {
        return pos;
    }

    public PVector getVel() {
        return vel;
    }

    public PVector getAcc() {
        return acc;
    }

    public float getMass() {
        return mass;
    }

    public float getRadius() {
        return radius;
    }
}