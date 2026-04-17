package client.fat;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Classe que implementa a interface com o utilizador na consola.
 * Implementa um cliente gordo com objetos Jogo serializados.
 *
 * @author Engº Porfírio Filipe
 */
public class JogadorSerial {

	/**
	 * Host por omissão do servidor (endereço IP).
	 */
	private final static String DEFAULT_HOST = "localhost";

	/**
	 * Porta padrão do servidor.
	 */
	private final static int DEFAULT_PORT = 5025;

	/**
	 * Stream de output do jogo.
	 */
	private static PrintStream saida = System.out;

	/**
	 * Scanner para ler o input do jogo.
	 */
	private static Scanner leitor = new Scanner(System.in);
	
	/**
	 * Método principal do jogo. Inicia o jogo e controla o loop principal.
	 *
	 * @param args 						Não utilizado.
	 * @throws IOException 				Em caso de erro.
	 * @throws ClassNotFoundException	Em caso de erro. 
	 */
	public static void main(String[] args) {
	    // Usar try-with-resources para o Socket garante o fecho automático
	    try (Socket socket = new Socket(DEFAULT_HOST, DEFAULT_PORT)) {
	        System.out.println("Java-> Ligação estabelecida: " + socket);

	        // Receber o símbolo (ajustado para bater com o que o servidor envia)
	        char simbolo = (char) socket.getInputStream().read();
	        System.out.println("Símbolo atribuído: '" + simbolo + "'");
	        boolean minhaVez = (simbolo == 'X');
	        
	        // Streams para serialização, por esta ordem
	        try(ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
	            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
	        
	        // Cria um objeto jogo que vai ser serializado
	        Jogo objetoJogo = new Jogo(); 
	        
	        while (true) {
	        	if (minhaVez) {// Minha jogada
	            	objetoJogo.joga(simbolo, saida, leitor);
	                // Enviar jogo atualizado
	                out.writeObject(objetoJogo);
	                // Esvazia a memória intermédia
	                out.flush();
	                // Limpa o cache de serialização para enviar o objeto alterado
	                out.reset(); 
	                if (objetoJogo.terminou(saida)) 
	                	break;
	            } else {// Jogada do Oponente
	                System.out.println("Aguardando jogada do oponente...");
	                // Receber jogo do oponente
	                objetoJogo = (Jogo) in.readObject();
	                if (objetoJogo.terminou(saida)) 
	                	break;
	            }
	            // Alterna o turno
	            minhaVez = !minhaVez;
	        }
	      }
	    } catch (IOException | ClassNotFoundException e) {
	        System.out.println("Erro na ligação: " + e.getMessage());
	    }
	    System.out.println("Terminou o jogo!");
	}
}
