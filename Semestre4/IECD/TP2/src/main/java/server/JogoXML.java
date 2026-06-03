package server;


public class JogoXML extends Jogo {

    private String estado = "ND";


    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    

    public String getEstado() {
        return this.estado;
    }
    


    public String tabuleiroToXML() {
        StringBuilder tab = new StringBuilder("<tabuleiro estado='" + estado + "'>");
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                if (horiz[i][j]) {
                    tab.append(String.format("<linha x1='%d' y1='%d' x2='%d' y2='%d'/>", j+1, i+1, j+2, i+1));
                }
            }
        }
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (vert[i][j]) {
                    tab.append(String.format("<linha x1='%d' y1='%d' x2='%d' y2='%d'/>", j+1, i+1, j+1, i+2));
                }
            }
        }
        
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

    public boolean joga(int[] coords, char simbolo) {
        estado = "ND";

        int x1 = coords[0] - 1;
        int y1 = coords[1] - 1;
        int x2 = coords[2] - 1;
        int y2 = coords[3] - 1;

        boolean isValida = false;
        
        try {
            if (y1 == y2 && Math.abs(x1 - x2) == 1) {
                int startX = Math.min(x1, x2);
                if (!horiz[y1][startX]) isValida = true;
            } 
            else if (x1 == x2 && Math.abs(y1 - y2) == 1) {
                int startY = Math.min(y1, y2);
                if (!vert[startY][x1]) isValida = true;
            }
        } catch (IndexOutOfBoundsException e) {
            isValida = false;
        }

        if (!isValida) {
            estado = "IV";
            return false;
        }

        boolean fechouCaixa = super.joga(coords, simbolo);

        if (fechouCaixa) {
            estado = "BO";
        }

        if (super.jogoTerminou()) {
            int pontosX = 0;
            int pontosO = 0;
            
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (caixas[i][j] == 'X') pontosX++;
                    else if (caixas[i][j] == 'O') pontosO++;
                }
            }
            
            if (pontosX > pontosO) estado = "VX";
            else if (pontosO > pontosX) estado = "VO";
            else estado = "EM"; // Empate
        }

        return true;
    }

    public boolean terminou() {
        return estado.equals("VX") || estado.equals("VO") || estado.equals("EM");
    }
}