package peer;

import java.io.IOException;
import java.util.Scanner;

/**
 * Classe Cliente genérico, adaptação a um servidor concreto.
 * @author Engº Porfírio Filipe 
 */
public class Cliente {
    // **Constantes:**
    /**
     * Host por omissão do servidor (endereço IP).
     */
    private final static String DEFAULT_HOST = "localhost";

    /**
     * Porto por omissão do servidor.
     */
    private final static int DEFAULT_PORT = 5025;
    
    String host = DEFAULT_HOST;
    int port = DEFAULT_PORT;
    
    // Acesso único ao teclado, Define um Scanner para ser reutilziado
    private static Scanner sc = null; 
    
    // Configuração inicial do client no peer
    public Cliente(String Host, int Port, Scanner Sc) {
    	host = Host;
    	port = Port;
    	sc = Sc;
    }
       
	public void main(String[] args) throws ClassNotFoundException, IOException {
		new client.Jogador(host, port, sc).main(null);
	}
}
