package ca;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Cell {
	protected CellullarAutomata ca;
	protected int row, col;
	protected int state;
	protected Cell[] neighbours;
	protected int w,h;

	public Cell(CellullarAutomata ca, int row, int col) {
		this.ca = ca;
		this.row = row;
		this.col = col;
		state =0;
		neighbours = null; 
		// quando define a celula vizinha define apenas mais tarde por metodo
		w = ca.getCellWidth();
		h = ca.getCellHeight();
	}
	public void setNeighbours(Cell[] neighbours) {
		this.neighbours = neighbours;
	}
	public Cell[] getNeighbours() {
		return neighbours;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getState() {
		return state;
	}
	public PVector getCenter() {
		float x = (col+0.5f)*w;
		float y = (row+0.5f)*h;	
		return new PVector(x,y);
	}
	public void display(PApplet p) {
		//metodo para desenhar a celula
		// push matrix guarda a matriz da tranformação das coordenadas
		//push style guarda o estilo de desenho
		p.pushStyle();
		p.fill(ca.getStateColors()[state]);
		p.rect(col*w, row*h, w, h);
		//em vez de retangulos podemos usar elipsees pa desenhar
		//descomentas as linhas a baixo e comentas a linha 45
		//p.ellipseMode(PConstants.CORNER);
		//p.ellipse(col*w,row*h,w,h);
		p.popStyle();
		
	}
}
