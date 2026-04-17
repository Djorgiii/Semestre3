package server;

import java.io.*;
import java.net.Socket;

class ServidorDedicado extends Thread {
    final int timeout = 1000 * 30; 
    private Socket connectionX = null, connectionO = null; 

    public ServidorDedicado(Socket cx, Socket co) { this.connectionX = cx; this.connectionO = co; }

    public void run() {
        try (
            BufferedReader isX = new BufferedReader(new InputStreamReader(connectionX.getInputStream()));
            PrintWriter osX = new PrintWriter(connectionX.getOutputStream(), true);
            BufferedReader isO = new BufferedReader(new InputStreamReader(connectionO.getInputStream()));
            PrintWriter osO = new PrintWriter(connectionO.getOutputStream(), true);
        ) {
            connectionX.setSoTimeout(timeout); connectionO.setSoTimeout(timeout);
            JogoXML jogo = new JogoXML(); char turno = 'X'; 

            for(;;) {
                if (jogo.terminou()) {
                    Skeleton.runObter(isX, osX, 'X', connectionX, jogo);
                    Skeleton.runObter(isO, osO, 'O', connectionO, jogo);
                    break;
                }

                if (turno == 'X') {
                    Skeleton.runObter(isX, osX, 'X', connectionX, jogo);
                    jogo = Skeleton.runJogar(isX, osX, 'X', connectionX, jogo);
                    if (!jogo.getEstado().equals("BO") && !jogo.getEstado().equals("IV")) turno = 'O'; 
                } else {
                    Skeleton.runObter(isO, osO, 'O', connectionO, jogo);
                    jogo = Skeleton.runJogar(isO, osO, 'O', connectionO, jogo);
                    if (!jogo.getEstado().equals("BO") && !jogo.getEstado().equals("IV")) turno = 'X';
                }
            }
        } catch (Exception e) { System.out.println("Terminou: " + e.getMessage()); }
    } 
}