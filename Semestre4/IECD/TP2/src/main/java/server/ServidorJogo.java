package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.XMLDoc;

class ServidorDedicado extends Thread {

    private Socket connectionX = null;
    private Socket connectionO = null;

    public ServidorDedicado(Socket connection1, Socket connection2) {
        this.connectionX = connection1;
        this.connectionO = connection2;
    }

    public void run() {
        try (
            BufferedReader isX = new BufferedReader(new InputStreamReader(connectionX.getInputStream()));
            PrintWriter osX   = new PrintWriter(connectionX.getOutputStream(), true);
            BufferedReader isO = new BufferedReader(new InputStreamReader(connectionO.getInputStream()));
            PrintWriter osO   = new PrintWriter(connectionO.getOutputStream(), true);
        ) {
            System.out.println("Iniciou a Thread (" + this.getId() + ") do servidor dedicado!");

            JogoXML jogo = new JogoXML();
            char turno = 'X';

            for (;;) {
                // Seleccionar streams do jogador activo e passivo
                BufferedReader isAtivo   = (turno == 'X') ? isX : isO;
                PrintWriter    osAtivo   = (turno == 'X') ? osX : osO;
                BufferedReader isPassivo = (turno == 'X') ? isO : isX;
                PrintWriter    osPassivo = (turno == 'X') ? osO : osX;

                // -------------------------------------------------------
                // PASSO 1: Ler a próxima mensagem do jogador ACTIVO.
                // Pode ser <obter> (pede tabuleiro) ou <jogar> (joga).
                // Continua a responder a <obter> até receber um <jogar>.
                // -------------------------------------------------------
                String jogadaFeita = null;
                while (jogadaFeita == null) {
                    String linha = isAtivo.readLine();
                    if (linha == null) throw new Exception("Ligação perdida (" + turno + ")");

                    Document doc = XMLDoc.parseString(linha);

                    if (doc.getElementsByTagName("obter").getLength() > 0) {
                        // Responde com o tabuleiro actual
                        osAtivo.println("<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>");

                    } else if (doc.getElementsByTagName("jogar").getLength() > 0) {
                        // Processa a jogada
                        Element jogadaEl = (Element) doc.getElementsByTagName("jogar").item(0);
                        String jogadaStr = jogadaEl.getAttribute("jogada");
                        String[] partes  = jogadaStr.trim().split("\\s+");

                        if (partes.length == 4) {
                            int[] coords = new int[4];
                            for (int i = 0; i < 4; i++) coords[i] = Integer.parseInt(partes[i]);
                            jogo.joga(coords, turno);
                        }

                        // Responde ao jogador activo com o tabuleiro actualizado
                        osAtivo.println("<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>");
                        jogadaFeita = jogo.getEstado();

                    } else {
                        System.out.println("Servidor dedicado: mensagem desconhecida ignorada: " + linha);
                    }
                }

                // -------------------------------------------------------
                // PASSO 2: Decidir o que fazer com base no estado
                // -------------------------------------------------------
                if (jogo.terminou()) {
                    // Jogo acabou — responder a qualquer obter pendente do passivo e sair
                    String linhaPassivo = isPassivo.readLine();
                    if (linhaPassivo != null) {
                        osPassivo.println("<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>");
                    }
                    System.out.println("Jogo terminado! Estado final: " + jogadaFeita);
                    break;
                }

                if ("IV".equals(jogadaFeita)) {
                    // Jogada inválida: o mesmo jogador joga outra vez (turno não muda)
                    continue;
                }

                if ("BO".equals(jogadaFeita)) {
                    // Bónus: fechou caixa, joga outra vez (turno não muda)
                    continue;
                }

                // Jogada normal: passa a vez
                turno = (turno == 'X') ? 'O' : 'X';

                // -------------------------------------------------------
                // PASSO 3: Responder ao jogador PASSIVO que estava bloqueado
                // em obter() à espera que o activo jogasse.
                // O passivo já enviou <obter> — lemos e respondemos.
                // -------------------------------------------------------
                String linhaPassivo = isPassivo.readLine();
                if (linhaPassivo == null) throw new Exception("Ligação perdida (passivo)");
                // (Ignoramos o conteúdo — sabemos que é <obter>)
                osPassivo.println("<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>");
            }

        } catch (Exception e) {
            System.out.println("Servidor dedicado: terminou o jogo (" + e.getMessage() + ")!");
        } finally {
            try { if (connectionX != null) connectionX.close(); } catch (IOException e) {}
            try { if (connectionO != null) connectionO.close(); } catch (IOException e) {}
        }
        System.out.println("Servidor dedicado: terminou a Thread (" + this.getId() + ")!");
    }
}