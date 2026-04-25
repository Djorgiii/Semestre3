package server;


public class Jogo {
    protected boolean[][] horiz = new boolean[4][3];
    protected boolean[][] vert = new boolean[3][4];
    
    protected char[][] caixas = new char[3][3];
    
    public Jogo() {
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                caixas[i][j] = ' ';
            }
        }
    }

    public boolean joga(int[] coords, char simbolo) {
        if(coords.length != 4) return false;
        
        int x1 = coords[0] - 1;
        int y1 = coords[1] - 1;
        int x2 = coords[2] - 1;
        int y2 = coords[3] - 1;

        boolean fechouCaixa = false;

        if (y1 == y2 && Math.abs(x1 - x2) == 1) {
            int startX = Math.min(x1, x2);
            if (horiz[y1][startX]) return false;
            horiz[y1][startX] = true;
            fechouCaixa = verificarCaixas(simbolo);
        }
        else if (x1 == x2 && Math.abs(y1 - y2) == 1) {
            int startY = Math.min(y1, y2);
            if (vert[startY][x1]) return false;
            vert[startY][x1] = true;
            fechouCaixa = verificarCaixas(simbolo);
        } else {
            return false;
        }

        return fechouCaixa;
    }

    private boolean verificarCaixas(char simbolo) {
        boolean marcouNova = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (caixas[i][j] == ' ') {
                    if (horiz[i][j] && horiz[i+1][j] && vert[i][j] && vert[i][j+1]) {
                        caixas[i][j] = simbolo;
                        marcouNova = true;
                    }
                }
            }
        }
        return marcouNova;
    }

    public boolean jogoTerminou() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (caixas[i][j] == ' ') return false;
            }
        }
        return true;
    }
}