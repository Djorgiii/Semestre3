package AgentesAutonomos;

import processing.core.PApplet;
import processing.core.PVector;

public class Boid extends Body {

    // Limites do agente
    private float maxSpeed;
    private float maxForce;

    public Boid(PApplet p, PVector pos, float mass, float radius, int color) {
        super(p, pos, new PVector(0,0), mass, radius, color);
        this.maxSpeed = 200f;   // Velocidade padrão
        this.maxForce = 500f;   // Capacidade de manobra padrão
    }

    public void arrive(PVector target, float slowingRadius) {
        PVector desired = PVector.sub(target, pos);
        float d = desired.mag();
        
        // 1. Snap (Pregar no alvo) se estiver muito perto
        if (d < 5.0f) { // Reduzi para 5 para ser mais preciso
            vel.mult(0);
            pos = target.copy();
            return;
        }
        
        PVector steer;
        
        if (d < slowingRadius) {
            // --- DENTRO DA ZONA DE TRAVAGEM ---
            
            // Calcular a velocidade teórica ideal
            // k=1.0 (Linear) é o mais seguro para evitar passar do ponto
            double k = 1.0; 
            float factor = (float) Math.pow(d / slowingRadius, k);
            float desiredSpeed = maxSpeed * factor;
            
            desired.setMag(desiredSpeed);
            
            // Calcular a força de direção
            steer = PVector.sub(desired, vel);
            steer.limit(maxForce * 10.0f); // Travões fortes
            
            applyForce(steer);
            
            // --- A CORREÇÃO MÁGICA (Velocity Clamping) ---
            // Isto impede que a inércia vença a matemática.
            // Se o Boid estiver a andar mais rápido do que devia nesta distância,
            // cortamos o excesso de velocidade manualmente.
            if (vel.mag() > desiredSpeed) {
                vel.setMag(desiredSpeed);
            }
            // ---------------------------------------------

        } else {
            // --- FORA DA ZONA ---
            desired.setMag(maxSpeed);
            steer = PVector.sub(desired, vel);
            steer.limit(maxForce); 
            applyForce(steer);
        }
    }
    
    /**
     * O comportamento SEEK (Procurar):
     * Calcula a força necessária para virar em direção ao alvo.
     */
    public void seek(PVector target) {
        // 1. Vetor para o alvo (Desired Velocity)
        PVector desired = PVector.sub(target, pos);
        
        // 2. Normalizar e multiplicar pela velocidade máxima
        desired.normalize();
        desired.mult(maxSpeed);

        // 3. Steering Force = Desired - Velocity
        PVector steer = PVector.sub(desired, vel);
        
        // 4. Limitar a força (para ele não virar instantaneamente)
        steer.limit(maxForce);

        // 5. Aplicar a força (física de Newton)
        applyForce(steer);
    }

    @Override
    public void move(float dt) {
        super.move(dt);
        // Garantir que nunca excedemos a velocidade máxima
        // (Mesmo que a gravidade ou outras forças ajudem)
        vel.limit(maxSpeed);
    }

    /**
     * Desenha o Boid como um triângulo que aponta na direção do movimento
     */
    @Override
    public void display() {
        p.pushMatrix();
        p.translate(pos.x, pos.y);
        
        // Rodar na direção da velocidade
        float angle = vel.heading(); 
        p.rotate(angle);

        p.pushStyle();
        p.fill(color);
        p.noStroke();
        // Desenhar um triângulo
        p.beginShape();
        p.vertex(radius * 2, 0);
        p.vertex(-radius, -radius);
        p.vertex(-radius, radius);
        p.endShape(p.CLOSE);
        p.popStyle();
        
        p.popMatrix();
    }

    // --- Controlos para o Exercício ---

    public void setMaxSpeed(float v) {
        this.maxSpeed = v;
        // Não permitir velocidade negativa
        if (this.maxSpeed < 0) this.maxSpeed = 0; 
    }
    
    public void setMaxForce(float f) {
        this.maxForce = f;
        if (this.maxForce < 0) this.maxForce = 0;
    }
    
    public float getMaxSpeed() { return maxSpeed; }
    public float getMaxForce() { return maxForce; }
    
    // "Travão": aplica uma força contrária forte
    public void brake() {
    	
    	if (vel.mag() < 10.0f) {
    		vel.mult(0); // Parar completamente se estiver lento
    		return;
    	}
    	
        PVector brakeForce = vel.copy();
        brakeForce.mult(-1); // Direção oposta
        brakeForce.normalize();
        brakeForce.mult(maxForce * 0.5f); // Força de travagem
        applyForce(brakeForce);
    }
}