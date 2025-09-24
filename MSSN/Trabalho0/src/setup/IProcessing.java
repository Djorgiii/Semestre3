package setup;

import processing.core.PApplet;


public interface iProcessing {
	
	public void setup(PApplet parent);
	
	public void draw(PApplet parent, float dt);// dt = frame rate(delta time)
	
	public void keyPressed(PApplet parent);
	
	public void mousePressed(PApplet parent);

}
