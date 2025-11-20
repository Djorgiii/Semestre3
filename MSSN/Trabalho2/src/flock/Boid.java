package flock;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import java.util.ArrayList;

public class Boid extends Body {

    private float maximumSpeed;
    private float maximumForce;
    private float lifeTimer;

    public Boid(PApplet app, PVector position, float mass, float radius, int color) {
        // Chama o construtor da Body (que já usa os nomes novos)
        super(app, position, new PVector(0,0), mass, radius, color);
        
        this.maximumSpeed = 300.0f; 
        this.maximumForce = 500.0f; 
        this.lifeTimer = app.random(2, 5);
    }

    /**
     * SEPARAÇÃO: Calcula a força para evitar colisão com vizinhos
     * (Renomeado para combinar com FlockApp: calculateSeparationForce)
     */
    public PVector calculateSeparationForce(ArrayList<Boid> nearbyBoids) {
        float desiredSeparation = 25.0f; // Raio da "bolha pessoal"
        PVector sumOfRepulsions = new PVector(0, 0, 0);
        int neighborCount = 0;

        for (Boid otherBoid : nearbyBoids) {
            // Calcula a distância para o outro boid
            float distanceToNeighbor = PVector.dist(position, otherBoid.position);
            
            // Se for um vizinho válido (não eu mesmo) e estiver dentro da bolha
            if ((distanceToNeighbor > 0) && (distanceToNeighbor < desiredSeparation)) {
                
                // Cria vetor que aponta PARA LONGE do vizinho
                PVector vectorAway = PVector.sub(position, otherBoid.position);
                vectorAway.normalize();
                vectorAway.div(distanceToNeighbor); // Quanto mais perto, maior a força de fuga
                
                sumOfRepulsions.add(vectorAway);
                neighborCount++;
            }
        }

        // Média das forças de repulsão
        if (neighborCount > 0) {
            sumOfRepulsions.div((float)neighborCount);
        }

        // Se houver força para aplicar (Steering = Desired - Velocity)
        if (sumOfRepulsions.mag() > 0) {
            sumOfRepulsions.setMag(maximumSpeed);
            sumOfRepulsions.sub(velocity);
            sumOfRepulsions.limit(maximumForce);
        }
        return sumOfRepulsions;
    }

    /**
     * ARRIVE: Chegada suave com paragem precisa
     * (Renomeado para combinar com FlockApp: applyArriveBehavior)
     */
    public void applyArriveBehavior(PVector targetPosition, float slowingRadius, boolean shouldSnapToTarget) {
        PVector desiredVelocity = PVector.sub(targetPosition, position);
        float distanceToTarget = desiredVelocity.mag();
        
        // 1. SNAP: Se estiver muito perto (15px) e a flag estiver ativa
        if (shouldSnapToTarget && distanceToTarget < 15.0f) { 
            velocity.mult(0); 
            // Atualizar a posição diretamente
            this.position = targetPosition.copy(); 
            return;
        }
        
        PVector steeringForce;
        
        if (distanceToTarget < slowingRadius) {
            // --- DENTRO DA ZONA DE TRAVAGEM ---
            double brakingCurveExponent = 1.0; // Curva Linear
            
            float rampingFactor = (float) Math.pow(distanceToTarget / slowingRadius, brakingCurveExponent);
            float desiredSpeed = maximumSpeed * rampingFactor;
            
            desiredVelocity.setMag(desiredSpeed);
            
            steeringForce = PVector.sub(desiredVelocity, velocity);
            steeringForce.limit(maximumForce * 10.0f); // Travões fortes permitidos
            
            applyForce(steeringForce);
            
            // Velocity Clamping: Cortar velocidade excessiva manualmente
            if (velocity.mag() > desiredSpeed) {
                velocity.setMag(desiredSpeed);
            }

        } else {
            // --- FORA DA ZONA (Cruzeiro) ---
            desiredVelocity.setMag(maximumSpeed);
            steeringForce = PVector.sub(desiredVelocity, velocity);
            steeringForce.limit(maximumForce); 
            applyForce(steeringForce);
        }
    }

    // --- Métodos Auxiliares e de Estado (Renomeados) ---

    public void decreaseLifeTimer(float secondsElapsed) { // Antes: decreaseTimer
        this.lifeTimer -= secondsElapsed;
    }

    public boolean isDead() {
        return this.lifeTimer < 0;
    }

    public void setLifeTimer(float seconds) { // Antes: setTimer
        this.lifeTimer = seconds;
    }
    
    public void setVelocityManually(PVector newVelocity) { // Antes: setVelocity
        this.velocity = newVelocity.copy();
    }

    // Getters para MaxSpeed/MaxForce (caso precise na interface)
    public void setMaximumSpeed(float speed) { this.maximumSpeed = speed; }
    public void setMaximumForce(float force) { this.maximumForce = force; }
    public float getMaximumSpeed() { return maximumSpeed; }
    public float getMaximumForce() { return maximumForce; }

    @Override
    public void display() {
        // Efeito visual: Piscar vermelho no último segundo de vida
        if (lifeTimer < 1.0f && (app.millis() / 100) % 2 == 0) {
            app.fill(255, 0, 0); 
        } else {
            app.fill(color); // Usa a cor herdada do Body
        }
        
        app.pushMatrix();
        app.translate(position.x, position.y);
        
        // Rodar na direção da velocidade atual
        float rotationAngle = velocity.heading();
        app.rotate(rotationAngle);
        
        app.noStroke();
        // Desenha o triângulo (Boid)
        app.beginShape();
        app.vertex(radius * 2, 0);      // Nariz
        app.vertex(-radius, -radius);   // Asa esquerda
        app.vertex(-radius, radius);    // Asa direita
        app.endShape(PConstants.CLOSE);
        
        app.popMatrix();
    }
}