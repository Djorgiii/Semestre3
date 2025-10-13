package apps.face;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;
import setup.IProcessingApp;

public class FaceApp implements IProcessingApp 
{
    private final float DELTA_T = 2.0f;
    private float timer;
    private float fct = 1.05f;
    private float r = 50f;
    private List<Face> faces;

    @Override
    public void setup(PApplet parent) {
        faces = new ArrayList<Face>();
        timer = 0;
    }

    @Override
    public void draw(PApplet parent, float dt) {
        parent.background(200);
        timer += dt;
        if (timer >= DELTA_T) 
        {
            float xr = parent.random(0, parent.width);
            float yr = parent.random(0, parent.height);
            r *= fct;
            Face f = new Face(new PVector(xr, yr), r);
            faces.add(f);
            timer = 0f;
        }

        for (Face f : faces) {
            float dx = parent.random(0, 0.2f);
            float dy = parent.random(0, 0.2f);
            f.move(new PVector(dx, dy));
        }
        for (Face f : faces)
        {
            f.display(parent);
        }
    }

    @Override
    public void keyPressed(PApplet parent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'keyPressed'");
    }

    @Override
    public void mousePressed(PApplet parent) 
    {

    }
}
