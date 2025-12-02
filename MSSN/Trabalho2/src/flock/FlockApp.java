package flock;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;
import java.util.ArrayList;

public class FlockApp implements iProcessing {

    private ArrayList<Boid> flockList;
    private Boid leaderBoid;
    private ParticleSystem explosionSystem;
    private PVector mouseTarget;

    private boolean isLeadershipMode = false; 
    
    private boolean isMovingUp, isMovingLeft, isMovingDown, isMovingRight;

    @Override
    public void setup(PApplet app) {
        flockList = new ArrayList<Boid>();
        explosionSystem = new ParticleSystem();
        mouseTarget = new PVector(0,0);

        PVector screenCenter = new PVector(app.width/2, app.height/2);
        leaderBoid = new Boid(app, screenCenter, 1, 15, app.color(255, 0, 0));
        leaderBoid.setLifeTimer(999999);

        resetFlock(app);
    }

    private void resetFlock(PApplet app) {
        flockList.clear();
        
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
    public void draw(PApplet app, float secondsElapsed) {
        app.background(30);

        if (isLeadershipMode) {
            PVector inputVelocity = new PVector(0, 0);
            
            if (isMovingUp)    inputVelocity.y -= 1;
            if (isMovingDown)  inputVelocity.y += 1;
            if (isMovingLeft)  inputVelocity.x -= 1;
            if (isMovingRight) inputVelocity.x += 1;
            
            if (inputVelocity.mag() > 0) {
                inputVelocity.setMag(200);
                leaderBoid.setVelocityManually(inputVelocity);
            } else {
                leaderBoid.setVelocityManually(new PVector(0,0));
            }
            
            leaderBoid.move(secondsElapsed);
            leaderBoid.display(); 

            for (Boid currentBoid : flockList) {
                
                float distToLeader = PVector.dist(currentBoid.getPosition(), leaderBoid.getPosition());
                
                if (distToLeader > 40) {
                    currentBoid.applyArriveBehavior(leaderBoid.getPosition(), 100f, false); 
                }

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
            
            mouseTarget.set(app.mouseX, app.mouseY);
            app.fill(255, 100);
            app.circle(mouseTarget.x, mouseTarget.y, 10);

            for (int i = flockList.size() - 1; i >= 0; i--) {
                Boid currentBoid = flockList.get(i);
                
                currentBoid.applyArriveBehavior(mouseTarget, 200f, true); 
                
                currentBoid.move(secondsElapsed);
                currentBoid.decreaseLifeTimer(secondsElapsed);

                if (currentBoid.isDead()) {
                    explosionSystem.explode(app, currentBoid.getPosition());
                    flockList.remove(i);
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

        explosionSystem.update(secondsElapsed);
        explosionSystem.display(app);

        app.fill(255);
        app.text("[ESPAÇO] para Trocar de Modo", 10, app.height - 20);
    }

    @Override
    public void keyPressed(PApplet app) {
        if (app.key == ' ') {
            isLeadershipMode = !isLeadershipMode;
            resetFlock(app); 
            leaderBoid.setVelocityManually(new PVector(0,0));
        }
        
        if (app.key == 'w' || app.key == 'W') isMovingUp = true;
        if (app.key == 's' || app.key == 'S') isMovingDown = true;
        if (app.key == 'a' || app.key == 'A') isMovingLeft = true;
        if (app.key == 'd' || app.key == 'D') isMovingRight = true;
    }
    
    @Override
    public void keyReleased(PApplet app) {
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