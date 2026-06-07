package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.MyImage;
import util.XMLDoc;

/**
 * Stub — lado cliente do padrão Stub/Skeleton.
 *
 * Encapsula toda a comunicação TCP com o servidor, expondo métodos de alto
 * nível (iniciar, obter, jogar, registar, alterar) que ocultam os detalhes
 * do protocolo XML/XSD.
 *
 * Utilizado tanto pelo cliente de consola (CLI) como pelo servlet web (Tomcat).
 * Para o Tomcat, o construtor com dois argumentos permite especificar o caminho
 * onde o log deve ser escrito, independentemente do contexto estático XMLDoc.
 *
 * Implementa AutoCloseable para ser usado em blocos try-with-resources.
 */
public class Stub implements AutoCloseable {

    /** Stream de leitura das respostas do servidor. */
    private BufferedReader is = null;
    /** Stream de escrita dos pedidos ao servidor. */
    private PrintWriter os = null;
    /** Último documento XML recebido do servidor (registo de autenticação). */
    private Document registo = null;
    /** Caminho do directório onde escrever o protocolo.log (null = usar XMLDoc). */
    private String caminhoLog = null;

    /**
     * Construtor para o cliente de consola (CLI).
     * Usa o caminho definido em XMLDoc.getContexto() para o log.
     *
     * @param sk socket TCP já ligado ao servidor
     */
    public Stub(Socket sk) throws IOException {
        is = new BufferedReader(new InputStreamReader(sk.getInputStream()));
        os = new PrintWriter(sk.getOutputStream(), true);
    }

    /**
     * Construtor para uso no Tomcat.
     * Recebe o caminho real da webapp para que o log seja escrito
     * correctamente independentemente do contexto estático XMLDoc.
     *
     * @param sk             socket TCP já ligado ao servidor
     * @param caminhoWebapp  caminho absoluto do directório raiz da webapp
     */
    public Stub(Socket sk, String caminhoWebapp) throws IOException {
        this(sk);
        this.caminhoLog = caminhoWebapp;
    }

    /**
     * Fecha os streams de comunicação.
     * Chamado automaticamente em blocos try-with-resources ou explicitamente
     * pelo abandonar.jsp quando o jogador abandona a partida.
     */
    @Override
    public void close() {
        try { is.close(); } catch (IOException e) {}
        os.close();
    }

    /**
     * Converte o elemento XML do tabuleiro numa representação textual
     * para visualização no cliente de consola.
     *
     * @param tabuleiro elemento XML <tabuleiro> recebido do servidor
     * @return String com o tabuleiro desenhado em ASCII
     */
    public String tabuleiroToTXT(final Element tabuleiro) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- TABULEIRO DE JOGO ---\n\n");

        NodeList linhas = tabuleiro.getElementsByTagName("linha");
        NodeList caixas = tabuleiro.getElementsByTagName("caixa");

        int maxX = 4;
        int maxY = 4;

        // Matrizes de presença de linhas e donos de caixas
        boolean[][] hLines = new boolean[20][20];
        boolean[][] vLines = new boolean[20][20];
        char[][] donoCaixa = new char[20][20];
        for(int i = 0; i < 20; i++) for(int j = 0; j < 20; j++) donoCaixa[i][j] = ' ';

        // Preencher a matriz de linhas a partir do XML
        for (int i = 0; i < linhas.getLength(); i++) {
            Element l = (Element) linhas.item(i);
            int x1 = Integer.parseInt(l.getAttribute("x1"));
            int y1 = Integer.parseInt(l.getAttribute("y1"));
            int x2 = Integer.parseInt(l.getAttribute("x2"));
            int y2 = Integer.parseInt(l.getAttribute("y2"));
            maxX = Math.max(maxX, Math.max(x1, x2));
            maxY = Math.max(maxY, Math.max(y1, y2));
            if (y1 == y2) hLines[y1][Math.min(x1, x2)] = true;
            else if (x1 == x2) vLines[Math.min(y1, y2)][x1] = true;
        }

        // Preencher os donos das caixas
        for (int i = 0; i < caixas.getLength(); i++) {
            Element c = (Element) caixas.item(i);
            int x = Integer.parseInt(c.getAttribute("x"));
            int y = Integer.parseInt(c.getAttribute("y"));
            donoCaixa[y][x] = c.getAttribute("dono").charAt(0);
        }

