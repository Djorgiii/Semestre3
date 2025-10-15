package JogoDaVida;
import processing.core.PApplet;
import processing.core.PVector;

public class Cell {
    protected CellularAutomata ca;
    protected int row, col;
    protected int state;
    protected Cell[] neighbours;
    protected int w,h;
    private int nextState;
    private int color;

    public Cell(CellularAutomata ca, int row, int col) {
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
        p.pushStyle();
        if (isAlive()) {
            p.fill(color);
        } else {
            p.fill(60, 40, 70); // cor padrão para morta
        }
        p.rect(col*w, row*h, w, h);
        p.popStyle();
    }
    // Conta vizinhos vivos (exclui a própria célula)
    public int countAliveNeighbours() {
        int count = 0;
        if (neighbours != null) {
            for (Cell c : neighbours) {
                if (c != this && c.getState() == 1) count++;
            }
        }
        return count;
    }

    // Calcula próximo estado segundo regra 23/3
    public void computeNextState() {
        int aliveNeighbours = countAliveNeighbours();
        if (state == 1) {
            nextState = (aliveNeighbours == 2 || aliveNeighbours == 3) ? 1 : 0;
        } else {
            nextState = (aliveNeighbours == 3) ? 1 : 0;
        }
    }

    // Aplica o próximo estado
    public void applyNextState() {
        if (state == 0 && nextState == 1) {
            // Nasce: herda cor dominante dos vizinhos vivos
            color = ca.getDominantNeighbourColor(this);
        }
        state = nextState;
    }

    // Métodos utilitários para o App
    public boolean isAlive() {
        return state == 1;
    }
    public void setAlive(boolean alive) {
        state = alive ? 1 : 0;
    }
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
