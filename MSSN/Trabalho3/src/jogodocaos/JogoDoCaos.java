package jogodocaos;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;

public class JogoDoCaos implements iProcessing {

    private PVector[] vertices;
    private PVector X;

    @Override
    public void setup(PApplet parent) {
        parent.background(0);
        parent.strokeWeight(2);

        float side = parent.width * 0.75f;
        float h = (float)(Math.sqrt(3) / 2 * side);

        vertices = new PVector[3];
        vertices[0] = new PVector(parent.width / 2f, parent.height / 2f - h / 2f);
        vertices[1] = new PVector(parent.width / 2f - side / 2f, parent.height / 2f + h / 2f);
        vertices[2] = new PVector(parent.width / 2f + side / 2f, parent.height / 2f + h / 2f);

        X = PVector.add(vertices[0], vertices[1]);
        X.add(vertices[2]);
        X.div(3);

        drawTriangleAndLabels(parent);
    }

    private void drawTriangleAndLabels(PApplet parent) {
        parent.stroke(255);
        parent.strokeWeight(2);

        parent.line(vertices[0].x, vertices[0].y, vertices[1].x, vertices[1].y);
        parent.line(vertices[1].x, vertices[1].y, vertices[2].x, vertices[2].y);
        parent.line(vertices[2].x, vertices[2].y, vertices[0].x, vertices[0].y);

        parent.fill(255);
        parent.textSize(24);

        parent.text("A", vertices[0].x + 10, vertices[0].y - 10);
        parent.text("B", vertices[1].x - 30, vertices[1].y + 25);
        parent.text("C", vertices[2].x + 10, vertices[2].y + 25);

        parent.stroke(255);
    }

    @Override
    public void draw(PApplet parent, float dt) {

        int t = (int) parent.random(3);
        PVector T = vertices[t];

        X.x = X.x + 0.5f * (T.x - X.x);
        X.y = X.y + 0.5f * (T.y - X.y);

        if (t == 0) parent.stroke(0,150,0);
        if (t == 1) parent.stroke(120,40,190);
        if (t == 2) parent.stroke(230,150,20);
        parent.point(X.x, X.y);
    }

    @Override
    public void keyPressed(PApplet parent) {

        if (parent.key == 'r' || parent.key == 'R') {
            parent.background(0);

            drawTriangleAndLabels(parent);

            X = PVector.add(vertices[0], vertices[1]);
            X.add(vertices[2]);
            X.div(3);
        }
    }

    @Override public void mousePressed(PApplet parent) {}
    @Override public void keyReleased(PApplet parent) {}
    @Override public void mouseMoved(PApplet parent) {}
}
