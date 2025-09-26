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
        fill(255, 0, 0);
    }

    @Override
    public void draw() 
    {
        //background(0);

        fill(0, 255, 0, 64);
        rect(100, 100, 200, 150);

        fill(255, 0, 0, 64);
        circle(mouseX, mouseY, 50);
    }
}