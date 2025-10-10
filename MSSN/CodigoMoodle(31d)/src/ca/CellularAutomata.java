package ca;

import processing.core.PApplet;

public class CellularAutomata
{
	protected int nrows, ncols;
	protected int w, h;
	protected Cell[][] cells;
	protected int radius;
	protected boolean moore;
	protected int numberOfStates;
	protected int[] colors;
	protected PApplet p;

	public CellularAutomata(PApplet p, int nrows, int ncols, 
			int radius, int numberOfStates)
	{
		this.p = p;
		this.nrows = nrows;
		this.ncols = ncols;
		this.radius = radius;
		this.numberOfStates = numberOfStates;		
		w = p.width/ncols;
		h = p.height/nrows;
		cells = new Cell[nrows][ncols];
		createGrid();
		colors = new int[numberOfStates];
		setRandomStateColors();
	}

	protected void createGrid()
	{
		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				cells[i][j] = new Cell(this, i, j);
			}
		}
		setNeighbors();
	}

	public Cell getCellGrid(int row, int col)
	{
		return cells[row][col];	
	}
	
	public void setNeighbors()
	{
		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				Cell[] neigh = new Cell[(int)Math.pow(2*radius+1,2)];
				int n = 0;
				for (int ii=-radius; ii<=radius; ii++) {
					for (int jj=-radius; jj<=radius; jj++) {
						int row = (i + ii + nrows) % nrows;
						int col = (j + jj + ncols) % ncols;
						neigh[n++] = cells[row][col];
					}
				}
				cells[i][j].setNeighbors(neigh);
			}
		}
	}


	public void reset()
	{
		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				cells[i][j].setState(0);
			}
		}
	}

	public void setRandomStateColors()
	{
		for(int i=0;i<numberOfStates;i++) {
			colors[i] = p.color(p.random(255),p.random(255),p.random(255));
		}
	}

	public void setStateColors(int[] colors)
	{
		this.colors = colors;
	}

	public int[] getStateColors()
	{
		return colors;
	}

	public int getCellWidth()
	{
		return w;
	}

	public int getCellHeight()
	{
		return h;
	}

	public int getNumberOfStates()
	{
		return numberOfStates;
	}

	public Cell getCell(int x, int y)
	{
		int row = y/h;
		int col = x/w;
		if (row >= nrows) row = nrows - 1;
		if (col >= ncols) col = ncols - 1;
	
		return cells[row][col];
	}

	public void setRandomStates() {
		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				cells[i][j].setState((int) p.random(numberOfStates));
			}
		}
	}

	/*public void setRandomStatesCustom(double[] pmf) {
		CustomRandomGenerator crg = new CustomRandomGenerator(pmf);
		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				cells[i][j].setState(crg.getRandomClass());
			}
		}
	}*/

	public void display()
	{
		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				cells[i][j].display(p);
			}
		}
	}
}