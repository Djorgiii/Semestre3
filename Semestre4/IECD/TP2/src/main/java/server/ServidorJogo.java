package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.time.LocalDateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.XMLDoc;

class ServidorDedicado extends Thread {

    // Tempo máximo (em milissegundos) que cada jogador tem para jogar
    private static final int TIMEOUT_JOGADA_MS = 30_000;

    /** Regista uma mensagem no protocolo.log no formato do protocolo. */
    private static synchronized void registaLog(String prefixo, String mensagem) {
        // Remove quebras de linha da mensagem para manter uma entrada por linha
        String linha = mensagem == null ? "null" : mensagem.replaceAll("[\r\n]", "");
        String entrada = LocalDateTime.now() + " - " + prefixo + "{" + linha + "}";
        try (PrintWriter escritor = new PrintWriter(new BufferedWriter(
                new FileWriter(XMLDoc.getContexto() + "protocolo.log", true)))) {
            escritor.println(entrada);
        } catch (IOException e) {
            System.err.println("[LOG] Nao foi possivel escrever no protocolo.log: " + e.getMessage());
        }
    }

    private Socket connectionX = null;
    private Socket connectionO = null;

    // Nomes dos jogadores, preenchidos após autenticação
    private String nomeX = null;
    private String nomeO = null;

    public ServidorDedicado(Socket connection1, Socket connection2,
                            String nomeX, String nomeO) {
        this.connectionX = connection1;
        this.connectionO = connection2;
        this.nomeX = nomeX;
        this.nomeO = nomeO;
    }

    public void run() {
        // Instante de início do jogo para calcular duração
        Instant inicio = Instant.now();
        String vencedor = "AB"; // por omissão: abandono

        try (
            BufferedReader isX = new BufferedReader(new InputStreamReader(connectionX.getInputStream()));
            PrintWriter    osX = new PrintWriter(connectionX.getOutputStream(), true);
            BufferedReader isO = new BufferedReader(new InputStreamReader(connectionO.getInputStream()));
            PrintWriter    osO = new PrintWriter(connectionO.getOutputStream(), true);
        ) {
            System.out.println("Iniciou a Thread (" + this.getId() + ") do servidor dedicado! "
                + nomeX + "(X) vs " + nomeO + "(O)");

            JogoXML jogo = new JogoXML();
            char turno = 'X';

            for (;;) {
                // Seleccionar streams do jogador activo e passivo
                BufferedReader isAtivo   = (turno == 'X') ? isX : isO;
                PrintWriter    osAtivo   = (turno == 'X') ? osX : osO;
                BufferedReader isPassivo = (turno == 'X') ? isO : isX;
                PrintWriter    osPassivo = (turno == 'X') ? osO : osX;
                Socket         skAtivo   = (turno == 'X') ? connectionX : connectionO;

                // -------------------------------------------------------
                // PASSO 1: Ler mensagens do jogador ACTIVO.
                // Responde a <obter> até receber <jogar>.
                // O timeout de 30s inicia após o primeiro <obter>.
                // -------------------------------------------------------
                String  jogadaFeita   = null;
                boolean timerIniciado = false;

                while (jogadaFeita == null) {
                    String linha = isAtivo.readLine();
                    if (linha == null) throw new Exception("Ligação perdida (" + turno + ")");
                    registaLog("Servidor", linha);

                    Document doc = XMLDoc.parseString(linha);

                    if (doc.getElementsByTagName("obter").getLength() > 0) {
                        // Envia o tabuleiro actual
                        String respostaObter = "<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>";
                        registaLog("Cliente", respostaObter);
                        osAtivo.println(respostaObter);

                        // Activa o timeout após o jogador receber o tabuleiro pela 1ª vez
                        if (!timerIniciado) {
                            skAtivo.setSoTimeout(TIMEOUT_JOGADA_MS);
                            timerIniciado = true;
                        }

                    } else if (doc.getElementsByTagName("jogar").getLength() > 0) {
                        // Jogada recebida — desactiva o timeout
                        skAtivo.setSoTimeout(0);

                        Element jogadaEl = (Element) doc.getElementsByTagName("jogar").item(0);
                        String  jogadaStr = jogadaEl.getAttribute("jogada");
                        String[] partes   = jogadaStr.trim().split("\\s+");

                        if (partes.length == 4) {
                            int[] coords = new int[4];
                            for (int i = 0; i < 4; i++) coords[i] = Integer.parseInt(partes[i]);
                            jogo.joga(coords, turno);
                        }

                        // Responde com o tabuleiro actualizado
                        String respostaJogar = "<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>";
                        registaLog("Cliente", respostaJogar);
                        osAtivo.println(respostaJogar);
                        jogadaFeita = jogo.getEstado();

                    } else {
                        System.out.println("Servidor dedicado: mensagem desconhecida ignorada: " + linha);
                    }
                }

                // -------------------------------------------------------
                // PASSO 2: Decidir com base no estado
                // -------------------------------------------------------
                if (jogo.terminou()) {
                    // Determinar vencedor para gravar no resultado
                    vencedor = jogadaFeita; // VX, VO ou EM

                    // Notificar o jogador passivo com o estado final.
                    // O passivo está bloqueado em obter() — já enviou o <obter> e aguarda resposta.
                    // Enviamos o tabuleiro final directamente sem esperar outra linha.
                    String respostaFinal = "<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>";
                    registaLog("Cliente", respostaFinal);
                    osPassivo.println(respostaFinal);
                    System.out.println("Jogo terminado! Estado final: " + vencedor);
                    break;
                }

                if ("IV".equals(jogadaFeita)) {
                    // Jogada inválida: mesmo jogador tenta outra vez
                    continue;
                }

                if ("BO".equals(jogadaFeita)) {
                    // Bónus: fechou caixa, joga outra vez
                    continue;
                }

                // Jogada normal: passa a vez
                turno = (turno == 'X') ? 'O' : 'X';

                // -------------------------------------------------------
                // PASSO 3: Notificar o jogador PASSIVO (estava em obter())
                // -------------------------------------------------------
                String linhaPassivo = isPassivo.readLine();
                if (linhaPassivo == null) throw new Exception("Ligação perdida (passivo)");
                char passivo = (turno == 'X') ? 'O' : 'X';
                registaLog("Servidor", linhaPassivo);
                String respostaPassivo = "<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>";
                registaLog("Cliente", respostaPassivo);
                osPassivo.println(respostaPassivo);
            }

        } catch (SocketTimeoutException e) {
            // Jogador não jogou nos 30 segundos
            System.out.println("Servidor dedicado: timeout! Jogador não jogou a tempo.");
            vencedor = "TO";
        } catch (Exception e) {
            System.out.println("Servidor dedicado: terminou o jogo (" + e.getMessage() + ")!");
            vencedor = "AB"; // ligação perdida = abandono
            // Tentar notificar o jogador que ainda está ligado com estado de abandono.
            // Isto permite que o browser do passivo receba "AB" e mostre mensagem adequada.
            try {
                String msgAbandono = "<metodo><obter><tabuleiro estado='AB'></tabuleiro></obter></metodo>";
                // Enviar para ambos (um deles pode já estar desligado — ignorar erro)
                try { new java.io.PrintWriter(connectionX.getOutputStream(), true).println(msgAbandono); } catch (Exception ex) {}
                try { new java.io.PrintWriter(connectionO.getOutputStream(), true).println(msgAbandono); } catch (Exception ex) {}
            } catch (Exception ex) { /* ignora */ }
        } finally {
            // Calcular duração em segundos
            long duracaoSeg = Instant.now().getEpochSecond() - inicio.getEpochSecond();

            // Gravar resultado no ficheiro XML
            gravarResultado(nomeX, nomeO, vencedor, duracaoSeg);

            try { if (connectionX != null) connectionX.close(); } catch (IOException e) {}
            try { if (connectionO != null) connectionO.close(); } catch (IOException e) {}
        }
        System.out.println("Servidor dedicado: terminou a Thread (" + this.getId() + ")!");
    }

