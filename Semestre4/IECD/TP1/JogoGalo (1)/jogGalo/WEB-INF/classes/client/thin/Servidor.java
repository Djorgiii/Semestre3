package client.thin;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Classe ServidorTCPConcorrente tem por base um servidor TCP concorrente. 
 * Cada tarefa disponibiliza acesso remoto a um jogo do galo mantido no servidor.
 * 
 * @author Engº Porfírio Filipe
 */
public class Servidor {

	/**
	 * Porta padrão onde o servidor irá aguardar conexões de clientes.
	 */
	public final static int DEFAULT_PORT = 5025;

	/**
	 * Método principal do programa ServidorTCPConcorrente.
	 * 
	 * @param args argumentos da linha de comando: port
	 */
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		// Acede ao parametro se existir
		if(args.length==1) 
			port = Integer.parseInt(args[0]);
		// Cria um socket servidor na porta especificada
		try (ServerSocket serverSocket = new ServerSocket(port);) {

			System.out.println("Servidor TCP concorrente iniciado...");
			// Loop infinito para aguardar ligações de clientes
			for (;;) {
				System.out.println("Aguarda ligação no porto " + port + "...");
				// Espera ligação do primeiro jogador
				System.out.println("Espera pelo Jogador X");
				Socket newSock1 = serverSocket.accept();
				System.out.println("Aceitou ligação do Jogador X");
				// Espera ligação do segundo jogador
				System.out.println("Espera pelo Jogador O");
				Socket newSock2 = serverSocket.accept();
				System.out.println("Aceitou ligação do Jogador O");
				// Cria uma nova thread gerir o jogo entre os dois jogadores
				Thread th = new HandleConnectionThread2(newSock1, newSock2);
				th.start(); // Inicia a thread para executar a interação com os jogadores
			}
			
		} catch (IOException e) {
			System.err.println("Exceção no servidor: " + e.getLocalizedMessage());
		}
	} // fim main
} // end ServidorTCPConcorrente

/**
 * Classe HandleConnectionThread representa uma thread responsável por tratar a
 * comunicação com dois clientes ligados que interagem durante um jogo.
 * 
 */
class HandleConnectionThread2 extends Thread {

	private Socket connection1 = null; // Socket da ligação com o jogador X
	private Socket connection2 = null; // Socket da ligação com o jogador O

	public HandleConnectionThread2(Socket connection1, Socket connection2) {
		this.connection1 = connection1;
		this.connection2 = connection2;
	}

	/**
	 * Método executado pela thread para gerir um jogo.
	 */
	@Override
	public void run() {
	    // 💡 O try-with-resources garante que os Scanners e PrintStreams fecham sozinhos
	    try (
	        Scanner scX = new Scanner(connection1.getInputStream());
	        Scanner scO = new Scanner(connection2.getInputStream());
	        PrintStream osX = new PrintStream(connection1.getOutputStream(), true);
	        PrintStream osO = new PrintStream(connection2.getOutputStream(), true)
	    ) {
	        System.out.println("🧵 Thread " + this.threadId() + ":");
	        System.out.println("	🎮 Jogador X: " + connection1);
	        System.out.println("	🎮 Jogador O: " + connection2);

	        // Gere esta instância do jogo
	        Jogo jogo = new Jogo();

	        // Ciclo para gerir a interação entre os jogadores
	        for (;;) {
	            // Vez do X ✖️
	            jogo.joga('X', osX, scX);
	            if (jogo.terminou(osX)) {
	                jogo.terminou(osO);
	                break;
	            } 
	            // Vez do O ⭕
	            jogo.joga('O', osO, scO);
	            if (jogo.terminou(osO)) {
	                jogo.terminou(osX);
	                break;
	            }
	        }
	    } catch (IOException e) {
	        System.out.println("🔌 A ligação foi interrompida inesperadamente!");
	    } finally {
	        // Garante que os sockets são fechados para libertar recursos do SO 🛡️
	        try {
	            if (connection1 != null) connection1.close();
	        } catch (IOException e) {
	            // Erro ao fechar? Ignoramos em silêncio... 🤫
	        }
	        try {
	            if (connection2 != null) connection2.close();
	        } catch (IOException e) {
	            // Erro ao fechar? Ignoramos em silêncio... 🤫
	        }
	    }
	    System.out.println("🏁 Terminou a Thread (servidor dedicado) " + this.threadId());
	} // fim run
} // end HandleConnectionThread