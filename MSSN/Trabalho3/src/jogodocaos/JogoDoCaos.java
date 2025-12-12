package jogodocaos;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;

public class JogoDoCaos implements iProcessing {

    private ArrayList<PVector> V;
    private PVector X;
    private PApplet p;

    @Override
    public void setup(PApplet p) {
        this.p = p;

        p.background(0);
        p.strokeWeight(2);

        initializeTriangle();
        initializePointX();
        drawTriangleAndLabels();
    }

    @Override
    public void draw(PApplet p, float dt) {


        int t = (int)p.random(V.size());
        PVector T = V.get(t);

        X.x = X.x + 0.5f * (T.x - X.x);
        X.y = X.y + 0.5f * (T.y - X.y);

        if (t == 0) p.stroke(255,   0,  0);
        if (t == 1) p.stroke(  0, 255,  0);
        if (t == 2) p.stroke(  0, 150,255);

        p.point(X.x, X.y);
    }


    private void initializeTriangle() {
        V = new ArrayList<>();

        float side = p.width * 0.75f;
        float h = (float)(Math.sqrt(3) / 2 * side);

        // A
        V.add(new PVector(p.width/2f, p.height/2f - h/2f));
        // B
        V.add(new PVector(p.width/2f - side/2f, p.height/2f + h/2f));
        // C
        V.add(new PVector(p.width/2f + side/2f, p.height/2f + h/2f));
    }

    private void initializePointX() {
        X = new PVector(
            (V.get(0).x + V.get(1).x + V.get(2).x) / 3f,
            (V.get(0).y + V.get(1).y + V.get(2).y) / 3f
        );
    }

    private void drawTriangleAndLabels() {
        p.stroke(255);

        p.line(V.get(0).x, V.get(0).y, V.get(1).x, V.get(1).y);
        p.line(V.get(1).x, V.get(1).y, V.get(2).x, V.get(2).y);
        p.line(V.get(2).x, V.get(2).y, V.get(0).x, V.get(0).y);

        p.fill(255);
        p.textSize(24);
        p.text("B", V.get(0).x + 10, V.get(0).y - 10);
        p.text("A", V.get(1).x - 25, V.get(1).y + 25);
        p.text("C", V.get(2).x + 10, V.get(2).y + 25);
    }

    @Override
    public void keyPressed(PApplet p) {
        if (p.key == 'r' || p.key == 'R') {

            p.background(0);
            drawTriangleAndLabels();

            initializePointX();
        }
    }

    @Override
    public void mousePressed(PApplet p) {}

	@Override
	public void keyReleased(PApplet parent) {
		
	}

	@Override
	public void mouseMoved(PApplet parent) {		
	}
}
