package apps;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Face {
    private PVector position;
    private float raio;

    public Face(PVector position, float raio) {
        this.position = position;
        this.raio = raio;
    }

    public void display(PApplet parent) {
        // face exterior
        parent.translate(position.x,position.y);
        parent.fill(255,224,189);
        parent.circle(0, 0, 2*raio);
        // boca
        parent.arc(0,0, 2*raio, raio,PApplet.radians(60),PApplet.radians(120), PConstants.CHORD);
        // nariz
        parent.circle(0, 0, raio/20f);
        // olho direito
        parent.fill(0,0,255);
        parent.translate(0.3f * raio, -0.4f * raio);
        parent.circle(0,0,raio/5f);
        parent.translate(-0.6f * raio, 0);
        parent.circle(0,0,raio/5f);

    }
}
