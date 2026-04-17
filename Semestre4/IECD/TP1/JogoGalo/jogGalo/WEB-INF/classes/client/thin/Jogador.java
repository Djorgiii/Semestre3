package client.thin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Classe ClienteTCP genérico, simula uma consola. 
 * Implementa o conceito de Thin Client com atualização do ecrã realizada com uma tarefa.
 * 
 * @author Engº Porfírio Filipe
 */
public class Jogador {

	/**
	 * Host padrão do servidor (endereço IP).
	 */
	private final static String DEFAULT_HOST = "localhost";

	/**
	 * Porta padrão do servidor.
	 */
	private final static int DEFAULT_PORT = 5025;

	/**
	 * Método principal do Cliente.
	 * 
	 * @param args argumentos da linha de comando: host e port
	 */
	public static void main(String[] args) {
		String host = DEFAULT_HOST;
		int port = DEFAULT_PORT;
		// Acede aos parametros se existirem
		if (args.length == 2) {
			host = args[0];
			port = Integer.parseInt(args[1]);
		}

		try (
                // Cria um socket para se ligar ao servidor.
                Socket socket = new Socket(host, port);
                // Stream para ler dados do socket.
                BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Stream para escrever dados no socket.
                PrintWriter os = new PrintWriter(socket.getOutputStream(), true);
                // Cria um scanner para ler a entrada do utilizador.
                Scanner leitor = new Scanner(System.in)) {

            // Mostra informações sobre a ligação.
            System.out.println("Java-> Ligação estabelecida: " + socket);

            // Cria uma thread para ler do socket e escrever no ecrã.
			Thread threadLeitura = new Thread(() -> {
				try {
					String line;
					while ((line = is.readLine()) != null) {
                        if (line.equals("")) { // salta linhas vazias
                            continue;
                        }
						System.out.println(line);
					}
				} catch (IOException e) {
					System.err.println("Ligação cancelada remotamente pelo servidor!" + e.getLocalizedMessage());
				}
			});
            threadLeitura.start();

            // Loop para enviar mensagens para o servidor.
            for (;;) {
                // Envia a linha escrita pelo jogador para o servidor.
                os.println(leitor.nextLine());
            }
        } catch (IOException e) {
            System.err.println("Erro na ligação: " + e.getLocalizedMessage());
        }
	} // fim do método main
} // fim da classe
