import processing.core.PApplet;

public class HelloProcessing extends PApplet {
    public static void main(String[] args) {
        PApplet.main(HelloProcessing.class.getName());

    }

    @Override
    public void settings() {
        size(800, 600);
    }

    @Override
    public void setup() {
        background(255,0,0);
    }

    @Override
    public void draw() {
        //background(0);
        circle(mouseX,mouseY,50);
    }
}