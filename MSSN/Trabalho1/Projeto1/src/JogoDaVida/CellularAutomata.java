package JogoDaVida;
import processing.core.PApplet;


public class CellularAutomata {
    protected int nrows, ncols;
    protected int w, h;
    protected Cell[][] cells;
    protected int radius; // raio da vizinhança
    protected boolean moore; // considera os vizinhos nas diagonais da celula
    //se for true considera os vizinhos nas diagonais
    protected int numberOfStates;
    protected int[] colors;
    protected PApplet p;

    public CellularAutomata(PApplet p, int nrows, int ncols, int radius, boolean moore, int numberOfStates) {
        this.p = p;
        this.nrows = nrows;
        this.ncols = ncols;
        this.radius = radius;
        this.moore = moore;
        this.numberOfStates = numberOfStates;
        w = p.width/ncols;
        h = p.height/nrows;
        cells = new Cell[nrows][ncols];
        createGrid();
        colors = new int[numberOfStates];
        setRandomStateColors();
    }
    protected void createGrid() {
        for(int i=0; i<nrows; i++) {
            for(int j=0; j<ncols; j++) {
                cells[i][j] = new Cell(this, i, j);
            }
        }
        if (moore) setNeighbours(); // todos os vizinhos
        //else setNeighbours4(); // apenas os vizinhos em cruz (von neumann)
    }
    public Cell getCellGrid(int row, int col) {
        return cells[row][col];
    }
    protected void setNeighbours() { //serve para criar um array com os vizinhos
        for(int i=0; i<nrows; i++) {
            for(int j=0; j<ncols; j++) {
                Cell[] neigh = new Cell[(int)Math.pow((2*radius+1),2)-1];// considera a propria celula como vizinha
                int n = 0;
                for(int ii=-radius; ii<=radius; ii++) {
                    for(int jj=-radius; jj<=radius; jj++) {
                        //if (ii==0 &&jj==0) continue; // não considera a celula central
                        int row = (i + ii + nrows) % nrows; // wrap around
                        int col = (j + jj + ncols) % ncols; // wrap around
                        neigh[n++] = cells[row][col];
                    }
                }
                cells[i][j].setNeighbours(neigh);
            }
        }
    }
    /* acabar de passar
     * este codigo todo ta nos vidios do prof do moodle
    protected void setNeighbours4() {
        int numberOfNeighbours = 2*(radius*radius+radius)+1;//serve para criar um array com os vizinhos
        for(int i=0; i<nrows; i++) {
            for(int j=0; j<ncols; j++) {
                Cell[] neigh = new Cell[(int)Math.pow(2*radius+1),b:2];// considera a propria celula como vizinha
                int n = 0;
                for(int ii=-radius; ii<=radius; ii++) {
                    for(int jj=-radius; jj<=radius; jj++) {
                        //if (ii==0 &&jj==0) continue; // não considera a celula central
                        int row = (i + ii + nrows) % nrows; // wrap around
                        int col = (j + jj + ncols) % ncols; // wrap around
                        neigh[n++] = cells[row][col];
                    }
                }
                cells[i][j].setNeighbours(neigh);
            }
        }
    }
    */
    public void reset() {
        for(int i=0; i<nrows; i++) {
            for(int j=0; j<ncols; j++) {
                cells[i][j].setState(0);
            }
        }
    }
    public void setRandomStateColors() {
        for(int i=0; i<numberOfStates; i++) {
            colors[i] = p.color(p.random(255), p.random(255), p.random(255));
        }
    }
    public void setStateColors(int[] colors) {
        this.colors = colors;
    }
    public int[] getStateColors() {
        return colors;
    }
    public int getCellWidth() {
        return w;
    }
    public int getCellHeight() {
        return h;
    }
    public int getNumberOfStates() {
        return numberOfStates;
    }
    public Cell getCell(int x, int y) {// x e y coordenadas em pixels
        //objetivo e retornar qual e a celula em que clicares
        int row = y/h;
        int col = x/w;
        //se o numero de linhas ou colunas for maior que o numero de linhas ou colunas
        // entao devolve a celula mais proxima
        // ex se clicares fora da janela
        if(row>= nrows) row = nrows-1;
        if(col>= ncols) col = ncols-1;

        return cells[row][col];
    }

    public void setRandomStates() {
        for(int i=0; i<nrows; i++) {
            for(int j=0; j<ncols; j++) {
                cells[i][j].setState((int)p.random(numberOfStates));
            }
        }
    }
    public void display() {
        for(int i=0; i<nrows; i++) {
            for(int j=0; j<ncols; j++) {
                cells[i][j].display(p);
            }
        }
    }
    // Atualiza todas as células segundo a regra do Jogo da Vida
    public void update() {
        // 1º: calcula próximo estado para todas
        for (int r = 0; r < nrows; r++)
            for (int c = 0; c < ncols; c++)
                cells[r][c].computeNextState();
        // 2º: aplica próximo estado
        for (int r = 0; r < nrows; r++)
            for (int c = 0; c < ncols; c++)
                cells[r][c].applyNextState();
    }

    // Métodos utilitários para o App
    public boolean isAlive(int r, int c) {
        return cells[r][c].isAlive();
    }
    public void setAlive(int r, int c, boolean alive) {
        cells[r][c].setAlive(alive);
    }
    public int getRows() { return nrows; }
    public int getCols() { return ncols; }

    // Devolve cor tipo arco-íris conforme número de vizinhos vivos
    public int getAliveNeighboursColor(int n) {
        // HSV: matiz varia de 0 a 255 conforme n (0 a 8 vizinhos)
        float hue = (n / 8.0f) * 255.0f; // 0 a 255
        float sat = 200;
        float bri = 255;
        p.colorMode(PApplet.HSB, 255);
        int color = p.color(hue, sat, bri);
        p.colorMode(PApplet.RGB, 255); // volta ao modo padrão
        return color;
    }

    // Devolve uma cor RGB aleatória
    public int getRandomColor() {
        return p.color((int)p.random(255), (int)p.random(255), (int)p.random(255));
    }
    // Returns the most prevalent color among live neighbors of a cell
    public int getDominantNeighbourColor(Cell cell) {
        java.util.HashMap<Integer, Integer> colorCount = new java.util.HashMap<>();
        int maxCount = 0;
        int chosenColor = getRandomColor(); // fallback if no live neighbors
        if (cell.neighbours != null) {
            for (Cell neigh : cell.neighbours) {
                if (neigh != cell && neigh.isAlive()) {
                    int c = neigh.getColor();
                    int count = colorCount.getOrDefault(c, 0) + 1;
                    colorCount.put(c, count);
                    if (count > maxCount) {
                        maxCount = count;
                        chosenColor = c;
                    }
                }
            }
        }
        // In case of tie, pick any of the tied colors
        if (maxCount > 0) {
            java.util.ArrayList<Integer> tiedColors = new java.util.ArrayList<>();
            for (java.util.Map.Entry<Integer, Integer> entry : colorCount.entrySet()) {
                if (entry.getValue() == maxCount) tiedColors.add(entry.getKey());
            }
            if (!tiedColors.isEmpty()) {
                chosenColor = tiedColors.get((int)(p.random(tiedColors.size())));
            }
        }
        return chosenColor;
    }
}
