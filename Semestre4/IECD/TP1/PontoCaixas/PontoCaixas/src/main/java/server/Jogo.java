package server;

/**
 * Lógica base para o jogo Pontos e Caixas (Dots and Boxes).
 * Grelha 3x3 caixas (o que implica 4x4 pontos).
 */
public class Jogo {
    // Matrizes para guardar se a linha foi desenhada
    protected boolean[][] horiz = new boolean[4][3]; // 4 linhas, 3 arestas horizontais por linha
    protected boolean[][] vert = new boolean[3][4];  // 3 linhas de arestas, 4 arestas verticais por linha
    
    // Matriz para guardar o dono de cada caixa ('A' ou 'B')
    protected char[][] caixas = new char[3][3];
    
    public Jogo() {
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                caixas[i][j] = ' '; // Caixas vazias no início
            }
        }
    }

    /**
     * Tenta desenhar uma linha entre dois pontos.
     * Recebe um array com [x1, y1, x2, y2]
     * @return true se a jogada for válida e fechar uma caixa (Bónus), false caso contrário.
     */
    public boolean joga(int[] coords, char simbolo) {
        if(coords.length != 4) return false;
        
        // Converte de 1-indexed (utilizador) para 0-indexed (matrizes)
        int x1 = coords[0] - 1;
        int y1 = coords[1] - 1;
        int x2 = coords[2] - 1;
        int y2 = coords[3] - 1;

        boolean fechouCaixa = false;

        // É uma linha Horizontal? (mesmo Y, X adjacente)
        if (y1 == y2 && Math.abs(x1 - x2) == 1) {
            int startX = Math.min(x1, x2);
            if (horiz[y1][startX]) return false; // Linha já existe
            horiz[y1][startX] = true;
            fechouCaixa = verificarCaixas(simbolo);
        }
        // É uma linha Vertical? (mesmo X, Y adjacente)
        else if (x1 == x2 && Math.abs(y1 - y2) == 1) {
            int startY = Math.min(y1, y2);
            if (vert[startY][x1]) return false; // Linha já existe
            vert[startY][x1] = true;
            fechouCaixa = verificarCaixas(simbolo);
        } else {
            return false; // Diagonal ou muito longe
        }

        return fechouCaixa; // Retorna true se fechou (dá direito a bónus)
    }

    /**
     * Verifica o tabuleiro todo para ver se alguma caixa nova foi fechada
     */
    private boolean verificarCaixas(char simbolo) {
        boolean marcouNova = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (caixas[i][j] == ' ') { // Se a caixa está vazia
                    // Verifica os 4 lados (topo, base, esq, dir)
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