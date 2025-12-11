package lsystem;

import processing.core.PApplet;
import setup.iProcessing;

public class JogoLSystem implements iProcessing {

    private LSystem sistema;
    private int modo = 0; // 0 = Koch, 1 = Planta, 2 = Árvore

    @Override
    public void setup(PApplet p) {
        p.background(0);
        carregarSistema();
    }

    private void carregarSistema() {

        switch (modo) {
            case 0:
                sistema = FractalExamples.koch();
                break;

            case 1:
                sistema = FractalExamples.planta();
                break;

            case 2:
                sistema = ArvoreFrutos.gerar();
                break;

            default:
                sistema = FractalExamples.koch();
                break;
        }
    }

    @Override
    public void draw(PApplet p, float dt) {

        p.background(0);

        float step = 5;
        float angle = 25;  // default

        switch (modo) {
            case 0:
                angle = 60; // Koch
                TurtleDrawer.draw(p, sistema.getString(), step, angle);
                break;

            case 1:
                angle = 25; // Planta
                TurtleDrawer.draw(p, sistema.getString(), step, angle);
                break;

            case 2:
                angle = 25; // Árvore de frutos
                TurtleDrawer.drawFruitTree(p, sistema.getString(), step, angle);
                break;
        }
    }


    @Override
    public void keyPressed(PApplet p) {

        switch (p.key) {

            case '1':
                modo = 0;
                carregarSistema();
                break;

            case '2':
                modo = 1;
                carregarSistema();
                break;

            case '3':
                modo = 2;
                carregarSistema();
                break;

            case 'r':
            case 'R':
                carregarSistema();
                break;
        }

        p.background(0);
    }

    @Override public void mousePressed(PApplet p) {}
    @Override public void keyReleased(PApplet p) {}
    @Override public void mouseMoved(PApplet p) {}
}
