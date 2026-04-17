package client.fat;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.NoSuchElementException;

/**
 * Classe que implementa a interface para jogar o Jogo do Galo na consola 🎮.
 * Utiliza redirecionamento de input/output para comunicar via Socket com o servidor 🌐.
 *
 * @author Engº Porfírio Filipe
 */
public class Jogador {

	private final static String DEFAULT_HOST = "localhost";
	private final static int DEFAULT_PORT = 5025;

	// Streams para interação com o utilizador local ⌨️
	private final static Scanner leitor = new Scanner(System.in);
	private final static PrintStream escritor = System.out;
	
	/**
	 * Método principal: faz a ponte entre o jogador local e o servidor ⚡.
	 */
	public static void main(String[] args) {
		System.out.println("🚀 A ligar ao servidor de Jogo do Galo...");
		
		try (
			// Ligação ao servidor 🔗
			Socket socket = new Socket(DEFAULT_HOST, DEFAULT_PORT);
			Scanner scRede = new Scanner(socket.getInputStream());
			PrintStream osRede = new PrintStream(socket.getOutputStream(), true)
		) {
			Jogo jogo = new Jogo();
			System.out.println("✅ Ligação estabelecida: " + socket);

			// Recebe o símbolo atribuído pelo servidor (X ou O) 🎫
			int sinal = socket.getInputStream().read();
			if (sinal == -1) throw new IOException("O servidor fechou a ligação precocemente.");
			
			char meuSimbolo = (char) sinal;
			char simboloOponente = (meuSimbolo == 'X') ? 'O' : 'X';
			System.out.println("🎭 Jogas com o símbolo: '" + meuSimbolo + "'");

			// No Jogo do Galo, o 'X' começa sempre ⏱️
			boolean minhaVez = (meuSimbolo == 'X');
			while (true) {
				if (minhaVez) {
					// Turno Local: Eu jogo e envio para a rede 📤
					short jogadaLocal = jogo.joga(meuSimbolo, escritor, leitor);
					osRede.println(jogadaLocal);
					if (jogo.terminou(escritor)) break;
					minhaVez = false; // Passo a vez ao oponente
				} else {
					// Turno Remoto: Espero pela jogada do outro 📥
					System.out.println("⏳ Aguarda a jogada do oponente...");
					if (scRede.hasNextShort()) {
						short jogadaRemota = scRede.nextShort();
						jogo.joga(jogadaRemota, simboloOponente);
						if (jogo.terminou(escritor)) break;
						minhaVez = true; // Volta a ser o meu turno
					} else {
						System.out.println("🔌 Oponente desconectado.");
						break;
					}
				}
			}
		} catch (IOException | NoSuchElementException e) {
			System.out.println("💥 A ligação foi interrompida ou fechada!");
		} 
		System.out.println("🏁 O programa terminou. Até à próxima!");
	}
}