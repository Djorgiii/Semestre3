package server;

/**
 * Classe que implementa as regras do Jogo da Galo.
 *
 * @author Engº Porfírio Filipe
 */
public class Jogo {

	/**
	 * Tabuleiro do jogo da galo (3x3).
	 */
	protected char[][] tabuleiro = { { '1', '2', '3' }, { '4', '5', '6' }, { '7', '8', '9' } };

	/**
	 * Construtor sem argumentos que inicializa o tabuleiro com os números de 1 a 9.
	 */
	public Jogo() {

	}

	/**
	 * Verifica se o jogador com o símbolo especificado venceu o jogo.
	 *
	 * @param simbolo Símbolo do jogador.
	 * @return true se o jogador venceu, false caso contrário.
	 */
	public boolean vitoria(char simbolo) {
		// Verifica linhas.
		for (int i = 0; i < 3; i++) {
			if (tabuleiro[i][0] == simbolo && tabuleiro[i][1] == simbolo && tabuleiro[i][2] == simbolo) {
				return true;
			}
		}

		// Verifica colunas.
		for (int i = 0; i < 3; i++) {
			if (tabuleiro[0][i] == simbolo && tabuleiro[1][i] == simbolo && tabuleiro[2][i] == simbolo) {
				return true;
			}
		}

		// Verifica diagonais.
		if (tabuleiro[0][0] == simbolo && tabuleiro[1][1] == simbolo && tabuleiro[2][2] == simbolo) {
			return true;
		}
		if (tabuleiro[0][2] == simbolo && tabuleiro[1][1] == simbolo && tabuleiro[2][0] == simbolo) {
			return true;
		}

		return false;
	}

	/**
	 * Determina um empate detetando se o tabuleiro está completo.
	 *
	 * @return true se existe empate, false caso contrário.
	 */
	public boolean empate() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (tabuleiro[i][j] != 'X' && tabuleiro[i][j] != 'O') {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Assinala a jogada 'simbolo' na casa assinala pelo 'numero'.
	 *
	 * @param numero  Identifica a casa da jogada (1..9).
	 * @param simbolo Símbolo do jogador.
	 * @return true se a jogada é válida, false caso contrário.
	 */
	public boolean joga(short numero, char simbolo) {
		// Valida o número da casa.
		if (numero > 9 || numero < 1) {
			return false; // salta a jogada
		}

		// Converte o número da casa para índices na matriz.
		numero--;
		int linha = numero / 3;
		int coluna = numero % 3;

		// Verifica se a casa está disponível.
		if (tabuleiro[linha][coluna] == 'X' || tabuleiro[linha][coluna] == 'O') {
			return false; // casa ocupada
		}

		// Preenche a casa com o símbolo do jogador.
		tabuleiro[linha][coluna] = simbolo;
		return true;
	}
}
