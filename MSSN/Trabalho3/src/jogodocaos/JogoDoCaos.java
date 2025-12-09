package jogodocaos;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;

public class JogoDoCaos implements iProcessing {

    private PVector[] vertices;
    private PVector ponto;
    private boolean initialized = false;

    @Override
    public void setup(PApplet parent) {
        parent.background(0);
        parent.stroke(255);
        parent.strokeWeight(2);

        // --- Define os 3 vértices (triângulo) ---
        vertices = new PVector[3];
        vertices[0] = new PVector(parent.width / 2f, 50);
        vertices[1] = new PVector(100, parent.height - 100);
        vertices[2] = new PVector(parent.width - 100, parent.height - 100);

        // Ponto inicial aleatório
        ponto = new PVector(parent.random(parent.width), parent.random(parent.height));

        // Desenha vértices
        for (PVector v : vertices) {
            parent.point(v.x, v.y);
        }

        initialized = true;
    }

    @Override
    public void draw(PApplet parent, float dt) {
        if (!initialized) return;

        // Escolher vértice aleatório
        int alvo = (int) parent.random(vertices.length);

        // Mover para metade da distância
        ponto.x = PApplet.lerp(ponto.x, vertices[alvo].x, 0.5f);
        ponto.y = PApplet.lerp(ponto.y, vertices[alvo].y, 0.5f);

        // Desenhar ponto
        parent.point(ponto.x, ponto.y);
    }

    @Override
    public void keyPressed(PApplet parent) {
        // Reset com 'R'
        if (parent.key == 'r' || parent.key == 'R') {
            parent.background(0);
            ponto = new PVector(parent.random(parent.width), parent.random(parent.height));
        }
    }

    @Override
    public void mousePressed(PApplet parent) {
        // Redefinir ponto inicial com clique
        ponto = new PVector(parent.mouseX, parent.mouseY);
    }

    @Override
    public void keyReleased(PApplet parent) {}

    @Override
    public void mouseMoved(PApplet parent) {}
}
