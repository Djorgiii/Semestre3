package apps;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;
import apps.HelloFaceApp;

public class HelloFaceApp implements iProcessing{
	

	@Override
	public void setup(processing.core.PApplet parent) {
		
		
	
	}
	
	@Override
	public void draw(processing.core.PApplet parent, float dt) {
		
		
		
	}
	
	@Override
	public void keyPressed(processing.core.PApplet parent) {
		
		
		
	}
	
	@Override
	public void mousePressed(processing.core.PApplet parent) {
		
		PVector position= new PVector(parent.mouseX,parent.mouseY);
		Face f= new Face(position,parent.random(50,100),parent);
		f.display(parent);
		
	}
	
	
}
