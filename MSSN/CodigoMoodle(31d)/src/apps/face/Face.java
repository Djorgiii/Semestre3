package apps.face;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Face {
    private PVector pos;
    private float radius;
    private PVector target;
    private int faceColor;

    public Face(PVector pos, float radius)
    {
        this.pos = pos;
        this.radius = radius;
    }

    public void setColor(int c)
    {
        this.faceColor = c;
    }

    public void setTarget(PVector target)
    {
        this.target = target;
    }

    public void move(PVector d)
    {
        pos.add(d);
    }

    public boolean isInside(PVector clickPos)
    {
        float d = PVector.dist(clickPos, pos);
        return (d < radius);
    }

    public void moveToTarget()
    {
        if (target == null)
           return;
        float alpha = 0.01f;
        PVector d = PVector.sub(target, pos);
        pos.add(d.mult(alpha));
    }

    public void display(PApplet p)
    {
        p.pushMatrix();

        p.translate(pos.x, pos.y);
        //face
        p.fill(faceColor);
        p.fill(this.faceColor);
        p.circle(0, 0, 2f * radius);
        //nose
        p.circle(0, 0, radius / 20f);
        //mouth
        p.fill(200, 50, 0);
        p.arc(0, 0, 2*radius, radius,
                PApplet.radians(60f),
                PApplet.radians(120f),PConstants.CHORD);
        //eyes
        p.translate(0.3f * radius, -0.4f * radius);
        p.fill(0, 200, 200);
        p.circle(0, 0, radius / 5f);
        p.translate(-2 * 0.3f * radius, 0);
        p.circle(0, 0, radius / 5f);

        p.popMatrix();
    }
}
