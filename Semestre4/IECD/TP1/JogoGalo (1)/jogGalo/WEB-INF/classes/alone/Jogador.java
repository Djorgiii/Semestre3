package alone;

import java.io.PrintStream;
import java.util.Scanner;


/**
 * Classe que implementa o Jogo do Galo na consola 🎮.
 * Utiliza redirecionamento de input/output para comunicar.
 *
 * @author Engº Porfírio Filipe
 */
public class Jogador {

	// Streams para interação com o utilizador local ⌨️
	private final static Scanner leitor = new Scanner(System.in);
	private final static PrintStream escritor = System.out;
	
	/**
	 * Método principal: faz a ponte entre o jogador local e o servidor ⚡.
	 */
	public static void main(String[] args) {
		System.out.println("🚀 Jogo do Galo...");
		Jogo jogo = new Jogo();
		while (true) {// No Jogo do Galo, o 'X' começa sempre ⏱️
			jogo.joga('X', escritor, leitor);
			if (jogo.terminou(escritor)) 
				break;
			jogo.joga('O', escritor, leitor);
			if (jogo.terminou(escritor)) 
				break;
		}
		System.out.println("🏁 O programa terminou. Até à próxima!");
	}
}