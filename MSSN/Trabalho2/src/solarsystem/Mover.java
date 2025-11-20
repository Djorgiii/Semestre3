package solarsystem;

import processing.core.PVector;

/**
 * Classe base para física (Mover).
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
        // (Ignora a massa se for 0 para evitar divisão por zero, embora não deva acontecer)
        if (mass != 0) {
            PVector forceDividedByMass = PVector.div(force, mass);
            this.acceleration.add(forceDividedByMass);
        }
    }

    /**
     * Atualiza a física (Integração de Euler)
     * @param secondsElapsed Tempo decorrido desde o último frame (dt)
     */
    public void move(float secondsElapsed) { 
        // Velocidade altera-se com a aceleração
        // v = v + a * t
        this.velocity.add(PVector.mult(acceleration, secondsElapsed));
        
        // Posição altera-se com a velocidade
        // p = p + v * t
        this.position.add(PVector.mult(velocity, secondsElapsed));
        
        // Limpar a aceleração para o próximo frame
        this.acceleration.mult(0);
    }

    // --- Getters (Renomeados para clareza) ---
    
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