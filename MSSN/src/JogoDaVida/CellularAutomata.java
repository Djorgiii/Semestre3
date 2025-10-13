package JogoDaVida;

public class CellularAutomata {
    private Cell[][] grid;
    private int rows, cols;

    public CellularAutomata(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new Cell[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                grid[i][j] = new Cell(false);
    }

    public void setAlive(int x, int y, boolean alive) {
        grid[x][y].setAlive(alive);
    }

    public boolean isAlive(int x, int y) {
        return grid[x][y].isAlive();
    }

    private int countAliveNeighbors(int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && nx < rows && ny >= 0 && ny < cols && grid[nx][ny].isAlive())
                    count++;
            }
        }
        return count;
    }

    public void update() {
        Cell[][] next = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int aliveNeighbors = countAliveNeighbors(i, j);
                boolean nextAlive = false;
                if (grid[i][j].isAlive()) {
                    nextAlive = (aliveNeighbors == 2 || aliveNeighbors == 3);
                } else {
                    nextAlive = (aliveNeighbors == 3);
                }
                next[i][j] = new Cell(nextAlive);
            }
        }
        grid = next;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
}