package ca;

import processing.core.PApplet;
import setup.iProcessing;

public class testCA implements iProcessing {
	CellullarAutomata ca;
	private int nrows=5;
	private int ncols = 8;
	private int nstates= 4;
	
	public void setup(PApplet parent) {
		ca = new CellullarAutomata(parent, nrows, ncols, 1, true, nstates);
		ca.setRandomStates();
		ca.display();
	}
	public void draw(PApplet parent, float dt) {
		
	}
	public void keyPressed(PApplet parent) {
		
	}
	public void mousePressed(PApplet parent) {
		Cell c= ca.getCell(parent.mouseX, parent.mouseY);
		for(Cell cc: c.getNeighbours()) {
			cc.setState(1);
		}
		
	}
	public void mouseMoved(PApplet parent) {
		
	}
	

}
