package setup;
import processing.core.PApplet;
import simulacao.ForestFireApp;

public class processingMain extends PApplet {
	
	private static iProcessing app;
	private int lastUpdateTime;
	
	public static void main(String[] args) {
		
		app= new ForestFireApp();
		PApplet.main(processingMain.class.getName());
		
	}
	
	@Override
	public void settings() {
		
		size(1024,800);
	
	}
	
	@Override
	public void setup() {
		
		app.setup(this);
		lastUpdateTime = millis();
		
	}
	
	@Override
	public void draw() {
		
		int now = millis();
		float dt = (now - lastUpdateTime) / 1000.0f;
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
	
	@Override
    public void keyReleased() {
        app.keyReleased(this);
    }
	
}
