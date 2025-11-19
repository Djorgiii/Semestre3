package flock;

import processing.core.PVector;

public class Mover {
    
    protected PVector position;     // Antes: pos
    protected PVector velocity;     // Antes: vel
    protected PVector acceleration; // Antes: acc
    protected float mass;
    protected float radius;

    public Mover(PVector initialPosition, PVector initialVelocity, float mass, float radius) {
        this.position = initialPosition.copy();
        this.velocity = initialVelocity.copy();
        this.mass = mass;
        this.radius = radius;
        this.acceleration = new PVector();
    }

    public void applyForce(PVector force) {
        // F = m * a  ->  a = F / m
        PVector forceDividedByMass = PVector.div(force, mass);
        this.acceleration.add(forceDividedByMass);
    }

    public void move(float secondsElapsed) { // Antes: dt
        // Velocidade muda com a aceleração (v = v + a*t)
        this.velocity.add(PVector.mult(acceleration, secondsElapsed));
        
        // Posição muda com a velocidade (p = p + v*t)
        this.position.add(PVector.mult(velocity, secondsElapsed));
        
        // Limpar a aceleração para o próximo frame
        this.acceleration.mult(0);
    }

    // Getters explícitos
    public PVector getPosition() { return position; }
    public PVector getVelocity() { return velocity; }
    public PVector getAcceleration() { return acceleration; }
    public float getMass() { return mass; }
    public float getRadius() { return radius; }
}