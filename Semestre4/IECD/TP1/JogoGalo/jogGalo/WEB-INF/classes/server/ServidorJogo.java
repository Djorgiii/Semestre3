package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Classe `Accept` representa uma thread responsável por tratar a
 * comunicação entre dois jogadores que interagem durante um jogo do galo.
 * 
 * Esta classe é responsável por:
 * * Gerir a comunicação entre os jogadores X e O.
 * * Ler e escrever dados nos sockets dos jogadores.
 * * Controlar o fluxo do jogo, alternando entre jogadores.
 * * Fechar os sockets quando o jogo termina.
 * 
 * @author Engº Porfírio Filipe
 * 
 */
class ServidorDedicado extends Thread {

    // **Atributos:**
	
	// Tempo máximo para jogar
	final int timeout = 1000*30;  // 0 - sem timeout

    /**
     * Socket da ligação com o jogador X.
     */
    private Socket connectionX = null; 

    /**
     * Socket da ligação com o jogador O.
     */
    private Socket connectionO = null; 

    // **Construtor:**

    /**
     * Manipula os circuitos virtuais dos jogadores.
     *
     * @param connection1 Socket do jogador X.
     * @param connection2 Socket do jogador O.
     */
    public ServidorDedicado(Socket connection1, Socket connection2) {
        this.connectionX = connection1;
        this.connectionO = connection2;
    }

    /**
     * Método executado pela thread para gerir um jogo.
     * 
     * Este método é responsável por:
     * * Criar streams de leitura e escrita para os sockets.
     * * Iniciar o jogo.
     * * Gerir o ciclo de jogadas entre os jogadores.
     * * Fechar os sockets quando o jogo termina.
     */
    public void run() {

        try (
            // Cria streams para leitura e escrita de dados nos sockets
        	
        	// **Socket X:**
            // Stream para ler dados do socket X.
            BufferedReader isX = new BufferedReader(new InputStreamReader(connectionX.getInputStream()));
            // Stream para escrever dados no socket X.
            PrintWriter osX = new PrintWriter(connectionX.getOutputStream(), true);
        	
        	// **Socket O:**
            // Stream para ler dados do socket O.
            BufferedReader isO = new BufferedReader(new InputStreamReader(connectionO.getInputStream()));
            // Stream para escrever dados no socket X.
            PrintWriter osO = new PrintWriter(connectionO.getOutputStream(), true);
        ) {
        	// Define timeout para inatvidade
        	connectionX.setSoTimeout(timeout);
        	connectionO.setSoTimeout(timeout);
        	
            // **Informação sobre a thread**

            System.out.println("Iniciou a Thread ("+ this.threadId()+") do servidor dedicado:");

            // **Criação do jogo**

            // Cria uma nova instância do jogo.
            JogoXML jogo = new JogoXML();

            // Ciclo para gerir a interação entre jogadores

            for(;;) {

                // ** Jogador X **
            	// **Obter tabuleiro:**
                // Atualiza o tabuleiro e o estado do jogo do jogador X.
            	Skeleton.runObter(isX, osX, 'X', connectionX, jogo);
            	
            	// **Processar jogada:**
                // Se o jogo não terminou, concretiza a jogada do jogador X.
                if(!jogo.terminou()) 
                	jogo = Skeleton.runJogar(isX, osX, 'X', connectionX, jogo);

                // ** Jogador O **
                // **Obter tabuleiro:**
                // Atualiza o tabuleiro e o estado do jogo do jogador O.
                Skeleton.runObter(isO, osO, 'O', connectionO, jogo);
                
                // **Processar jogada:**
                // Se o jogo não terminou, concretiza a jogada do jogador O.
                if(!jogo.terminou()) 
                	jogo = Skeleton.runJogar(isO, osO, 'O', connectionO, jogo);
            }
		} catch (Exception e) {
			System.out.println("Servidor dedicado: terminou o jogo ("+e.getMessage()+")!");
			// e.printStackTrace();
		} finally {
			// Garante que os sockets são fechados, mesmo em caso de exceção
			try {
				connectionX.close();
				connectionO.close();
			} catch (IOException e) {
				// Ignora a exceção caso ocorra algum erro ao fechar
			}
		}
		System.out.println("Servidor dedicado: terminou a Thread ("+ this.threadId()+") do servidor dedicado!");
	} // fim run
} // end Servidor Dedicado