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

/**
 * Servidor Dedicado — gere uma partida completa entre dois jogadores.
 *
 * Cada instância corre numa Thread independente, o que permite que o
 * servidor suporte múltiplos jogos em simultâneo sem bloqueios.
 *
 * Protocolo de comunicação:
 *   - Jogador activo envia <obter/> para receber o tabuleiro, depois <jogar jogada='...'/>.
 *   - Jogador passivo aguarda bloqueado em <obter/> até o adversário jogar.
 *   - No final do jogo, o resultado é gravado em resultados.xml com backup.
 */
@SuppressWarnings("resource")
class ServidorDedicado extends Thread {

    /** Tempo máximo (em milissegundos) que cada jogador tem para efectuar uma jogada. */
    private static final int TIMEOUT_JOGADA_MS = 30_000;

    /**
     * Regista uma mensagem no ficheiro protocolo.log para auditoria.
     * O formato é: timestamp - prefixo{mensagem}
     * O método é sincronizado para evitar escritas sobrepostas de Threads concorrentes.
     *
     * @param prefixo   "Servidor" ou "Cliente" consoante a direcção da mensagem
     * @param mensagem  conteúdo XML da mensagem
     */
    private static synchronized void registaLog(String prefixo, String mensagem) {
        // Remove quebras de linha para manter uma entrada por linha no log
        String linha = mensagem == null ? "null" : mensagem.replaceAll("[\r\n]", "");
        String entrada = LocalDateTime.now() + " - " + prefixo + "{" + linha + "}";
        try (PrintWriter escritor = new PrintWriter(new BufferedWriter(
                new FileWriter(XMLDoc.getContexto() + "protocolo.log", true)))) {
            escritor.println(entrada);
        } catch (IOException e) {
            System.err.println("[LOG] Nao foi possivel escrever no protocolo.log: " + e.getMessage());
        }
    }

    /** Socket do jogador X (primeiro a ligar-se ao par). */
    private Socket connectionX = null;
    /** Socket do jogador O (segundo a ligar-se ao par). */
    private Socket connectionO = null;
    /** Nome de utilizador do jogador X. */
    private String nomeX = null;
    /** Nome de utilizador do jogador O. */
    private String nomeO = null;

    /**
     * Constrói um servidor dedicado para uma partida entre dois jogadores já autenticados.
     *
     * @param connection1 socket do jogador X
     * @param connection2 socket do jogador O
     * @param nomeX       nome de utilizador do jogador X
     * @param nomeO       nome de utilizador do jogador O
     */
    public ServidorDedicado(Socket connection1, Socket connection2,
                            String nomeX, String nomeO) {
        this.connectionX = connection1;
        this.connectionO = connection2;
        this.nomeX = nomeX;
        this.nomeO = nomeO;
    }

