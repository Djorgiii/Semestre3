package SolarSystem;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;
import java.util.ArrayList;

public class SolarSystemSim implements iProcessing {

    // Variáveis da Simulação
    private Body star;
    private ArrayList<Body> planets;
    private ParticleSystem sunCorona;

    // Variável para o fundo de estrelas
    private ArrayList<PVector> starfield;
    private final int numBackgroundStars = 500;
    // ---

    // Parâmetros Físicos
    private final float G = 30f;
    private final float starMass = 30000;
    private final int numPlanets = 8;
    private final int stepsPerFrame = 10;

    @Override
    public void setup(PApplet p) {
        planets = new ArrayList<Body>();
        PVector starPos = new PVector(p.width / 2.0f, p.height / 2.0f);

        // 1. Criar o Sol
        star = new Body(p, starPos, new PVector(0, 0), starMass, 25, p.color(255, 255, 0));
        
        // 2. Inicializar a coroa do Sol
        sunCorona = new ParticleSystem();

        // 3. Criar N planetas
        for (int i = 0; i < numPlanets; i++) {
            float orbitalRadius = 100 + i * 40;
            PVector planetPos = new PVector(starPos.x + orbitalRadius, starPos.y);
            float planetMass = p.random(5, 15);
            float planetRadius = p.random(4, 9);
            int planetColor = p.color(p.random(100, 255), p.random(100, 255), p.random(100, 255));
            float velMagnitude = (float) Math.sqrt((G * starMass) / orbitalRadius);
            PVector planetVel = new PVector(0, -velMagnitude);
            Body newPlanet = new Planet(p, planetPos, planetVel, planetMass, planetRadius, planetColor);
            planets.add(newPlanet);
        }

        // Gerar o fundo de estrelas
        starfield = new ArrayList<PVector>();
        for (int i = 0; i < numBackgroundStars; i++) {
            PVector starPosBg = new PVector(p.random(p.width), p.random(p.height));
            starfield.add(starPosBg);
        }
        // ---
    }

    @Override
    public void draw(PApplet p, float dt) {
        p.background(0);
        p.pushStyle();
        p.stroke(255, 200);
        for (PVector starPosBg : starfield) {
            p.strokeWeight(p.random(0.5f, 1.5f));
            p.point(starPosBg.x, starPosBg.y);
        }
        p.popStyle();

        
        float subStep_dt = dt / (float) stepsPerFrame;
        for (int i = 0; i < stepsPerFrame; i++) {
            sunCorona.addSunParticle(p, star.getPos()); 
            sunCorona.update(subStep_dt); 

            for (Body planet : planets) {
                PVector force = calculateGravitationalForce(star, planet);
                planet.applyForce(force);
                planet.move(subStep_dt); 
            }
        }

        sunCorona.display(p); 
        star.display();
        
        for (Body planet : planets) {
            planet.display();
        }
    }


    private PVector calculateGravitationalForce(Body b1, Body b2) {
        PVector pos1 = b1.getPos();
        PVector pos2 = b2.getPos();
        PVector force = PVector.sub(pos1, pos2);
        float distance = force.mag();
        float minDistance = b1.getRadius() + b2.getRadius();
        distance = PApplet.constrain(distance, minDistance, 2000f);
        float strength = (G * b1.getMass() * b2.getMass()) / (distance * distance);
        force.normalize();
        force.mult(strength);
        return force;
    }

    @Override
    public void keyPressed(PApplet parent) {}
    @Override
    public void mousePressed(PApplet parent) {}
    @Override
    public void mouseMoved(PApplet parent) {}
}