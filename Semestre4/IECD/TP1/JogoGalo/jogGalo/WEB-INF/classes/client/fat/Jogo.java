package client.fat;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Scanner;

/**
 * Classe que implementa as regras do Jogo do Galo.
 *
 * @author Engº Porfírio Filipe
 */
public class Jogo implements Serializable{

	private static final long serialVersionUID = 1L;
	/**
	 * Tabuleiro do jogo da galo (3x3).
	 */
	private char[][] tabuleiro = { { '1', '2', '3' }, { '4', '5', '6' }, { '7', '8', '9' } };

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
	private boolean vitoria(char simbolo) {
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
	private boolean empate() {
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
	 * @return true   Se a jogada é válida, false caso contrário.
	 */
	protected boolean joga(short numero, char simbolo) {
		// Valida o número da casa.
		if (numero > 9 || numero < 1) 
			return false; // salta a jogada

		// Converte o número da casa para índices na matriz.
		numero--;
		int linha = numero / 3;
		int coluna = numero % 3;

		// Verifica se a casa está disponível.
		if (tabuleiro[linha][coluna] == 'X' || tabuleiro[linha][coluna] == 'O') 
			return false; // casa ocupada

		// Preenche a casa com o símbolo do jogador.
		tabuleiro[linha][coluna] = simbolo;
		System.out.println("Assinalou ("+linha+", "+coluna+"): "+simbolo);
		return true;
	}
	
	/**
	 * Converte o tabuleiro numa string visual com numeração de 1 a 9 📍.
	 * Substitui os números pelos emojis ✖️ ou ⭕ conforme as jogadas.
	 *
	 * @return String com o tabuleiro formatado.
	 */
	protected String jogoToTXT() {
	    StringBuilder sb = new StringBuilder();
	    int contador = 1; // Para identificar as quadriculas de 1 a 9

	    for (int i = 0; i < 3; i++) {
	        for (int j = 0; j < 3; j++) {
	            char celula = tabuleiro[i][j];

	            if (celula == 'X') {
	                sb.append(" ✖️ ");
	            } else if (celula == 'O') {
	                sb.append(" ⭕ ");
	            } else {
	                // Mostra o número da casa (1-9) se estiver vazia 🔢
	                // Espaçamento extra para compensar a largura dos emojis
	                sb.append(" ").append(contador).append("  ");
	            }

	            if (j < 2) {
	                sb.append("|");
	            }
	            contador++;
	        }
	        sb.append("\n");
	        if (i < 2) {
	            sb.append("------------\n");
	        }
	    }
	    return sb.toString();
	}
	
	/**
	 * Verifica se o jogo acabou e anuncia o resultado 🏁.
	 */
	protected boolean terminou(PrintStream saida) {
		String msg = "";
		if (vitoria('X')) msg = "🏆 Vitória do ✖️";
		else if (vitoria('O')) msg = "🏆 Vitória do ⭕";
		else if (empate()) msg = "Empate! 🤝";

		if (!msg.equals("")) {
			saida.println("\n" + jogoToTXT());
			saida.println("📢 " + msg + "!");
			return true;
		}
		return false;
	}
	
	/**
	 * Realiza a jogada do utilizador e valida o input 📍.
	 */
	protected short joga(char simbolo, PrintStream saida, Scanner leitor) {
		saida.println("\n" + jogoToTXT());
		saida.println("👉 É a tua vez (" + simbolo + ")! Introduz a posição (1-9): ");
		
		while (true) {
			if (leitor.hasNextShort()) {
				short numero = leitor.nextShort();
				// Tenta aplicar a jogada no tabuleiro herdado de Jogo
				if (!joga(numero, simbolo)) {
					saida.println("❌ Jogada inválida ou posição ocupada! Tenta outra: ");
				} else {
					return numero; // Jogada confirmada com sucesso ✅
				}
			} else {
				// Limpa lixo do buffer (ex: se o user escrever letras) 🧹
				leitor.next();
				saida.println("⚠️ Por favor, insere apenas números entre 1 e 9.");
			}
		}
	}
}
