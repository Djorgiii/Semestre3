package setup;
import DLA.DLA;
import processing.core.PApplet;

public class processingMain extends PApplet {
	
	private static iProcessing app;
	private int lastUpdateTime;
	
	public static void main(String[] args) {
		
		app= new DLA();
		PApplet.main(processingMain.class.getName());
		
	}
	
	@Override
	public void settings() {
		
		size(800,600);
	
	}
	
	@Override
	public void setup() {
		
		app.setup(this);
		lastUpdateTime = millis();
		
	}
	
	@Override
	public void draw() {
		
		int now = millis();
		float dt = (now - lastUpdateTime) / 1000.0f; // Convert to seconds e transforma em float
		lastUpdateTime = now;
		
		app.draw(this, dt);
		
	}
	
	@Override
	public void keyPressed() {
		app.keyPressed(this);
	}
	
	@Override
	public void mousePressed() {
		app.mousePressed(this);
	}
	@Override
	public void mouseMoved() {
		app.mouseMoved(this);
	}
	
}
