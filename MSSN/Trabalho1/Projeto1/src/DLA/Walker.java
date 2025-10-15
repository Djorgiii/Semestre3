package DLA;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.List;

public class Walker {
	public enum State {
		WANDER, STICK, STOPPED
	}
	private PVector pos;
	private int color;
	private static int radius = 4; // atributo da classe entao fica static
	private State state;
	private int stoppedIndex = -1; // For coloring

	public Walker(PApplet p) {
		this.pos = new PVector(p.random(p.width), p.random(p.height));
		setState(p, State.WANDER, -1);
	}
	public Walker(PApplet p, PVector pos) {
		this.pos = pos;
		setState(p, State.STOPPED, 0);
	}
	public boolean updateState(PApplet p, List<Walker> walkers, int stoppedCount) {
		for(Walker w: walkers) {
			if (w.state == State.STOPPED) {
				// calcular a distancia entre a particula e a q ta parada
				// se a distancia for menor q o raio 
				float dist = PVector.dist(this.pos, w.pos);
				if(dist < 2*radius) {
					state = State.STOPPED;
					setState(p, state, stoppedCount);
					return true;
				}
			}
		}
		return false;
	}
	public void setState(PApplet p, State state, int stoppedIndex) {
		this.state = state;
		this.stoppedIndex = stoppedIndex;
		if(state == State.STOPPED) {
			// Gradient from blue to red based on stoppedIndex
			this.color = p.color(lerp(15, 255, stoppedIndex/100.0f), 50, lerp(150, 0, stoppedIndex/100.0f));
		} else {
			// Moving particles are green
			this.color = p.color(0, 255, 0);
		}
	}
	private float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}
	public void wander(PApplet p) {
		if(this.state == State.STOPPED) return;
		PVector step = PVector.random2D();
		pos.add(step.mult(2));
	}
	
	public void display(PApplet p) {
		p.noStroke();
		p.fill(this.color);
		p.circle(this.pos.x, this.pos.y, Walker.radius*2);
		
	}
	public State getState() {
	    return state;
	}
}
