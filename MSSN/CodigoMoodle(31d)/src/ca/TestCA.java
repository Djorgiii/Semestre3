package ca;
import setup.IProcessingApp;
import processing.core.PApplet;

public class TestCA implements IProcessingApp {

	private CellularAutomata ca;
	private int nrows = 50;
	private int ncols = 70;
	private int radius = 1;
	private int numberOfStates = 3;

    private int state = 1;
	@Override
	public void setup(PApplet p) 
    {
		ca = new CellularAutomata(p, nrows, ncols, 
                radius, numberOfStates);
        ca.setRandomStates();
                
        //ca.setStateColors(colors);

		//ca.getCellGrid(4, 2).setState(1);
		//ca.getCellGrid(2, 4).setState(2);
	}

	@Override
	public void draw(PApplet p, float dt) 
	{
		ca.display();
	}

	@Override
	public void keyPressed(PApplet p) 
	{
		state = p.key - '0';
		if (state < 0) state = 0;
		if (state >= numberOfStates) 
			state = numberOfStates - 1;
		System.out.println("state: " + state);
	}

	@Override
	public void mousePressed(PApplet p) 
	{
		Cell c = ca.getCell(p.mouseX, p.mouseY);
		for(Cell cc : c.getNeighbors())
			cc.setState(state);
	}
}
