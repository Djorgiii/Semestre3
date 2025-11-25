package solarsystem;

import processing.core.PVector;


public class Mover {
    
    protected PVector position;
    protected PVector velocity;
    protected PVector acceleration;
    protected float mass;
    protected float radius;

    // Construtor
    protected Mover(PVector initialPosition, PVector initialVelocity, float mass, float radius) {
        this.position = initialPosition.copy();
        this.velocity = initialVelocity.copy();
        this.mass = mass;
        this.radius = radius;
        this.acceleration = new PVector();
    }

    public void applyForce(PVector force) {
        if (mass != 0) {
            PVector forceDividedByMass = PVector.div(force, mass);
            this.acceleration.add(forceDividedByMass);
        }
    }

    public void move(float secondsElapsed) { 

        this.velocity.add(PVector.mult(acceleration, secondsElapsed));
        
        this.position.add(PVector.mult(velocity, secondsElapsed));
        
        this.acceleration.mult(0);
    }

    
    public PVector getPosition() { 
        return position; 
    }
    
    public PVector getVelocity() { 
        return velocity; 
    }
    
    public PVector getAcceleration() { 
        return acceleration; 
    }
    
    public float getMass() { 
        return mass; 
    }
    
    public float getRadius() { 
        return radius; 
    }
}