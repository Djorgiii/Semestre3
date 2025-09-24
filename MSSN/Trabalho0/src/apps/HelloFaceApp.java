package apps;

import processing.core.PApplet;
import processing.core.PVector;
import setup.IProcessing;

public class HelloFaceApp implements IProcessing 
{
    private Face f;

    @Override
    public void setup(PApplet parent) 
    {
        PVector pos = new PVector(200, 300);
        float r = parent.random(20, 100);
        f = new Face(pos, r, parent);
    }

    @Override
    public void draw(PApplet parent, float dt) 
    {
        f.display(parent);
    }

    @Override
    public void keyPressed(PApplet parent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyPressed'");
    }

    @Override
    public void mousePressed(PApplet parent) 
    {
        int c = parent.color(255, 0, 0);
        f.setEyeColor(c);
    }
    
}
