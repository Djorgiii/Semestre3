package physics;
import processing.core.PApplet;

public class Water extends Fluid{
    private float h;
    private PApplet p;
    private int color;

    public Water(PApplet p, float waterHeight, float scaling){
        super(1000.0f * scaling);
        this.p = p;
        this.h = waterHeight;
        this.color = p.color(0, 255, 255);
    }

    public boolean isInside(Mover m){
        return m.getPos().y > p.height - h;
    }
    public void display(){
        p.pushStyle();
        p.fill(color);
        p.rect(0, p.height - h, p.width, h);
        p.popStyle();
    }
}