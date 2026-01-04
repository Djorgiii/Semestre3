package simulacao;

import processing.core.PApplet;
import processing.core.PVector;

public class Floresta {
    public int cols, rows;
    public int[][] grid;
    public float cellSize;

    public Floresta(PApplet p, int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.grid = new int[cols][rows];
        this.cellSize = (float) p.width / cols;
        gerarTerreno(p);
    }

    private void gerarTerreno(PApplet p) {
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                float val = p.noise(i * 0.03f, j * 0.03f);
                if (val > 0.45f) grid[i][j] = 1;
                else if (val > 0.30f) grid[i][j] = 4;
                else grid[i][j] = 0;
            }
        }
    }

    public void atualizar(PApplet p) {
        int[][] nextGrid = new int[cols][rows];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                nextGrid[i][j] = grid[i][j];

                if (grid[i][j] == 4 && p.random(1) < 0.015f) {
                    nextGrid[i][j] = 1;
                }

                if (grid[i][j] == 3 && p.random(1) < 0.02f) {
                    nextGrid[i][j] = 4;
                }

                if (grid[i][j] == 1 && temVizinhoArder(i, j)) {
                    if (p.random(1) < 0.10f) nextGrid[i][j] = 2;
                }
                else if (grid[i][j] == 1 && p.random(1) < 0.0001f) {
					nextGrid[i][j] = 2;
				}

                if (grid[i][j] == 2 && p.random(1) < 0.02f) {
                    nextGrid[i][j] = 3; 
                }
            }
        }
        grid = nextGrid;
    }

    private boolean temVizinhoArder(int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nx = x + i;
                int ny = y + j;
                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                    if (grid[nx][ny] == 2) return true;
                }
            }
        }
        return false;
    }
    
    public void forçarFogoAleatorio(PApplet p) {
        int rx = (int) p.random(cols); 
        int ry = (int) p.random(rows);
        
        if (grid[rx][ry] == 1) {
            grid[rx][ry] = 2;
        }
    }

    public void display(PApplet p) {
        p.noStroke();
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                float x = i * cellSize;
                float y = j * cellSize;
                float n = p.noise(i * 0.2f, j * 0.2f); 

                if (grid[i][j] == 1) {
                    p.fill(34 + (n * 15), 100 + (n * 40), 34); 
                } else if (grid[i][j] == 2) {
                    p.fill(255, 60 + p.random(150), 0); 
                } else if (grid[i][j] == 3) {
                    p.fill(40 + (n * 15)); 
                } else if (grid[i][j] == 4) {
                    p.fill(139, 115, 85);
                } else {
                    p.fill(20, 50 + (n * 10), 120 + (n * 20)); 
                }
                p.rect(x, y, cellSize, cellSize);
            }
        }
    }
}