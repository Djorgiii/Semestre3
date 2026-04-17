package server;

public class JogoXML extends Jogo {
    private String estado = "ND";

    public String getEstado() { return this.estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String tabuleiroToXML() {
        StringBuilder tab = new StringBuilder("<tabuleiro estado='" + estado + "'>");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                if (horiz[i][j]) tab.append(String.format("<linha x1='%d' y1='%d' x2='%d' y2='%d'/>", j+1, i+1, j+2, i+1));
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (vert[i][j]) tab.append(String.format("<linha x1='%d' y1='%d' x2='%d' y2='%d'/>", j+1, i+1, j+1, i+2));
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (caixas[i][j] != ' ') tab.append(String.format("<caixa dono='%c' x='%d' y='%d'/>", caixas[i][j], j+1, i+1));
            }
        }
        return tab.append("</tabuleiro>").toString();
    }

    public boolean joga(int[] coords, char simbolo) {
        estado = "ND"; 
        try {
            int x1 = coords[0] - 1, y1 = coords[1] - 1, x2 = coords[2] - 1, y2 = coords[3] - 1;
            boolean isValida = false;
            if (y1 == y2 && Math.abs(x1 - x2) == 1) {
                if (!horiz[y1][Math.min(x1, x2)]) isValida = true;
            } else if (x1 == x2 && Math.abs(y1 - y2) == 1) {
                if (!vert[Math.min(y1, y2)][x1]) isValida = true;
            }
            if (!isValida) { estado = "IV"; return false; }
        } catch (IndexOutOfBoundsException e) { estado = "IV"; return false; }

        if (super.joga(coords, simbolo)) estado = "BO"; // Regra Bónus

        if (super.jogoTerminou()) {
            int pX = 0, pO = 0;
            for (int i=0; i<3; i++) for (int j=0; j<3; j++) { if (caixas[i][j] == 'X') pX++; else if (caixas[i][j] == 'O') pO++; }
            if (pX > pO) estado = "VX"; else if (pO > pX) estado = "VO"; else estado = "EM";
        }
        return true;
    }

    public boolean terminou() { return estado.equals("VX") || estado.equals("VO") || estado.equals("EM"); }
}