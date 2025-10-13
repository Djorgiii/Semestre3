package DLA;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

public class DLA implements setup.iProcessing {
	private int NUM_WALKERS = 200;
	private int NUM_STEPS_PER_FRAME = 100;
	private  List<Walker> walkers;	
	
	@Override
	public void setup(PApplet p) {
		walkers = new ArrayList<Walker>();
		for (int i=0; i<NUM_WALKERS; i++) {
			Walker w= new Walker(p);
			walkers.add(w);
		}
		Walker w1= new Walker(p, new PVector(30,100));
		Walker w2= new Walker(p, new PVector(300,500));
		walkers.add(w1);
		walkers.add(w2);
		

	}
	@Override
	public void draw(PApplet p, float dt) {
		p.background(15,50,150);
		for(int i = 0; i<NUM_STEPS_PER_FRAME; i ++) {
			for(Walker w: walkers) {
				w.wander(p);
				w.updateState(p, walkers);
			}
		}
		
		for(Walker w: walkers) {
			w.display(p);
		}
		
	}
	@Override
	public void keyPressed(PApplet p) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(PApplet p) {
		// TODO Auto-generated method stub
		Walker w = new Walker(p,new PVector(p.mouseX, p.mouseY));
		walkers.add(w);
	}
	@Override
	public void mouseMoved(PApplet p) {
		// TODO Auto-generated method stub
		
	}
	
	
}
