package lsystem;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.Stack;

public class AnimatedTurtle {

    private String sequence;
    private int index = 0;
    private float step;
    private float angle;

    private PVector pos;
    private float heading;

    private Stack<PVector> posStack = new Stack<>();
    private Stack<Float> angStack = new Stack<>();

    public AnimatedTurtle(String sequence, float step, float angleDeg, PApplet p) {
        this.sequence = sequence;
        this.step = step;
        this.angle = PApplet.radians(angleDeg);

        pos = new PVector(p.width/2f, p.height - 20);
        heading = -PApplet.HALF_PI;
    }
    
    public AnimatedTurtle(String sequence, float step, float angleDeg, 
            PApplet p, PVector startPos, float startHeading) {

		this.sequence = sequence;
		this.step = step;
		this.angle = PApplet.radians(angleDeg);
		
		this.pos = startPos.copy();
		this.heading = startHeading;
    }


    public boolean drawNext(PApplet p) {
        if (index >= sequence.length()) return false;
        
        p.stroke(255);
        p.strokeWeight(2);

        char c = sequence.charAt(index);

        switch (c) {

            case 'F':
            case 'G':
                float nx = pos.x + step * PApplet.cos(heading);
                float ny = pos.y + step * PApplet.sin(heading);
                p.line(pos.x, pos.y, nx, ny);
                pos.set(nx, ny);
                break;
                

            case 'X':
                break;

            case '+':
                heading += angle;
                break;

            case '-':
                heading -= angle;
                break;

            case '[':
                posStack.push(pos.copy());
                angStack.push(heading);
                break;

            case ']':
                pos = posStack.pop();
                heading = angStack.pop();
                break;
        }

        index++;
        return true;
    }
}
