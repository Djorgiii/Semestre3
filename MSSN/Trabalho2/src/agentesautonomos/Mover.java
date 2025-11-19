package agentesautonomos; // O seu package

import processing.core.PVector;

/**
 * Classe base para física.
 * Refatorizada com nomes de variáveis explícitos.
 */
public class Mover {
    
    protected PVector position;     // Antes: pos
    protected PVector velocity;     // Antes: vel
    protected PVector acceleration; // Antes: acc
    protected float mass;
    protected float radius;

    // Construtor
    protected Mover(PVector initialPosition, PVector initialVelocity, float mass, float radius) {
        this.position = initialPosition.copy();
        this.velocity = initialVelocity.copy(); // .copy() para segurança
        this.mass = mass;
        this.radius = radius;
        this.acceleration = new PVector();
    }

    public void applyForce(PVector force) {
        // Segunda Lei de Newton: Aceleração = Força / Massa
        PVector forceDividedByMass = PVector.div(force, mass);
        this.acceleration.add(forceDividedByMass);
    }

    /**
     * Método move() mais estável
     */
    public void move(float secondsElapsed) { // Antes: dt
        // v = v + a * t
        this.velocity.add(PVector.mult(acceleration, secondsElapsed));
        
        // p = p + v * t
        this.position.add(PVector.mult(velocity, secondsElapsed));
        
        // Limpar a aceleração para o próximo frame
        this.acceleration.mult(0);
    }

    // --- Getters (nomes atualizados) ---
    
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