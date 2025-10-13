package setup;

import ca.TestCA;
import dla.DLA;
import processing.core.PApplet;

public class ProcessingSetup extends PApplet {
    private static IProcessingApp app;
    private int lastUpdateTime;

    public static void main(String[] args)
    {
        app = new DLA();
        PApplet.main(ProcessingSetup.class.getName());
    }
    
    @Override
    public void settings()
    {
        size(800, 600);
    }

    @Override
    public void setup()
    {
        app.setup(this);
        lastUpdateTime = millis();
    }

    @Override
    public void draw()
    {
        int now = millis();
        float dt = (now - lastUpdateTime) / 1000f;
        app.draw(this, dt);
        lastUpdateTime = now;
    }

    @Override
    public void keyPressed()
    {
        app.keyPressed(this);
    }

    @Override
    public void mousePressed() {
        app.mousePressed(this);
    }
}
