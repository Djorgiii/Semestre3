package ca;
import processing.core.PApplet;

public class lifeCell extends Cell{
	
	private int  nAlives;
	public void LifeCell (CellullarAutomata ca, int row, int col) {
		super(ca, row, col);
		
	}
	public void flipState() {
		if (state==0) state=1;
		else state=0;
	}
	public void countAlives() {
		nAlives=0;
		for(Cell c: neighbours) nAlives+=c.getState();
		nAlives-=state; // não contar a propria celula	
	}
	public void applyRules() {
		if (state==0 && nAlives==3) state=1; // celula morta com 3 vizinhas vivas nasce
		if (state==1 && (nAlives<2 || nAlives>3)) state=0; // celula viva com menos de 2 ou mais de 3 vizinhas vivas morre
	}
	

}
