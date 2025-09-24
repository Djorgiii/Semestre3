package apps;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Face 
{
    private PVector pos;
    private float radius;
    private int eyeColor;

    public Face(PVector pos, float radius, PApplet parent)
    {
        this.pos = pos;
        this.radius = radius;
        eyeColor = parent.color(0, 200, 200);
    }

    public void setEyeColor(int c)
    {
        eyeColor = c;
    }

    public void display(PApplet parent)
    {
        //face boundary
        parent.translate(pos.x, pos.y);
        parent.fill(180, 160, 90);
        parent.circle(0, 0, 2 * radius);
        //mouth
        parent.arc(0, 0, radius, radius, 
                PApplet.radians(60),
                PApplet.radians(120),
                PConstants.CHORD);
        // nose
        parent.fill(0);
        parent.circle(0, 0, radius / 20f);
        parent.fill(eyeColor);
        //right eye
        parent.translate(0.3f * radius, -0.4f * radius);
        parent.circle(0, 0, radius / 5f);
        //left eye
        parent.translate(-2 * 0.3f * radius, 0);
        parent.circle(0, 0, radius / 5f);
    }
}
