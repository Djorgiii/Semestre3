package server;

/**
 * Jogo — lógica base do jogo Pontos e Caixas (3x3 caixas, 4x4 pontos).
 *
 * Mantém o estado do tabuleiro em duas matrizes booleanas:
 *   horiz[4][3] — linhas horizontais (4 linhas de pontos, 3 segmentos por linha)
 *   vert[3][4]  — linhas verticais   (3 linhas de pontos, 4 segmentos por linha)
 *
 * E uma matriz de caracteres para os donos das caixas:
 *   caixas[3][3] — ' ' se livre, 'X' ou 'O' se fechada
 *
 * Esta classe é estendida por JogoXML que adiciona serialização XML e
 * gestão de estados do protocolo (ND, BO, IV, VX, VO, EM).
 */


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

    /**
     * Regista uma jogada no tabuleiro.
     * Valida se a linha já existe e se as coordenadas formam um segmento válido.
     *
     * @param coords  array de 4 inteiros [x1, y1, x2, y2] (base 1)
     * @param simbolo símbolo do jogador ('X' ou 'O')
     * @return true se a jogada fechou pelo menos uma caixa (bónus), false caso contrário
     */
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

    /**
     * Verifica se alguma caixa ficou fechada após a última linha desenhada.
     * Uma caixa fecha quando tem os 4 lados preenchidos (2 horizontais + 2 verticais).
     *
     * @param simbolo símbolo do jogador que fecha a(s) caixa(s)
     * @return true se pelo menos uma nova caixa foi fechada
     */
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

    /**
     * Verifica se o jogo terminou, ou seja, se todas as 9 caixas estão fechadas.
     *
     * @return true se não houver caixas livres
     */
    public boolean jogoTerminou() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (caixas[i][j] == ' ') return false;
            }
        }
        return true;
    }
}