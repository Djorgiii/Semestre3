package agentesautonomos; // O seu package

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Boid extends Body {

    // Limites do agente (nomes explícitos)
    private float maximumSpeed;
    private float maximumForce;

    public Boid(PApplet app, PVector position, float mass, float radius, int color) {
        // Chama o construtor da Body (que já usa os nomes novos)
        super(app, position, new PVector(0,0), mass, radius, color);
        
        this.maximumSpeed = 200f;   // Velocidade padrão
        this.maximumForce = 500f;   // Capacidade de manobra padrão
    }

    public void arrive(PVector targetPosition, float slowingRadius) {
        PVector desiredVelocity = PVector.sub(targetPosition, position);
        float distanceToTarget = desiredVelocity.mag();
        
        // 1. Snap (Pregar no alvo) se estiver muito perto
        if (distanceToTarget < 5.0f) { 
            velocity.mult(0);
            // Atualizar a posição diretamente
            // (Nota: se a variável 'position' for protected na classe Mover, podemos acessá-la assim. 
            // Se for private, teríamos de usar um setter ou assumir que 'pos' era acessível)
            this.position = targetPosition.copy();
            return;
        }
        
        PVector steeringForce;
        
        if (distanceToTarget < slowingRadius) {
            // --- DENTRO DA ZONA DE TRAVAGEM ---
            
            // Calcular a velocidade teórica ideal
            double brakingCurveExponent = 1.0; // Antes: k
            
            // Factor de rampa (0.0 a 1.0)
            float rampingFactor = (float) Math.pow(distanceToTarget / slowingRadius, brakingCurveExponent);
            float desiredSpeed = maximumSpeed * rampingFactor;
            
            desiredVelocity.setMag(desiredSpeed);
            
            // Calcular a força de direção
            steeringForce = PVector.sub(desiredVelocity, velocity);
            steeringForce.limit(maximumForce * 10.0f); // Travões fortes permitidos aqui
            
            applyForce(steeringForce);
            
            // --- Velocity Clamping ---
            if (velocity.mag() > desiredSpeed) {
                velocity.setMag(desiredSpeed);
            }

        } else {
            // --- FORA DA ZONA ---
            desiredVelocity.setMag(maximumSpeed);
            steeringForce = PVector.sub(desiredVelocity, velocity);
            steeringForce.limit(maximumForce); 
            applyForce(steeringForce);
        }
    }
    
    /**
     * O comportamento SEEK (Procurar)
     */
    public void seek(PVector targetPosition) {
        // 1. Vetor para o alvo (Desired Velocity)
        PVector desiredVelocity = PVector.sub(targetPosition, position);
        
        // 2. Normalizar e multiplicar pela velocidade máxima
        desiredVelocity.normalize();
        desiredVelocity.mult(maximumSpeed);

        // 3. Steering Force = Desired - Velocity
        PVector steeringForce = PVector.sub(desiredVelocity, velocity);
        
        // 4. Limitar a força
        steeringForce.limit(maximumForce);

        // 5. Aplicar a força
        applyForce(steeringForce);
    }

    @Override
    public void move(float secondsElapsed) { // Antes: dt
        super.move(secondsElapsed);
        // Garantir que nunca excedemos a velocidade máxima
        velocity.limit(maximumSpeed);
    }

    /**
     * Desenha o Boid como um triângulo
     */
    @Override
    public void display() {
        app.pushMatrix();
        app.translate(position.x, position.y);
        
        // Rodar na direção da velocidade
        float rotationAngle = velocity.heading(); 
        app.rotate(rotationAngle);

        app.pushStyle();
        app.fill(color);
        app.noStroke();
        
        // Desenhar um triângulo
        app.beginShape();
        app.vertex(radius * 2, 0);
        app.vertex(-radius, -radius);
        app.vertex(-radius, radius);
        app.endShape(PConstants.CLOSE);
        
        app.popStyle();
        
        app.popMatrix();
    }

    // --- Controlos ---

    public void setMaximumSpeed(float speed) {
        this.maximumSpeed = speed;
        if (this.maximumSpeed < 0) this.maximumSpeed = 0; 
    }
    
    public void setMaximumForce(float force) {
        this.maximumForce = force;
        if (this.maximumForce < 0) this.maximumForce = 0;
    }
    
    public float getMaximumSpeed() { return maximumSpeed; }
    public float getMaximumForce() { return maximumForce; }
    
    // "Travão"
    public void brake() {
        
        if (velocity.mag() < 10.0f) {
            velocity.mult(0); // Parar completamente se estiver lento
            return;
        }
        
        PVector brakingForce = velocity.copy();
        brakingForce.mult(-1); // Direção oposta
        brakingForce.normalize();
        brakingForce.mult(maximumForce * 0.5f); // Força de travagem
        applyForce(brakingForce);
    }
}