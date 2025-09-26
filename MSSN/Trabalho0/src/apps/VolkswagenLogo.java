package apps;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;

import java.util.ArrayList;

public class VolkswagenLogo implements iProcessing {
    public void mouseMoved(PApplet parent) {

    }

    @Override
    public void setup(processing.core.PApplet parent) {
    }

    @Override
    public void draw(processing.core.PApplet parent, float dt) {

        parent.background(255);


    }

    @Override
    public void keyPressed(processing.core.PApplet parent) {

    }

    @Override
    public void mousePressed(processing.core.PApplet parent) {

        // no programa do prof ele meteu o clickPos como cp O.o
        PVector clickPos = new PVector(parent.mouseX, parent.mouseY);
        boolean anyface= false;

        if(parent.mouseButton == PApplet.RIGHT) { //quando clicar como botao direito
            //mudar a cor dos olhos

            Face f= new Face(clickPos, 30 ,parent);
            faces.add(f);

        }else {

            for (int i= faces.size()-1; i >= 0; i--) {
                Face f = faces.get(i);
                if(f.isInside(clickPos)) {
                    anyface= true;
                    faces.remove(f);
                    //PApplet.println("ta dentro filho");
                }else {
                    f.setTarget(clickPos);
                }
            }
            if(!anyface && faces.size()>0) {
                Face f= faces.get(0);
                f.setTarget(clickPos);

            }
        }

    }
}
