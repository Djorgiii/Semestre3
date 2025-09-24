package setup;

import apps.HelloFaceApp;
import processing.core.PApplet;

public class Processing extends PApplet {
    private static IProcessing App;
    private int lastUpdateTime;

    public static void main(String[] args) {
        App = new HelloFaceApp();
        PApplet.main(Processing.class.getName());
    }

    @Override
    public void settings() {
        size(800, 600);
    }

    @Override
    public void setup() {
        App.setup(this);
        lastUpdateTime = millis();

    }

    @Override
    public void draw() {
        int now = millis();
        float dt = (now - lastUpdateTime) / 1000f;
        App.draw(this,dt);
        lastUpdateTime = now;
    }
    @Override
    public void keyPressed(){
        App.keyPressed(this);
    }
    @Override
    public void mousePressed(){
        App.mousePressed(this);
    }
}
