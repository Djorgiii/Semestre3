package apps.face;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;

import setup.IProcessingApp;

public class FaceApp2 implements IProcessingApp 
{
    private List<Face> faces;
    private int NumFaces = 5;

    @Override
    public void setup(PApplet parent) 
    {
        faces = new ArrayList<Face>();
        for(int i=0; i<NumFaces;i++)
        {
            float r = parent.random(20f, 60f);
            float x = parent.random(r, parent.width-r);
            float y = parent.random(r, parent.height-r);

            Face f = new Face(new PVector(x, y), r);
            faces.add(f);
        }
    }

    @Override
    public void draw(PApplet parent, float dt) 
    {
        parent.background(200);

        /*for (Face f : faces) {
            f.moveToTarget();
        }*/

        for(int i=faces.size()-1; i>=0; i--)
        {
            Face f = faces.get(i);
            f.display(parent);
        }

    }

    @Override
    public void keyPressed(PApplet parent) {
       
    }

    @Override
    public void mousePressed(PApplet parent) 
    {
        /*for (Face f : faces) {
            f.setTarget(new PVector(parent.mouseX,
                    parent.mouseY));
        }*/

        /*for (int i = faces.size() - 1; i >= 0; i--)
        {
            Face f = faces.get(i);
            boolean kill = f.isInside(new PVector(parent.mouseX,
                    parent.mouseY));
            if (kill)
                faces.remove(f);
        }*/

         for (int i = 0; i < faces.size(); i++)
         {
            Face f = faces.get(i);
            boolean changeColor = f.isInside(new PVector(parent.mouseX,
                    parent.mouseY));
            if (changeColor)
            {
                int c = parent.color(
                    parent.random(255),
                    parent.random(255),
                    parent.random(255)           
                );
                f.setColor(c);
            }
         }
         

    }
}
