package flock;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;
import java.util.ArrayList;

public class FlockApp implements iProcessing {

    private ArrayList<Boid> flockList;      // Antes: flock
    private Boid leaderBoid;                // Antes: leader
    private ParticleSystem explosionSystem; // Antes: explosions
    private PVector mouseTarget;            // Antes: target

    // Estado: false = Explosivo, true = Liderança
    private boolean isLeadershipMode = false; 
    
    // Variáveis para movimento suave (WASD)
    private boolean isMovingUp, isMovingLeft, isMovingDown, isMovingRight;

    @Override
    public void setup(PApplet app) { // Antes: p -> app
        flockList = new ArrayList<Boid>();
        explosionSystem = new ParticleSystem();
        mouseTarget = new PVector(0,0);

        // 1. Configurar o Líder (Vermelho, Imortal)
        PVector screenCenter = new PVector(app.width/2, app.height/2);
        leaderBoid = new Boid(app, screenCenter, 1, 15, app.color(255, 0, 0));
        leaderBoid.setLifeTimer(999999); // Imortal (Antes: setTimer)

        // 2. Criar o enxame inicial
        resetFlock(app);
    }

    // Função para reiniciar o enxame quando trocamos de modo
    private void resetFlock(PApplet app) {
        flockList.clear();
        
        // Criamos 20 boids para a cobra, ou 15 para as explosões
        int numberOfBoids = isLeadershipMode ? 20 : 15;
        
        for (int i = 0; i < numberOfBoids; i++) { 
            createRandomBoid(app);
        }
    }

    private void createRandomBoid(PApplet app) {
        PVector randomPos = new PVector(app.random(app.width), app.random(app.height));
        int boidColor = app.color(0, 200, 255); 
        Boid newBoid = new Boid(app, randomPos, 1, 8, boidColor);
        flockList.add(newBoid);
    }

    @Override
    public void draw(PApplet app, float secondsElapsed) { // Antes: dt -> secondsElapsed
        app.background(30);

        if (isLeadershipMode) {
            // ==================================================
            // MODO 2: LIDERANÇA (WASD)
            // ==================================================
            
            // --- 1. CONTROLAR O LÍDER (Movimento Fluido) ---
            PVector inputVelocity = new PVector(0, 0);
            
            if (isMovingUp)    inputVelocity.y -= 1;
            if (isMovingDown)  inputVelocity.y += 1;
            if (isMovingLeft)  inputVelocity.x -= 1;
            if (isMovingRight) inputVelocity.x += 1;
            
            if (inputVelocity.mag() > 0) {
                inputVelocity.setMag(200); // Velocidade constante do líder
                leaderBoid.setVelocityManually(inputVelocity); // Antes: setVelocity
            } else {
                leaderBoid.setVelocityManually(new PVector(0,0)); // Para se largar as teclas
            }
            
            leaderBoid.move(secondsElapsed);
            leaderBoid.display(); 

            // --- 2. BOIDS SEGUEM O LÍDER ---
            for (Boid currentBoid : flockList) {
                
                // Calcular distância para não bater no líder
                float distToLeader = PVector.dist(currentBoid.getPosition(), leaderBoid.getPosition());
                
                // Zona de Respeito: Só acelera se estiver a mais de 40px
                if (distToLeader > 40) {
                    // FALSE = Não usar Snap (não colar)
                    // Antes: arrive(...) -> Agora: applyArriveBehavior(...)
                    currentBoid.applyArriveBehavior(leaderBoid.getPosition(), 100f, false); 
                }

                // --- APLICAR SEPARAÇÃO ---
                // Antes: separate(...) -> Agora: calculateSeparationForce(...)
                PVector separationForce = currentBoid.calculateSeparationForce(flockList);
                separationForce.mult(2.0f); // Prioridade à separação
                currentBoid.applyForce(separationForce);
                
                currentBoid.move(secondsElapsed);
                currentBoid.display();
            }
            
            // HUD
            app.fill(255);
            app.text("MODO: LIDERANÇA (WASD)", 10, 20);
            app.text("Seguidores: " + flockList.size(), 10, 40);

        } else {
            // ==================================================
            // MODO 1: EXPLOSIVOS (Rato + Timer)
            // ==================================================
            
            mouseTarget.set(app.mouseX, app.mouseY);
            app.fill(255, 100);
            app.circle(mouseTarget.x, mouseTarget.y, 10);

            for (int i = flockList.size() - 1; i >= 0; i--) {
                Boid currentBoid = flockList.get(i);
                
                // TRUE = Usar Snap (colar no rato)
                currentBoid.applyArriveBehavior(mouseTarget, 200f, true); 
                
                currentBoid.move(secondsElapsed);
                currentBoid.decreaseLifeTimer(secondsElapsed); // Antes: decreaseTimer

                if (currentBoid.isDead()) {
                    // Antes: explode -> Agora: explode (no explosionSystem atualizado)
                    explosionSystem.explode(app, currentBoid.getPosition());
                    flockList.remove(i);
                    // createRandomBoid(app); // Descomente para repovoar automático
                } else {
                    currentBoid.display();
                }
            }
            
            // HUD
            app.fill(255);
            app.text("MODO: EXPLOSIVO (Timer)", 10, 20);
            app.text("Boids: " + flockList.size(), 10, 40);
            app.text("Clique para adicionar", 10, 60);
        }

        // --- 3. ATUALIZAR EXPLOSÕES ---
        explosionSystem.update(secondsElapsed);
        explosionSystem.display(app);

        app.fill(255);
        app.text("[ESPAÇO] para Trocar de Modo", 10, app.height - 20);
    }

    @Override
    public void keyPressed(PApplet app) {
        // Trocar de Modo e Resetar
        if (app.key == ' ') {
            isLeadershipMode = !isLeadershipMode;
            resetFlock(app); 
            leaderBoid.setVelocityManually(new PVector(0,0));
        }
        
        // Teclas WASD (Ativar flags)
        if (app.key == 'w' || app.key == 'W') isMovingUp = true;
        if (app.key == 's' || app.key == 'S') isMovingDown = true;
        if (app.key == 'a' || app.key == 'A') isMovingLeft = true;
        if (app.key == 'd' || app.key == 'D') isMovingRight = true;
    }
    
    @Override
    public void keyReleased(PApplet app) {
        // Teclas WASD (Desativar flags)
        if (app.key == 'w' || app.key == 'W') isMovingUp = false;
        if (app.key == 's' || app.key == 'S') isMovingDown = false;
        if (app.key == 'a' || app.key == 'A') isMovingLeft = false;
        if (app.key == 'd' || app.key == 'D') isMovingRight = false;
    }

    @Override
    public void mousePressed(PApplet app) {
        for(int i=0; i<5; i++) createRandomBoid(app);
    }
    
    @Override
    public void mouseMoved(PApplet app) {}
}