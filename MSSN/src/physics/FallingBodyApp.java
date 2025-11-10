package physics;

import tools.SubPlot;
import setup.iProcessing;
import processing.core.PVector;
import processing.core.PApplet;

public class FallingBodyApp implements iProcessing{
	
	public static final float dimY = 15;
	public static final float dimX = 20;
	private static final float g = -9.8f;
	private static final float mass = 80;
	private static final float radius = 0.5f;
	private final double window[] = {0,dimX,0,dimY};
	private float viewport[] = {0f,0f,1f,1f};
	private SubPlot plt;
	private Body ball;
	private Water water;
	private Air air;
	private float speedUp = 0.5f;
	private float timer;
	
	
	@Override
	public void setup(PApplet p) {
		plt = new SubPlot(window, viewport, p.width, p.height);
		ball = new Body(new PVector(0,12), new PVector(8,6),mass,radius,p.color(255,255,0));
		water = new Water(4,p.color(0,255,255));
		air = new Air(speedUp);
		timer = 0.0f;
		
	}

	@Override
	public void draw(PApplet p, float dt) {
		PVector f = new PVector(0,mass*g);
		ball.applyForce(f);
		if(water.isInside(ball)) {
			f = water.drag(ball);
		}else {
			f = air.drag(ball);
		}
		ball.applyForce(f);
		ball.move(speedUp*dt);
		
		water.display(p, plt);
		ball.display(p, plt);
		
		timer += (speedUp*dt);
		if(ball.pos.y < 0) {
			p.noLoop();
			System.out.println(timer);
		}
	}

	@Override
	public void keyPressed(PApplet parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(PApplet parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(PApplet parent) {
		// TODO Auto-generated method stub
		
	}
	
}
