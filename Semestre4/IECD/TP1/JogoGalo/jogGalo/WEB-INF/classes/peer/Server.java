package peer;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Classe Servidor genérico, adaptação a um servidor concreto.
 * @author Engº Porfírio Filipe 
 */
public class Server {
    /**
     * Porto por omissão do servidor.
     */
    private final static int DEFAULT_PORT = 5025;
       
	public static void main(String[] args) throws ClassNotFoundException, IOException {
    	// 🛠️ Força o output do servidor a ser interpretado como UTF-8
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        System.setErr(new PrintStream(System.err, true, "UTF-8"));
        String[] argumentos = {String.valueOf(DEFAULT_PORT)};
		server.Servidor.main(argumentos);
	}
}