    /**
     * Ciclo principal do jogo. Corre na Thread dedicada a esta partida.
     *
     * Fluxo por turno:
     *   1. O jogador activo envia <obter/> (recebe o tabuleiro) e depois <jogar/>.
     *   2. Após a jogada, verifica o estado: fim, bónus, inválida ou normal.
     *   3. Notifica o jogador passivo com o tabuleiro actualizado.
     *   4. Troca de turno e repete.
     */
    public void run() {
        Instant inicio = Instant.now(); // instante de início para calcular duração
        String vencedor = "AB";         // valor por omissão: abandono (sobrescrito no fim normal)

        try (
            // Abrir streams de leitura e escrita para cada jogador
            BufferedReader isX = new BufferedReader(new InputStreamReader(connectionX.getInputStream()));
            PrintWriter    osX = new PrintWriter(connectionX.getOutputStream(), true);
            BufferedReader isO = new BufferedReader(new InputStreamReader(connectionO.getInputStream()));
            PrintWriter    osO = new PrintWriter(connectionO.getOutputStream(), true);
        ) {
            System.out.println("Iniciou a Thread (" + this.threadId() + ") do servidor dedicado! "
                + nomeX + "(X) vs " + nomeO + "(O)");

            JogoXML jogo = new JogoXML(); // estado do tabuleiro
            char turno = 'X';             // X começa sempre

            for (;;) {
                // Seleccionar os streams do jogador activo e passivo com base no turno actual
                BufferedReader isAtivo   = (turno == 'X') ? isX : isO;
                PrintWriter    osAtivo   = (turno == 'X') ? osX : osO;
                BufferedReader isPassivo = (turno == 'X') ? isO : isX;
                PrintWriter    osPassivo = (turno == 'X') ? osO : osX;
                Socket         skAtivo   = (turno == 'X') ? connectionX : connectionO;

                // -------------------------------------------------------
                // PASSO 1: Aguardar a jogada do jogador activo.
                // O jogador pode enviar vários <obter/> antes de jogar.
                // O timeout de 30s é activado após o primeiro <obter/>.
                // -------------------------------------------------------
                String  jogadaFeita   = null;
                boolean timerIniciado = false;

                while (jogadaFeita == null) {
                    String linha = isAtivo.readLine();
                    if (linha == null) throw new Exception("Ligação perdida (" + turno + ")");
                    registaLog("Servidor", linha);

                    Document doc = XMLDoc.parseString(linha);

                    if (doc.getElementsByTagName("obter").getLength() > 0) {
                        // Pedido de tabuleiro — envia o estado actual
                        String respostaObter = "<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>";
                        registaLog("Cliente", respostaObter);
                        osAtivo.println(respostaObter);

                        // Activa o timeout de jogada após o jogador receber o tabuleiro pela 1ª vez
                        if (!timerIniciado) {
                            skAtivo.setSoTimeout(TIMEOUT_JOGADA_MS);
                            timerIniciado = true;
                        }

                    } else if (doc.getElementsByTagName("jogar").getLength() > 0) {
                        // Jogada recebida — desactiva o timeout para não expirar durante o processamento
                        skAtivo.setSoTimeout(0);

                        // Extrair as coordenadas da jogada do atributo XML
                        Element jogadaEl  = (Element) doc.getElementsByTagName("jogar").item(0);
                        String  jogadaStr = jogadaEl.getAttribute("jogada");
                        String[] partes   = jogadaStr.trim().split("\\s+");

                        if (partes.length == 4) {
                            int[] coords = new int[4];
                            for (int i = 0; i < 4; i++) coords[i] = Integer.parseInt(partes[i]);
                            jogo.joga(coords, turno);
                        }

                        // Responde ao jogador activo com o tabuleiro actualizado
                        String respostaJogar = "<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>";
                        registaLog("Cliente", respostaJogar);
                        osAtivo.println(respostaJogar);
                        jogadaFeita = jogo.getEstado();

                    } else {
                        System.out.println("Servidor dedicado: mensagem desconhecida ignorada: " + linha);
                    }
                }

                // -------------------------------------------------------
                // PASSO 2: Analisar o resultado da jogada.
                // -------------------------------------------------------
                if (jogo.terminou()) {
                    // Jogo terminado (vitória ou empate) — registar o vencedor
                    vencedor = jogadaFeita; // VX, VO ou EM

                    // O jogador passivo está bloqueado em <obter/> à espera da sua vez.
                    // Enviamos o tabuleiro final directamente sem esperar novo pedido.
                    String respostaFinal = "<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>";
                    registaLog("Cliente", respostaFinal);
                    osPassivo.println(respostaFinal);
                    System.out.println("Jogo terminado! Estado final: " + vencedor);
                    break;
                }

                if ("IV".equals(jogadaFeita)) {
                    // Jogada inválida — o mesmo jogador tenta outra vez (não troca turno)
                    continue;
                }

                if ("BO".equals(jogadaFeita)) {
                    // Bónus: o jogador fechou uma caixa e joga novamente
                    continue;
                }

                // Jogada normal válida — passa a vez ao adversário
                turno = (turno == 'X') ? 'O' : 'X';

                // -------------------------------------------------------
                // PASSO 3: Notificar o jogador passivo.
                // O passivo já enviou <obter/> e está bloqueado à espera.
                // -------------------------------------------------------
                String linhaPassivo = isPassivo.readLine();
                if (linhaPassivo == null) throw new Exception("Ligação perdida (passivo)");
                registaLog("Servidor", linhaPassivo);
                String respostaPassivo = "<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>";
                registaLog("Cliente", respostaPassivo);
                osPassivo.println(respostaPassivo);
            }

        } catch (SocketTimeoutException e) {
            // O jogador activo não jogou dentro dos 30 segundos permitidos
            System.out.println("Servidor dedicado: timeout! Jogador não jogou a tempo.");
            vencedor = "TO";

        } catch (Exception e) {
            // Erro de ligação — tipicamente abandono de um dos jogadores
            System.out.println("Servidor dedicado: terminou o jogo (" + e.getMessage() + ")!");
            vencedor = "AB";

            // Tentar notificar ambos os jogadores com o estado de abandono.
            // Um deles pode já estar desligado — os erros são ignorados.
            try {
                String msgAbandono = "<metodo><obter><tabuleiro estado='AB'></tabuleiro></obter></metodo>";
                try { new java.io.PrintWriter(connectionX.getOutputStream(), true).println(msgAbandono); } catch (Exception ex) {}
                try { new java.io.PrintWriter(connectionO.getOutputStream(), true).println(msgAbandono); } catch (Exception ex) {}
            } catch (Exception ex) { /* ignora */ }

        } finally {
            // Calcular a duração total do jogo
            long duracaoSeg = Instant.now().getEpochSecond() - inicio.getEpochSecond();

            // Gravar o resultado no ficheiro XML (sempre, independentemente do motivo de fim)
            gravarResultado(nomeX, nomeO, vencedor, duracaoSeg);

            // Fechar as ligações TCP de ambos os jogadores
            try { if (connectionX != null) connectionX.close(); } catch (IOException e) {}
            try { if (connectionO != null) connectionO.close(); } catch (IOException e) {}
        }
        System.out.println("Servidor dedicado: terminou a Thread (" + this.threadId() + ")!");
    }

