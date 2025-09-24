package apps;

import processing.core.PApplet;
import processing.core.PVector;
import setup.IProcessing;

public class HelloFaceApp implements IProcessing {
    @Override
    public void setup(PApplet parent) {
        PVector position = new PVector(400, 300);
        Face f = new Face(position,300);
        f.display(parent);
    }

    @Override
    public void draw(PApplet parent, float dt) {

    }

    @Override
    public void keyPressed(PApplet parent) {

    }

    @Override
    public void mousePressed(PApplet parent) {
        PVector position = new PVector(parent.mouseX,parent.mouseY);
        Face f = new Face(position, parent.random(200));
        f.display(parent);
    }
}
