package lsystem;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.Stack;

public class TurtleDrawer {

    // === DESENHO NORMAL (Koch, Planta, etc.) ===
    public static void draw(PApplet p, String s, float step, float angleDeg) {

        float angle = PApplet.radians(angleDeg);

        Stack<PVector> posStack = new Stack<>();
        Stack<Float> angStack = new Stack<>();

        // posição inicial (fundo do ecrã)
        PVector pos = new PVector(p.width/2f, p.height - 20);
        float heading = -PApplet.HALF_PI;

        p.stroke(255);
        p.strokeWeight(2);

        for (char c : s.toCharArray()) {

            switch (c) {

                case 'F':
                case 'G':
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



    // === DESENHO ESPECIAL PARA ÁRVORE DE FRUTOS ===
    public static void drawFruitTree(PApplet p, String s, float step, float angleDeg) {

        float angle = PApplet.radians(angleDeg);

        Stack<PVector> posStack = new Stack<>();
        Stack<Float> angStack = new Stack<>();

        // começa no fundo, voltado para cima (como uma árvore real)
        PVector pos = new PVector(p.width/2f, p.height - 20);
        float heading = -PApplet.HALF_PI;

        p.strokeWeight(2);

        for (char c : s.toCharArray()) {

            switch (c) {

                case 'F':   // ramo terminal
                    p.stroke(0, 180, 0);
                    float fx = pos.x + step * PApplet.cos(heading);
                    float fy = pos.y + step * PApplet.sin(heading);
                    p.line(pos.x, pos.y, fx, fy);
                    pos.set(fx, fy);

                    // 15% probabilidade de desenhar fruto
                    if (Math.random() < 0.15) {
                        p.noStroke();
                        p.fill(255, 50, 50);
                        p.circle(pos.x, pos.y, 7);
                        p.stroke(0, 180, 0);
                    }
                    break;


                case 'G':   // tronco grosso
                    p.stroke(150, 70, 20);
                    float gx = pos.x + step * PApplet.cos(heading);
                    float gy = pos.y + step * PApplet.sin(heading);
                    p.line(pos.x, pos.y, gx, gy);
                    pos.set(gx, gy);
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
