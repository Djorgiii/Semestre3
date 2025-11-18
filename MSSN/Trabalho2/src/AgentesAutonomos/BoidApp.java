package AgentesAutonomos;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;

public class BoidApp implements iProcessing {

    private Boid boid;
    private PVector target; 
    
    // VARIÁVEL DE CONTROLO
    private boolean useArrive = true; // Começa com Arrive ligado
    
    @Override
    public void setup(PApplet p) {
        PVector center = new PVector(p.width/2, p.height/2);
        boid = new Boid(p, center, 1, 10, p.color(0, 255, 255));
        target = center.copy();
    }

    @Override
    public void draw(PApplet p, float dt) {
        p.background(50);

        // Atualizar alvo para o rato
        target.set(p.mouseX, p.mouseY);
        
        // Desenhar alvo
        p.noStroke();
        p.fill(255, 0, 0);
        p.circle(target.x, target.y, 10);

        // --- LÓGICA DE ESCOLHA (SEEK vs ARRIVE) ---
        
        if (useArrive) {
            // MODO ARRIVE
            float R = 300f;
            
            // Desenhar o círculo branco (só faz sentido no Arrive)
            p.noFill();
            p.stroke(255, 100);
            p.circle(target.x, target.y, R * 2);
            
            boid.arrive(target, R);
            
            // Texto informativo
            p.fill(0, 255, 0);
            p.text("MODO ATUAL: ARRIVE (Chegada Suave)", 10, 100);
            
        } else {
            // MODO SEEK
            boid.seek(target);
            
            // Texto informativo
            p.fill(255, 165, 0); // Laranja
            p.text("MODO ATUAL: SEEK (Perseguição Rápida)", 10, 100);
        }

        // -------------------------------------------

        // Travão Manual (Funciona em ambos os modos)
        if (p.keyPressed && (p.key == 't' || p.key == 'T')) {
            boid.brake();
            p.fill(255, 0, 0);
            p.text("!!! A TRAVAR !!!", 10, 80);
        }

        boid.move(dt);
        boid.display();
        
        // Interface
        p.fill(255);
        p.text("Velocidade: " + (int)boid.getVel().mag() + " / " + (int)boid.getMaxSpeed(), 10, 20);
        p.text("Força: " + (int)boid.getMaxForce(), 10, 40);
        p.text("[ESPAÇO] para trocar de modo", 10, 60);
    }

    @Override
    public void mousePressed(PApplet p) {}
    
    @Override
    public void keyPressed(PApplet p) {
        // --- TROCAR DE MODO ---
        if (p.key == ' ') { // Barra de Espaço
            useArrive = !useArrive; // Inverte (True vira False, False vira True)
        }
        // ----------------------
        
        if (p.keyCode == PApplet.UP) boid.setMaxSpeed(boid.getMaxSpeed() + 10f);
        if (p.keyCode == PApplet.DOWN) boid.setMaxSpeed(boid.getMaxSpeed() - 10f);
        if (p.keyCode == PApplet.RIGHT) boid.setMaxForce(boid.getMaxForce() + 10f);
        if (p.keyCode == PApplet.LEFT) boid.setMaxForce(boid.getMaxForce() - 10f);
    }

    @Override
    public void mouseMoved(PApplet p) {}
}