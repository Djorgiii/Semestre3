package server;

/**
 * Classe que implementa o adaptador do jogo para XML.
 *
 * @author Engº Porfírio Filipe
 */
public class JogoXML extends Jogo {

    /**
     * Estado do jogo após a última jogada.
     * Possíveis valores:
     * - "ND": Nada a registar, continua o jogo.
     * - "IV": Jogada inválida.
     * - "VX": Vitória do X.
     * - "VO": Vitória do O.
     * - "EM": Empate.
     */
    private String estado = "ND";

    /**
     * Converte o tabuleiro do jogo para XML e inclui o estado.
     *
     * @return String com XML que representa o tabuleiro.
     */
    public String tabuleiroToXML() {
        String tab = "<tabuleiro estado='" + estado + "'>";
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tab += "<casa simbolo='" + tabuleiro[i][j] + "'/>";
            }
        }
        return tab += "</tabuleiro>";
    }

    /**
     * Concretiza a jogada e atualiza o estado do jogo.
     *
     * @param 		numero  Número da casa (entre 1 e 9).
     * @param 		simbolo Símbolo do jogador ('X' ou 'O').
     * @return 		true se a jogada é válida, false caso contrário.
     */
	public boolean joga(short numero, char simbolo) {
		estado = "ND"; // Nada a registar, continua o jogo.

		// Verifica se a jogada é válida no jogo base.
		if (!super.joga(numero, simbolo)) {
			estado = "IV"; // Jogada inválida.
			return false;
		}

		// Verifica se há vitória.
		if (vitoria('X'))
			estado = "VX"; // Vitória do X.
		else if (vitoria('O'))
			estado = "VO"; // Vitória do O.

			// Verifica se há empate.
			else if (empate())
				estado = "EM"; // Empate.

		return true;
	}

    /**
     * Indica se o jogo terminou com base no estado atual.
     *
     * @return true se o jogo terminou, false caso contrário.
     */
    public boolean terminou() {
        return !estado.equals("ND") && !estado.equals("IV");
    }
}