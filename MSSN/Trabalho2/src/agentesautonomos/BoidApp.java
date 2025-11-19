package agentesautonomos;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;

public class BoidApp implements iProcessing {

    private Boid boid;
    private PVector targetPosition; 
    
    // Variável de controlo de estado
    private boolean isArriveMode = true; 
    
    @Override
    public void setup(PApplet app) { // Antes: p -> app
        PVector screenCenter = new PVector(app.width/2, app.height/2);
        
        // Criar o Boid (usando app)
        boid = new Boid(app, screenCenter, 1, 10, app.color(0, 255, 255));
        targetPosition = screenCenter.copy();
    }

    @Override
    public void draw(PApplet app, float secondsElapsed) { // Antes: dt -> secondsElapsed
        app.background(50);

        // Atualizar alvo para a posição do rato
        targetPosition.set(app.mouseX, app.mouseY);
        
        // Desenhar alvo visualmente
        app.noStroke();
        app.fill(255, 0, 0);
        app.circle(targetPosition.x, targetPosition.y, 10);

        // --- LÓGICA DE ESCOLHA (SEEK vs ARRIVE) ---
        
        if (isArriveMode) {
            // MODO ARRIVE
            float arrivalRadius = 300f; // Antes: R
            
            // Desenhar o círculo branco de travagem
            app.noFill();
            app.stroke(255, 100);
            app.circle(targetPosition.x, targetPosition.y, arrivalRadius * 2);
            
            boid.arrive(targetPosition, arrivalRadius);
            
            // Texto informativo
            app.fill(0, 255, 0);
            app.text("MODO ATUAL: ARRIVE (Chegada Suave)", 10, 100);
            
        } else {
            // MODO SEEK
            boid.seek(targetPosition);
            
            // Texto informativo
            app.fill(255, 165, 0); // Laranja
            app.text("MODO ATUAL: SEEK (Perseguição Rápida)", 10, 100);
        }

        // -------------------------------------------

        // Travão Manual
        if (app.keyPressed && (app.key == 't' || app.key == 'T')) {
            boid.brake();
            app.fill(255, 0, 0);
            app.text("!!! A TRAVAR !!!", 10, 80);
        }

        boid.move(secondsElapsed);
        boid.display();
        
        // Interface (HUD) - Atualizado com os novos Getters do Boid
        app.fill(255);
        app.text("Velocidade: " + (int)boid.getVelocity().mag() + " / " + (int)boid.getMaximumSpeed(), 10, 20);
        app.text("Força Máx: " + (int)boid.getMaximumForce(), 10, 40);
        app.text("[ESPAÇO] para trocar de modo", 10, 60);
    }

    @Override
    public void mousePressed(PApplet app) {}
    
    @Override
    public void keyPressed(PApplet app) {
        // --- TROCAR DE MODO ---
        if (app.key == ' ') { 
            isArriveMode = !isArriveMode; // Inverte o estado
        }
        
        // Controlos (Atualizado com os novos Setters/Getters)
        if (app.keyCode == PApplet.UP) {
            boid.setMaximumSpeed(boid.getMaximumSpeed() + 10f);
        }
        if (app.keyCode == PApplet.DOWN) {
            boid.setMaximumSpeed(boid.getMaximumSpeed() - 10f);
        }
        if (app.keyCode == PApplet.RIGHT) {
            boid.setMaximumForce(boid.getMaximumForce() + 10f);
        }
        if (app.keyCode == PApplet.LEFT) {
            boid.setMaximumForce(boid.getMaximumForce() - 10f);
        }
    }

    @Override
    public void mouseMoved(PApplet app) {}
    
    @Override
    public void keyReleased(PApplet app) {}
}