package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ServidorDedicado extends Thread {

    final int timeout = 0; // 30 segundos de timeout (podes alterar)
    private Socket connectionX = null; 
    private Socket connectionO = null; 

    public ServidorDedicado(Socket connection1, Socket connection2) {
        this.connectionX = connection1;
        this.connectionO = connection2;
    }

    public void run() {
        try (
            BufferedReader isX = new BufferedReader(new InputStreamReader(connectionX.getInputStream()));
            PrintWriter osX = new PrintWriter(connectionX.getOutputStream(), true);
            
            BufferedReader isO = new BufferedReader(new InputStreamReader(connectionO.getInputStream()));
            PrintWriter osO = new PrintWriter(connectionO.getOutputStream(), true);
        ) {
            connectionX.setSoTimeout(timeout);
            connectionO.setSoTimeout(timeout);
            
            System.out.println("Iniciou a Thread ("+ this.getId()+") do servidor dedicado!");

            JogoXML jogo = new JogoXML();
            char turno = 'X'; // O Jogador X começa sempre

            // Ciclo dinâmico de turnos
            for(;;) {
                if (jogo.terminou()) {
                    // Se o jogo acabou, envia o estado final para ambos e sai do ciclo
                    Skeleton.runObter(isX, osX, 'X', connectionX, jogo);
                    Skeleton.runObter(isO, osO, 'O', connectionO, jogo);
                    break;
                }

                if (turno == 'X') {
                    Skeleton.runObter(isX, osX, 'X', connectionX, jogo);
                    jogo = Skeleton.runJogar(isX, osX, 'X', connectionX, jogo);
                    
                    // Se a jogada foi válida e NÃO fechou caixa, passa a vez ao 'O'
                    if (!jogo.getEstado().equals("BO") && !jogo.getEstado().equals("IV")) {
                        turno = 'O'; 
                    }
                } else { // Turno do 'O'
                    Skeleton.runObter(isO, osO, 'O', connectionO, jogo);
                    jogo = Skeleton.runJogar(isO, osO, 'O', connectionO, jogo);
                    
                    // Se a jogada foi válida e NÃO fechou caixa, passa a vez ao 'X'
                    if (!jogo.getEstado().equals("BO") && !jogo.getEstado().equals("IV")) {
                        turno = 'X';
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Servidor dedicado: terminou o jogo (" + e.getMessage() + ")!");
        } finally {
            try {
                if (connectionX != null) connectionX.close();
                if (connectionO != null) connectionO.close();
            } catch (IOException e) {}
        }
        System.out.println("Servidor dedicado: terminou a Thread ("+ this.getId()+")!");
    } 
}