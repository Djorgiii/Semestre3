package lsystem;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;

public class LSystemApp implements iProcessing {

    private LSystem sistema;
    private AnimatedTurtle turtle;
    private int modo = 1;

    @Override
    public void setup(PApplet p) {
        p.background(0);
        carregarSistema(p);
    }

    private void carregarSistema(PApplet p) {

        switch (modo) {

            case 1:
                sistema = new LSystem("X");
                sistema.addRule('X', "F[+X][-X]FX");
                sistema.addRule('F', "FF");
                sistema.iterate(6);
                turtle = new AnimatedTurtle(sistema.getString(), 5, 25, p);
                break;

            case 2:
                sistema = new LSystem("F--F--F");
                sistema.addRule('F', "F+F--F+F");
                sistema.iterate(4);
                turtle = new AnimatedTurtle(
                	    sistema.getString(),
                	    3,                  // step
                	    60,                 // angle
                	    p,
                	    new PVector(p.width/2f - 200, p.height/2f),   // START POSITION
                	    0                   // heading → desenhar para a direita
                	);
                break;
        }
    }

    @Override
    public void draw(PApplet p, float dt) {

        turtle.drawNext(p);
        
        p.fill(255);
        p.textSize(24);
        p.text("Modo: " + (modo == 1 ? "Planta Fractal" : "Floco de Neve de Koch"), 20, 30);
        p.text("Pressione '1' ou '2' para mudar o modo", 20, 60);
    }

    @Override
    public void keyPressed(PApplet p) {

        switch (p.key) {

            case '1':
                modo = 1;
                p.background(0);
                carregarSistema(p);
                break;

            case '2':
                modo = 2;
                p.background(0);
                carregarSistema(p);
                break;

            case 'r':
            case 'R':
                p.background(0);
                carregarSistema(p);
                break;
        }
    }

    @Override public void mousePressed(PApplet p) {}
    @Override public void keyReleased(PApplet p) {}
    @Override public void mouseMoved(PApplet p) {}
}