        // Desenhar o tabuleiro em ASCII
        sb.append("    ");
        for (int x = 1; x <= maxX; x++) sb.append(x).append("   ");
        sb.append("\n");
        for (int y = 1; y <= maxY; y++) {
            sb.append(String.format("%2d ", y));
            for (int x = 1; x <= maxX; x++) {
                sb.append("*");
                if (x < maxX) sb.append(hLines[y][x] ? "---" : "   ");
            }
            sb.append("\n");
            if (y < maxY) {
                sb.append("   ");
                for (int x = 1; x <= maxX; x++) {
                    sb.append(vLines[y][x] ? "|" : " ");
                    if (x < maxX) sb.append(" ").append(donoCaixa[y][x]).append(" ");
                }
                sb.append("\n");
            }
        }
        sb.append("\nCaixas fechadas: ").append(caixas.getLength()).append("\n");
        return sb.toString();
    }

    /**
     * Converte o código de estado do jogo numa mensagem legível para o CLI.
     *
     * @param valor código de estado (VA, VB, EM, IV, BO)
     * @return mensagem em português com emoji
     */
    public String estadoToTXT(final String valor) {
        switch (valor) {
        case "VA": return "🏆 Vitória do Jogador A!";
        case "VB": return "🏆 Vitória do Jogador B!";
        case "EM": return "🤝 Empate.";
        case "IV": return "❌ Jogada inválida! (Linha já existe ou pontos inválidos)";
        case "BO": return "🔥 BÓNUS! Fechaste uma caixa, joga outra vez!";
        default: return "";
        }
    }

    /**
     * Valida o documento XML recebido do servidor contra o schema XSD do cliente.
     * Lança uma excepção com mensagem limpa se o documento não for válido.
     *
     * @param doc documento XML a validar
     * @throws Exception se o documento violar o schema metodos-cli.xsd
     */
    private void validXSD(final Document doc) throws Exception {
        try {
            XMLDoc.validDocXSD(doc, XMLDoc.getContexto() + "metodos-cli.xsd");
        } catch (Exception e) {
            throw new Exception("XML Inválido do Servidor: " + e.getMessage());
        }
    }

    /**
     * Regista uma entrada no ficheiro protocolo.log para auditoria.
     * Suporta dois modos: caminho explícito (Tomcat) ou contexto estático (CLI).
     *
     * @param evento texto a registar (prefixo ENVIO/RECEP + conteúdo XML)
     */
    private void registaLog(String evento) {
        String caminho = (caminhoLog != null && !caminhoLog.isEmpty())
                ? caminhoLog
                : XMLDoc.getContexto();
        if (caminho == null || caminho.isEmpty()) return;
        try (PrintWriter escritor = new PrintWriter(new BufferedWriter(
                new FileWriter(caminho + "protocolo.log", true)))) {
            escritor.println(LocalDateTime.now() + " - " + evento.replaceAll("\n","").replaceAll("\r",""));
        } catch (IOException e) {
            System.err.println("[PROTOCOLO] " + LocalDateTime.now() + " - " + evento);
        }
    }

    /**
     * Imprime informações do utilizador autenticado e guarda a fotografia de perfil.
     * Usado pelo cliente de consola após autenticação bem-sucedida.
     */
    public void print() {
        if (registo == null) return;
        System.out.println("--- Autenticado com sucesso ---");
        try {
            String username  = registo.getElementsByTagName("username").item(0).getTextContent();
            String fotoBase64 = registo.getElementsByTagName("photography").item(0).getTextContent();
            MyImage img = new MyImage();
            img.setBase64(fotoBase64);
            String nomeFicheiro = "src/main/webapp" + java.io.File.separator + username + "_perfil.jpg";
            img.save(nomeFicheiro);
            System.out.println("📸 Fotografia descarregada: " + nomeFicheiro);
        } catch (Exception e) {
            System.out.println("⚠️ Utilizador sem fotografia de perfil.");
        }
    }

    /**
     * Overload de compatibilidade para o cliente de consola (CLI).
     * Chama iniciar com adversário vazio, que o servidor trata como "qualquer".
     *
     * @param user nome de utilizador
     * @param pass senha
     * @return símbolo atribuído ('X' ou 'O')
     */
    public char iniciar(final String user, final String pass) throws Exception {
        return iniciar(user, pass, "");
    }

    /**
     * Autentica o jogador no servidor e aguarda emparelhamento com o adversário.
     * Envia o pedido <iniciar> com o nome do adversário pretendido para que o
     * servidor possa emparelhar correctamente jogos simultâneos.
     *
     * @param user      nome de utilizador
     * @param pass      senha
     * @param adversario nome do adversário pretendido (vazio = qualquer)
     * @return símbolo atribuído pelo servidor ('X' ou 'O')
     * @throws Exception se a ligação falhar ou a autenticação for recusada
     */
    public char iniciar(final String user, final String pass, final String adversario) throws Exception {
        String pedido = "<metodo><iniciar nickname='" + user + "' senha='" + pass
                      + "' adversario='" + adversario + "'/></metodo>";
        registaLog("ENVIO{" + pedido + "}");
        os.println(pedido);
        String resposta = is.readLine();
        registaLog("RECEP{" + resposta + "}");
        if (resposta == null) throw new Exception("Ligação cancelada!");
        registo = XMLDoc.parseString(resposta);
        validXSD(registo);
        NodeList jogadores = registo.getElementsByTagName("jogador");
        return ((Element) jogadores.item(0)).getAttribute("simbolo").charAt(0);
    }

    /**
     * Solicita o estado actual do tabuleiro ao servidor.
     * Bloqueia até o servidor responder (o que pode implicar aguardar a jogada
     * do adversário quando chamado pelo jogador passivo).
     *
     * @return elemento XML <tabuleiro> com o estado actual do jogo
     * @throws Exception se a ligação for perdida ou a resposta for inválida
     */
    public Element obter() throws Exception {
        String pedido = "<metodo><obter/></metodo>";
        registaLog("ENVIO{" + pedido + "}");
        os.println(pedido);
        String resposta = is.readLine();
        registaLog("RECEP{" + resposta + "}");
        if (resposta == null) throw new Exception("Ligação cancelada!");
        Document d = XMLDoc.parseString(resposta);
        validXSD(d);
        return (Element) d.getElementsByTagName("tabuleiro").item(0);
    }

    /**
     * Envia uma jogada ao servidor e aguarda o tabuleiro actualizado.
     *
     * @param coordenadas coordenadas da linha no formato "x1 y1 x2 y2"
     * @return elemento XML <tabuleiro> com o estado após a jogada
     * @throws Exception se a ligação falhar ou a resposta for inválida
     */
    public Element jogar(final String coordenadas) throws Exception {
        String pedido = "<metodo><jogar jogada='" + coordenadas + "'/></metodo>";
        registaLog("ENVIO{" + pedido + "}");
        os.println(pedido);
        String resposta = is.readLine();
        registaLog("RECEP{" + resposta + "}");
        if (resposta == null) throw new Exception("Ligação cancelada!");
        Document d = XMLDoc.parseString(resposta);
        validXSD(d);
        return (Element) d.getElementsByTagName("tabuleiro").item(0);
    }

    /**
     * Envia um pedido de registo de nova conta ao servidor.
     *
     * @param user   nome de utilizador pretendido
     * @param pass   senha
     * @param first  primeiro(s) nome(s)
     * @param last   apelido(s)
     * @param gender género (M/F)
     * @param birth  data de nascimento (YYYY-MM-DD)
     * @param nac    código de nacionalidade
     * @param foto   fotografia em Base64
     */
    public void registar(String user, String pass, String first, String last,
                         String gender, String birth, String nac, String foto) throws Exception {
        String xml = "<metodo><registar nickname='" + user + "' senha='" + pass + "' "
                   + "firstnames='" + first + "' lastnames='" + last + "' "
                   + "gender='" + gender + "' birthdate='" + birth + "' "
                   + "nacionalidade='" + nac + "'>"
                   + "<photography>" + foto + "</photography>"
                   + "</registar></metodo>";
        os.println(xml);
    }

    /**
     * Envia um pedido de alteração de perfil ao servidor.
     * Permite alterar a senha e/ou a fotografia de perfil.
     *
     * @param username      nome de utilizador
     * @param novaPassword  nova senha
     * @param novaFotoBase64 nova fotografia em Base64 (pode ser vazia para manter a actual)
     */
    public void alterar(String username, String novaPassword, String novaFotoBase64) throws Exception {
        String xml = "<metodo><alterar nickname='" + username + "' senha='" + novaPassword + "'>"
                   + "<photography>" + novaFotoBase64 + "</photography>"
                   + "</alterar></metodo>";
        os.println(xml);
        registaLog("Cliente{" + xml + "}");
        String resposta = is.readLine();
        if (resposta != null) {
            registaLog("Cliente{" + resposta + "}");
            System.out.println("Servidor respondeu: " + resposta);
        }
    }
}