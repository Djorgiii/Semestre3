package server;

/**
 * Classe que implementa o adaptador do jogo para XML.
 * Adaptado para o jogo Pontos e Caixas (Dots and Boxes).
 *
 * @author Engº Porfírio Filipe
 */
public class JogoXML extends Jogo {

    /**
     * Estado do jogo após a última jogada.
     * Possíveis valores:
     * - "ND": Nada a registar, turno passa para o outro jogador.
     * - "BO": Bónus! Jogador fechou uma caixa e joga novamente.
     * - "IV": Jogada inválida (linha já existe ou coordenadas erradas).
     * - "VX": Vitória do X.
     * - "VO": Vitória do O.
     * - "EM": Empate.
     */
    private String estado = "ND";

    /**
     * Permite ao Skeleton forçar um estado de erro (ex: sintaxe inválida enviada pelo cliente)
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    
    /**
     * Devolve o estado atual do jogo.
     */
    public String getEstado() {
        return this.estado;
    }
    

    /**
     * Converte as matrizes do jogo para XML.
     *
     * @return String com XML que representa as linhas e as caixas.
     */
    public String tabuleiroToXML() {
        // Usamos StringBuilder por ser mais eficiente para concatenar muitas strings
        StringBuilder tab = new StringBuilder("<tabuleiro estado='" + estado + "'>");
        
        // 1. Exporta as linhas horizontais desenhadas
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                if (horiz[i][j]) {
                    // As coordenadas do cliente são 1-indexed (começam em 1)
                    tab.append(String.format("<linha x1='%d' y1='%d' x2='%d' y2='%d'/>", j+1, i+1, j+2, i+1));
                }
            }
        }
        
        // 2. Exporta as linhas verticais desenhadas
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (vert[i][j]) {
                    tab.append(String.format("<linha x1='%d' y1='%d' x2='%d' y2='%d'/>", j+1, i+1, j+1, i+2));
                }
            }
        }
        
        // 3. Exporta as caixas que já têm dono
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (caixas[i][j] != ' ') {
                    tab.append(String.format("<caixa dono='%c' x='%d' y='%d'/>", caixas[i][j], j+1, i+1));
                }
            }
        }
        
        tab.append("</tabuleiro>");
        return tab.toString();
    }

    /**
     * Valida as coordenadas, concretiza a jogada e atualiza o estado do jogo.
     *
     * @param coords  Array com [x1, y1, x2, y2].
     * @param simbolo Símbolo do jogador ('X' ou 'O').
     * @return true se a jogada for válida (independentemente de dar bónus ou não).
     */
    public boolean joga(int[] coords, char simbolo) {
        estado = "ND"; // Por defeito, nada a registar, passa o turno

        // 1. Validação prévia para saber se devolvemos estado "IV" (Inválido)
        int x1 = coords[0] - 1;
        int y1 = coords[1] - 1;
        int x2 = coords[2] - 1;
        int y2 = coords[3] - 1;

        boolean isValida = false;
        
        // Verifica se está dentro dos limites e não está ocupada
        try {
            if (y1 == y2 && Math.abs(x1 - x2) == 1) { // Horizontal
                int startX = Math.min(x1, x2);
                if (!horiz[y1][startX]) isValida = true;
            } 
            else if (x1 == x2 && Math.abs(y1 - y2) == 1) { // Vertical
                int startY = Math.min(y1, y2);
                if (!vert[startY][x1]) isValida = true;
            }
        } catch (IndexOutOfBoundsException e) {
            // Jogou fora do tabuleiro
            isValida = false;
        }

        if (!isValida) {
            estado = "IV"; // Jogada inválida
            return false;
        }

        // 2. Executa a jogada na classe mãe (Jogo.java)
        boolean fechouCaixa = super.joga(coords, simbolo);

        // 3. Aplica a Regra de Bónus
        if (fechouCaixa) {
            estado = "BO"; // O jogador mantém o turno
        }

        // 4. Verifica se o jogo acabou
        if (super.jogoTerminou()) {
            int pontosX = 0;
            int pontosO = 0;
            
            // Conta os pontos
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (caixas[i][j] == 'X') pontosX++;
                    else if (caixas[i][j] == 'O') pontosO++;
                }
            }
            
            // Define o vencedor
            if (pontosX > pontosO) estado = "VX";
            else if (pontosO > pontosX) estado = "VO";
            else estado = "EM"; // Empate
        }

        return true;
    }

    /**
     * Indica se o jogo terminou. O jogo só termina em caso de Vitória ou Empate.
     */
    public boolean terminou() {
        return estado.equals("VX") || estado.equals("VO") || estado.equals("EM");
    }
}