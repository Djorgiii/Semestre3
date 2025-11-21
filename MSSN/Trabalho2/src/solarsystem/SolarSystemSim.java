package solarsystem;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;
import java.util.ArrayList;

public class SolarSystemSim implements iProcessing {

    // Variáveis da Simulação
    private Body centralStar;                   // Antes: star
    private ArrayList<Body> planetList;         // Antes: planets
    private ParticleSystem sunCoronaSystem;     // Antes: sunCorona

    // Variável para o fundo de estrelas
    private ArrayList<PVector> backgroundStarPositions; // Antes: starfield
    private final int numBackgroundStars = 500;
    
    // Parâmetros Físicos
    private final float gravitationalConstant = 30f; // Antes: G
    private final float starMass = 30000;
    private final int numberOfPlanets = 8;      // Antes: numPlanets
    private final int stepsPerFrame = 10;

    @Override
    public void setup(PApplet app) { // Antes: p -> app
        planetList = new ArrayList<Body>();
        PVector starPosition = new PVector(app.width / 2.0f, app.height / 2.0f); // Antes: starPos

        // 1. Criar o Sol
        centralStar = new Body(app, starPosition, new PVector(0, 0), starMass, 25, app.color(255, 255, 0));
        
        // 2. Inicializar a coroa do Sol
        sunCoronaSystem = new ParticleSystem();

        // 3. Criar N planetas
        for (int i = 0; i < numberOfPlanets; i++) {
            float orbitalRadius = 100 + i * 40;
            
            PVector planetPosition = new PVector(starPosition.x + orbitalRadius, starPosition.y);
            
            float planetMass = app.random(5, 15);
            float planetRadius = app.random(4, 9);
            int planetColor = app.color(app.random(100, 255), app.random(100, 255), app.random(100, 255));
            
            // Fórmula da velocidade orbital
            float velocityMagnitude = (float) Math.sqrt((gravitationalConstant * starMass) / orbitalRadius);
            PVector planetVelocity = new PVector(0, -velocityMagnitude);
            
            // Criar e adicionar (usando a classe Planet que tem rasto)
            Body newPlanet = new Planet(app, planetPosition, planetVelocity, planetMass, planetRadius, planetColor);
            planetList.add(newPlanet);
        }

        // Gerar o fundo de estrelas
        backgroundStarPositions = new ArrayList<PVector>();
        for (int i = 0; i < numBackgroundStars; i++) {
            PVector starPositionBg = new PVector(app.random(app.width), app.random(app.height));
            backgroundStarPositions.add(starPositionBg);
        }
    }

    @Override
    public void draw(PApplet app, float secondsElapsed) { // Antes: dt -> secondsElapsed
        app.background(0);
        
        // Desenhar fundo de estrelas
        app.pushStyle();
        app.stroke(255, 200);
        for (PVector starPositionBg : backgroundStarPositions) {
            app.strokeWeight(app.random(0.5f, 1.5f));
            app.point(starPositionBg.x, starPositionBg.y);
        }
        app.popStyle();

        
        float subStepSecondsElapsed = secondsElapsed / (float) stepsPerFrame;
        
        // Loop de Física (Sub-stepping)
        for (int i = 0; i < stepsPerFrame; i++) {
            // Atualizar partículas do Sol
            sunCoronaSystem.addSunParticle(app, centralStar.getPosition()); 
            sunCoronaSystem.update(subStepSecondsElapsed); 

            // Atualizar Planetas
            for (Body planet : planetList) {
                PVector gravitationalForce = calculateGravitationalForce(centralStar, planet);
                planet.applyForce(gravitationalForce);
                planet.move(subStepSecondsElapsed); 
            }
        }

        // Desenho
        sunCoronaSystem.display(app); 
        centralStar.display();
        
        for (Body planet : planetList) {
            planet.display();
        }
    }


    private PVector calculateGravitationalForce(Body body1, Body body2) {
        PVector position1 = body1.getPosition();
        PVector position2 = body2.getPosition();
        
        PVector forceVector = PVector.sub(position1, position2);
        float distance = forceVector.mag();
        
        float minDistance = body1.getRadius() + body2.getRadius();
        distance = PApplet.constrain(distance, minDistance, 2000f);
        
        float forceMagnitude = (gravitationalConstant * body1.getMass() * body2.getMass()) / (distance * distance);
        
        forceVector.normalize();
        forceVector.mult(forceMagnitude);
        
        return forceVector;
    }

    @Override
    public void keyPressed(PApplet app) {}
    @Override
    public void mousePressed(PApplet app) {}
    @Override
    public void mouseMoved(PApplet app) {}
    @Override
    public void keyReleased(PApplet app) {}
}