    /**
     * Grava o resultado do jogo no ficheiro resultados.xml de forma segura,
     * criando um backup antes de escrever (Req. 5 — preservação em caso de falha).
     */
    private void gravarResultado(String jogX, String jogO, String result, long duracaoSeg) {
        try {
            String ficheiroRes = XMLDoc.getContexto() + "resultados.xml";
            String ficheiroXsd = XMLDoc.getContexto() + "resultados.xsd";

            // Ler o documento existente (ou criar um novo se não existir)
            Document doc;
            java.io.File f = new java.io.File(ficheiroRes);
            if (f.exists()) {
                doc = XMLDoc.parseFile(ficheiroRes);
            } else {
                doc = XMLDoc.parseString("<resultados></resultados>");
            }

            // Converter VX/VO para X/O para simplificar o XSD
            String vencedorSimples = result;
            if ("VX".equals(result)) vencedorSimples = "X";
            else if ("VO".equals(result)) vencedorSimples = "O";

            // Construir o novo elemento <jogo>
            Element raiz  = doc.getDocumentElement();
            Element jogo  = doc.createElement("jogo");

            Element eX    = doc.createElement("jogadorX");
            eX.setTextContent(jogX);
            Element eO    = doc.createElement("jogadorO");
            eO.setTextContent(jogO);
            Element eVenc = doc.createElement("vencedor");
            eVenc.setTextContent(vencedorSimples);
            Element eDur  = doc.createElement("duracaoSegundos");
            eDur.setTextContent(String.valueOf(duracaoSeg));
            Element eData = doc.createElement("data");
            eData.setTextContent(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .format(java.time.format.DateTimeFormatter.ISO_INSTANT));

            jogo.appendChild(eX);
            jogo.appendChild(eO);
            jogo.appendChild(eVenc);
            jogo.appendChild(eDur);
            jogo.appendChild(eData);
            raiz.appendChild(jogo);

            // Gravar com backup (Req. 5 — preservação em caso de falha)
            String backup = XMLDoc.gerarNomeFBackupVersao(ficheiroRes);
            XMLDoc.gravarLock(doc, ficheiroRes, backup);

            System.out.println("Resultado gravado: " + jogX + " vs " + jogO
                + " -> " + vencedorSimples + " (" + duracaoSeg + "s)");

        } catch (Exception e) {
            System.err.println("Erro ao gravar resultado: " + e.getMessage());
        }
    }
}