    /**
     * Grava o resultado do jogo no ficheiro resultados.xml.
     *
     * Para garantir a preservação dos dados em caso de falha (Req. 5),
     * é criado um backup antes de qualquer escrita. Se a escrita falhar,
     * o backup permite recuperar o estado anterior.
     *
     * @param jogX       nome do jogador X
     * @param jogO       nome do jogador O
     * @param result     resultado: VX, VO, EM, TO ou AB
     * @param duracaoSeg duração da partida em segundos
     */
    private void gravarResultado(String jogX, String jogO, String result, long duracaoSeg) {
        try {
            String ficheiroRes = XMLDoc.getContexto() + "resultados.xml";

            // Ler o documento existente ou criar um novo se o ficheiro ainda não existir
            Document doc;
            java.io.File f = new java.io.File(ficheiroRes);
            if (f.exists()) {
                doc = XMLDoc.parseFile(ficheiroRes);
            } else {
                doc = XMLDoc.parseString("<resultados></resultados>");
            }

            // Simplificar o vencedor: VX→X, VO→O (o XSD usa apenas X, O, EM, TO, AB)
            String vencedorSimples = result;
            if ("VX".equals(result)) vencedorSimples = "X";
            else if ("VO".equals(result)) vencedorSimples = "O";

            // Construir o novo elemento <jogo> com todos os dados da partida
            Element raiz  = doc.getDocumentElement();
            Element jogo  = doc.createElement("jogo");

            Element eX    = doc.createElement("jogadorX");      eX.setTextContent(jogX);
            Element eO    = doc.createElement("jogadorO");      eO.setTextContent(jogO);
            Element eVenc = doc.createElement("vencedor");      eVenc.setTextContent(vencedorSimples);
            Element eDur  = doc.createElement("duracaoSegundos"); eDur.setTextContent(String.valueOf(duracaoSeg));
            Element eData = doc.createElement("data");
            eData.setTextContent(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .format(java.time.format.DateTimeFormatter.ISO_INSTANT));

            jogo.appendChild(eX);
            jogo.appendChild(eO);
            jogo.appendChild(eVenc);
            jogo.appendChild(eDur);
            jogo.appendChild(eData);
            raiz.appendChild(jogo);

            // Gravar com backup automático (Req. 5 — preservação em caso de falha)
            String backup = XMLDoc.gerarNomeFBackupVersao(ficheiroRes);
            XMLDoc.gravarLock(doc, ficheiroRes, backup);

            System.out.println("Resultado gravado: " + jogX + " vs " + jogO
                + " -> " + vencedorSimples + " (" + duracaoSeg + "s)");

        } catch (Exception e) {
            System.err.println("Erro ao gravar resultado: " + e.getMessage());
        }
    }
}