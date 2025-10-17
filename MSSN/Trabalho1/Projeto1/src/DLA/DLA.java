package DLA;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

public class DLA implements setup.iProcessing {
	private int NUM_WALKERS = 2000;
	private int NUM_STEPS_PER_FRAME = 80;
	private  List<Walker> walkers;
	private int stoppedCount = 2;

	@Override
	public void setup(PApplet p) {
		walkers = new ArrayList<Walker>();
		for (int i=0; i<NUM_WALKERS; i++) {
			Walker w= new Walker(p);
			walkers.add(w);
		}
		Walker w1= new Walker(p, new PVector(p.width/2, p.height/2));
		walkers.add(w1);


	}
	@Override
	public void draw(PApplet p, float dt) {
		p.background(15,50,150);
		for(int i = 0; i<NUM_STEPS_PER_FRAME; i ++) {
			List<Walker> newWalkers = new ArrayList<>();
			for(Walker w: walkers) {
				if (w.getState() == Walker.State.WANDER) {
					w.wander(p);
					boolean stopped = w.updateState(p, walkers, stoppedCount);
					if (stopped) {
						stoppedCount++;
						newWalkers.add(new Walker(p));
					}
				}
			}
			walkers.addAll(newWalkers);
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
		Walker w = new Walker(p,new PVector(p.mouseX, p.mouseY));
		walkers.add(w);
		stoppedCount++;
	}
	@Override
	public void mouseMoved(PApplet p) {
		// TODO Auto-generated method stub
		
	}
	
	
}
