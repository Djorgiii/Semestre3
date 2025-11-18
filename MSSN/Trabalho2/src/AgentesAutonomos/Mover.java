package AgentesAutonomos; // (use o nome do seu package)
import processing.core.PVector;

public class Mover {
    protected PVector pos;
    protected PVector vel;
    protected PVector acc;
    protected float mass;
    protected float radius;

    protected Mover(PVector pos, PVector vel, float mass, float radius) {
        this.pos = pos.copy();
        this.vel = vel.copy(); // Usar .copy() para segurança
        this.mass = mass;
        this.radius = radius;
        this.acc = new PVector();
    }

    public void applyForce(PVector f) {
        acc.add(PVector.div(f, mass));
    }

    /**
     * Método move() mais estável
     */
    public void move(float dt) {
        // v = v + a*dt
        vel.add(PVector.mult(acc, dt));
        // p = p + v*dt
        pos.add(PVector.mult(vel, dt));
        // Limpar aceleração para o próximo frame
        acc.mult(0);
    }

    // --- Getters ---
    public PVector getPos() { return pos; }
    public PVector getVel() { return vel; }
    public PVector getAcc() { return acc; }
    public float getMass() { return mass; }
    public float getRadius() { return radius; }
}