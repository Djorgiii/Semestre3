package client.fat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe ServidorP implementa um servidor TCP simples que copia bytes
 * de um socket de entrada para outro de saída e vice-versa.
 * 
 * @author Engº Porfírio Filipe
 */
public class Servidor {

    /**
     * Porta padrão do servidor.
     */
    private static final int DEFAULT_PORT = 5025;

    /**
     * Método principal do servidor.
     *
     * @param args 			argumentos da linha de comando (não utilizados)
     * @throws IOException 	caso haja algum erro de E/S
     */
    @SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
        // Cria um socket de servidor na porta DEFAULT_PORT
        ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
        System.out.println("Servidor TCP iniciado no porto " + DEFAULT_PORT);

        // Loop infinito para esperar por conexões de clientes
        for(;;) {
            // Aceita uma nova ligação de cliente
            Socket socketPrimeiro = serverSocket.accept();
            // Mostra informação sobre a ligação de entrada
            System.out.println("Ligação estabelecida com: " + socketPrimeiro);

            // Aceita uma nova ligação de cliente
            Socket socketSegundo = serverSocket.accept();
            // Mostra informação sobre a ligação de saida
            System.out.println("Ligação estabelecida com: " + socketSegundo);
            
            // Cria threads para copiar caracteres em ambas as direções
            // Uma thread para copiar da entrada para a saída
            // Quem tem o 'O' espera, quem tem 'X' envia
            new Thread(() -> copiarCaracteres(socketPrimeiro, socketSegundo, 'X')).start();
            // Outra thread para copiar da saída para a entrada
            new Thread(() -> copiarCaracteres(socketSegundo, socketPrimeiro, 'O')).start();
        }
    }

    /**
     * Copia caracteres de um socket de entrada para um socket de saída.
     *
     * @param entrada 			socket de entrada
     * @param saida 			socket de saída
     * @throws IOException 		caso haja algum erro de E/S
     */
    private static void copiarCaracteres(final Socket entrada, final Socket saida, final char simbolo) {
        // Cria streams de entrada e saída para os sockets
		try (InputStream inputStream 	= entrada.getInputStream();
			 OutputStream outputStream 	= saida.getOutputStream();
			) {
			// atribui o simbolo
	        outputStream.write(simbolo);
	        outputStream.flush();
	        // Enquanto houverem, lê bytes do socket de entrada
	        // e escreve no socket de saída
	        int umByte;
	        while ((umByte = inputStream.read()) != -1) {
	            // Escreve o byte lido no socket de saída
	            outputStream.write(umByte);
	            // Esvazia o buffer de saída
	            outputStream.flush();
	        }
		} catch (IOException e) {
			System.out.println("Terminou "+entrada+"<->"+saida+": "+e.getLocalizedMessage());
		}
		finally {
	        // Fecha os sockets
	        try {
				entrada.close();
		        saida.close();
			} catch (IOException e) {
			}
		}
    }
}

