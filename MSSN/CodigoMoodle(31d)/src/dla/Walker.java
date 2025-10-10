package dla;

import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;

public class Walker {
    
    public enum State {
        STOPPED,
        WANDER
    }

    private PVector pos;
    private State state;
    private int colour;
    private static int radius = 1;

    public Walker(PApplet p)
    {
        //pos = new PVector(p.random(p.width), p.random(p.height));
        pos = new PVector(p.width / 2, p.height / 2);
        PVector d = PVector.random2D();
        pos.add(d.mult(p.height / 2));

        setState(p, State.WANDER);
    }

    public Walker(PApplet p, PVector pos) {
        this.pos = pos;

        setState(p, State.STOPPED);
    }

    public void setState(PApplet p, State state)
    {
        this.state = state;
        if (state == State.STOPPED)
        {
            colour = p.color(0);
        }
        else
        {
            colour = p.color(255);
        }

    }

    public State getState()
    {
        return state;
    }

    public void wander(PApplet p)
    {
        PVector step = PVector.random2D();
        pos.add(step);

        pos.lerp(new PVector(p.width / 2, p.height / 2), 0.0002f);
    }

    public void updateState(PApplet p, List<Walker> walkers)
    {
        /*if (state == State.STOPPED)
            return;*/
        
        for (Walker w : walkers)
        {
            if (w.state == State.STOPPED) {
                float dist = PVector.dist(this.pos, w.pos);
                if (dist < 2 * radius) {
                    setState(p, State.STOPPED);
                    break;
                }
            }
        }
    }

    public void display(PApplet p)
    {
        p.fill(colour);
        p.circle(pos.x, pos.y, 2*radius);
    }
}
