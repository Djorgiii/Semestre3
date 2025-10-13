package DLA;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;

public class Walker {
	public enum State {
		WANDER, STICK, STOPPED
	}
	private PVector pos;
	private int color;
	private static int radius = 4; // atributo da classe entao fica static
	private State state;
	
	public Walker(PApplet p) {
		this.pos = new PVector(p.random(p.width), p.random(p.height));
		setState(p, State.WANDER);
		}
	public Walker(PApplet p, PVector pos) {
		this.pos = pos;
		setState(p, State.STOPPED);
	}
	public void updateState(PApplet p, List<Walker> walkers) {
		
		for(Walker w: walkers) {
			if (w.state == State.STOPPED) {
				// calcular a distancia entre a particula e a q ta parada
				// se a distancia for menor q o raio 
				float dist = PVector.dist(this.pos, w.pos);
				if(dist < 2*radius) {
					state= State.STOPPED;
					setState(p,state);
					break;
				}
			}
		}
		if(state == State.STOPPED) {return;}
		
	}
	public void setState(PApplet p,State state) {
		this.state= state;
		if(state== State.STOPPED) {
			this.color = p.color(0);
		}
		else {
			this.color= p.color(255);
		}
	}
	public void wander(PApplet p) {
		if(this.state == State.STOPPED) return;
		PVector step= PVector.random2D();
		pos.add(step.mult(2));
	}
	
	public void display(PApplet p) {
		p.noStroke();
		p.fill(this.color);
		p.circle(this.pos.x, this.pos.y, Walker.radius*2);
		
	}
}
