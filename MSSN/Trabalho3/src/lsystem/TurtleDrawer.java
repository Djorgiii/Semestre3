package lsystem;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.Stack;

public class TurtleDrawer {

    public static void draw(PApplet p, String s, float step, float angleDeg) {
        float angle = PApplet.radians(angleDeg);

        Stack<PVector> posStack = new Stack<>();
        Stack<Float> angStack = new Stack<>();

        PVector pos = new PVector(p.width/2f, p.height - 20);
        float heading = -PApplet.HALF_PI;

        p.stroke(255);
        p.strokeWeight(2);

        for (char c : s.toCharArray()) {
            switch (c) {

                case 'F':
                    float nx = pos.x + step * PApplet.cos(heading);
                    float ny = pos.y + step * PApplet.sin(heading);
                    p.line(pos.x, pos.y, nx, ny);
                    pos.set(nx, ny);
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
        }
    }
}
