package physics;
import processing.core.PVector;
import  processing.core.PApplet;

public abstract class Fluid {
    private float density;

    protected Fluid(float density) {;
        this.density = density;
    }

    public PVector drag(Mover m) {
        float area = PApplet.pow(m.getRadius(),2.0f) * PApplet.PI;
        float mag = m.getVel().mag();
        return PVector.mult(m.getVel(), -0.5f * density * mag * area);
    }
}