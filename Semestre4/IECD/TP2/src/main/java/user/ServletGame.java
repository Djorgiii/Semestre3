package user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import client.Stub;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Servlet principal do jogo web — intermediário entre o browser e o servidor TCP.
 *
 * Cada pedido HTTP GET corresponde a uma acção de jogo:
 *   - Inicialização da ligação TCP (INIT)
 *   - Handshake inicial para receber o tabuleiro de partida
 *   - Jogar uma linha (acao=jogar&linha=...)
 *   - Polling do estado do tabuleiro (acao=estado)
 *
 * O estado de cada partida é guardado na sessão HTTP com chaves prefixadas
 * pelo nome do adversário, permitindo que o mesmo utilizador jogue
 * múltiplos jogos em simultâneo em abas diferentes.
 *
 * Responde sempre em JSON para ser consumido pelo JavaScript do jogo.jsp.
 */
@WebServlet("/ServletGame")
public class ServletGame extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Prefixos das chaves de sessão — sufixo é sempre o nome do adversário
    private static final String PREF_STUB      = "stub_";       // Stub TCP desta partida
    private static final String PREF_VEZ       = "vez_";        // booleano: é a minha vez?
    private static final String PREF_OBTER     = "obterFeito_"; // booleano: handshake concluído?
    private static final String PREF_TABULEIRO = "tabuleiro_";  // último JSON do tabuleiro
    private static final String PREF_PENDENTE  = "pendente_";   // há um obter() TCP em curso?

    /**
     * Utilitário de log para a consola do servidor, com contexto da partida.
     *
     * @param s   sessão HTTP do jogador
     * @param adv nome do adversário (identifica a partida)
     * @param msg mensagem a registar
     */
    private void log(HttpSession s, String adv, String msg) {
        String user = (String) s.getAttribute("username");
        Boolean vez = (Boolean) s.getAttribute(PREF_VEZ + adv);
        System.out.println("[GAME][" + user + " vs " + adv + "] vez=" + vez + " | " + msg);
    }

    /**
     * Ponto de entrada de todos os pedidos de jogo.
     *
     * Parâmetros HTTP esperados:
     *   adversario — nome do adversário (obrigatório)
     *   acao       — "jogar" ou "estado" (após handshake)
     *   linha      — identificador da linha clicada (apenas para acao=jogar)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Garantir que o XMLDoc aponta para o caminho real da webapp no Tomcat
        String caminhoBase = getServletContext().getRealPath("/");
        if (caminhoBase != null && !caminhoBase.endsWith(java.io.File.separator))
            caminhoBase += java.io.File.separator;
        util.XMLDoc.setContextoReal(caminhoBase);

        // Todas as respostas são JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession sessao   = request.getSession();
        String username      = (String) sessao.getAttribute("username");
        String password      = (String) sessao.getAttribute("password");

        // Rejeitar pedidos de utilizadores não autenticados
        if (username == null || password == null) {
            out.print("{\"erro\": \"Nao autenticado\"}");
            return;
        }

        // O adversário identifica unicamente a partida dentro da sessão
        String adversario = request.getParameter("adversario");
        if (adversario == null || adversario.isBlank()) {
            out.print("{\"erro\": \"Adversario nao especificado\"}");
            return;
        }

        // Chaves de sessão únicas para esta partida (adversário como sufixo)
        String keyStub      = PREF_STUB      + adversario;
        String keyVez       = PREF_VEZ       + adversario;
        String keyObter     = PREF_OBTER     + adversario;
        String keyTabuleiro = PREF_TABULEIRO + adversario;
        String keyPendente  = PREF_PENDENTE  + adversario;

        Stub meuStub = (Stub) sessao.getAttribute(keyStub);

        // ----------------------------------------------------------------
        // INIT — criar a ligação TCP ao servidor de jogo se ainda não existe
        // ----------------------------------------------------------------
        if (meuStub == null) {
            try {
                // Ler host e porto do servidor TCP a partir do web.xml
                String servidorHost = getServletContext().getInitParameter("servidorHost");
                String servidorPortoStr = getServletContext().getInitParameter("servidorPorto");
                if (servidorHost == null || servidorHost.isBlank()) servidorHost = "localhost";
                int servidorPorto = (servidorPortoStr != null) ? Integer.parseInt(servidorPortoStr) : 25565;

                // Estabelecer ligação TCP e autenticar com o nome do adversário
                Socket socket = new Socket(servidorHost, servidorPorto);
                meuStub = new Stub(socket, caminhoBase);
                char meuSimbolo = meuStub.iniciar(username, password, adversario);

                // Guardar o estado inicial da partida na sessão HTTP
                sessao.setAttribute(keyStub,      meuStub);
                sessao.setAttribute(keyVez,        (meuSimbolo == 'X')); // X começa sempre
                sessao.setAttribute(keyObter,      false);               // handshake pendente
                sessao.setAttribute(keyTabuleiro,  null);
                sessao.setAttribute(keyPendente,   false);
                log(sessao, adversario, "INIT - simbolo=" + meuSimbolo);

            } catch (Exception e) {
                System.err.println("[GAME] Falha TCP: " + e.getMessage());
                out.print("{\"erro\": \"Falha de comunicacao com o servidor de jogo\"}");
                return;
            }
        }

        String acao = request.getParameter("acao");
        log(sessao, adversario, "PEDIDO acao=" + acao);

        try {
            // ----------------------------------------------------------------
            // HANDSHAKE — primeiro obter() após a ligação, recebe o tabuleiro inicial
            // Necessário para sincronizar ambos os jogadores antes de começar.
            // ----------------------------------------------------------------
            Boolean primeiroObterFeito = (Boolean) sessao.getAttribute(keyObter);
            if (primeiroObterFeito != null && !primeiroObterFeito) {
                log(sessao, adversario, "HANDSHAKE - a fazer obter() inicial");
                Element tabuleiro = meuStub.obter(); // bloqueia até o servidor responder
                sessao.setAttribute(keyObter, true);
                Boolean vezHandshake = (Boolean) sessao.getAttribute(keyVez);
                // Salvaguarda: se os atributos foram limpos por erro concorrente, encerrar
                if (vezHandshake == null) {
                    out.print("{\"estado\": \"FIM\", \"linhas\": [], \"caixas\": []}");
                    return;
                }
                String json = gerarJson(tabuleiro, vezHandshake);
                sessao.setAttribute(keyTabuleiro, json);
                log(sessao, adversario, "HANDSHAKE concluido, estado=" + tabuleiro.getAttribute("estado"));
                out.print(json);
                return;
            }

            // ----------------------------------------------------------------
            // JOGAR — o jogador clicou numa linha
            // ----------------------------------------------------------------
            if ("jogar".equals(acao)) {
                Boolean minhaVez = (Boolean) sessao.getAttribute(keyVez);
                if (!Boolean.TRUE.equals(minhaVez)) {
                    out.print("{\"erro\": \"Espera a tua vez!\"}");
                    return;
                }

                // Converter o identificador visual da linha (ex: "h-0-1") para coordenadas TCP
                String linhaID = request.getParameter("linha");
                String[] partes = linhaID.split("-");
                String coord;
                if (partes[0].equals("h")) {
                    // Linha horizontal: h-lin-col → "col+1 lin+1 col+2 lin+1"
                    int lin = Integer.parseInt(partes[1]);
                    int col = Integer.parseInt(partes[2]);
                    coord = (col+1) + " " + (lin+1) + " " + (col+2) + " " + (lin+1);
                } else {
                    // Linha vertical: v-col-lin → "col+1 lin+1 col+1 lin+2"
                    int col = Integer.parseInt(partes[1]);
                    int lin = Integer.parseInt(partes[2]);
                    coord = (col+1) + " " + (lin+1) + " " + (col+1) + " " + (lin+2);
                }

                log(sessao, adversario, "JOGAR coord=" + coord);
                Element tabuleiro = meuStub.jogar(coord);
                String estado = tabuleiro.getAttribute("estado");
                log(sessao, adversario, "JOGAR resposta estado=" + estado);

                if ("IV".equals(estado)) {
                    // Jogada inválida — devolver o tabuleiro sem alterar o turno
                    out.print(gerarJson(tabuleiro, true));
                } else if ("BO".equals(estado)) {
                    // Bónus — o jogador fechou uma caixa e joga outra vez
                    String json = gerarJson(tabuleiro, true);
                    sessao.setAttribute(keyTabuleiro, json);
                    out.print(json);
                } else {
                    // Jogada normal — passar a vez ao adversário
                    sessao.setAttribute(keyVez, false);
                    sessao.setAttribute(keyPendente, false);
                    String json = gerarJson(tabuleiro, false);
                    sessao.setAttribute(keyTabuleiro, json);
                    log(sessao, adversario, "JOGAR vez passou para adversario");
                    out.print(json);
                }

            // ----------------------------------------------------------------
            // ESTADO — polling do jogador passivo enquanto aguarda a jogada do adversário
            // ----------------------------------------------------------------
            } else if ("estado".equals(acao)) {
                Boolean minhaVez = (Boolean) sessao.getAttribute(keyVez);
                Boolean pendente = (Boolean) sessao.getAttribute(keyPendente);

                // Salvaguarda: atributos podem ser null após limpeza por erro concorrente
                if (minhaVez == null) {
                    out.print("{\"estado\": \"FIM\", \"linhas\": [], \"caixas\": []}");
                    return;
                }

                if (Boolean.TRUE.equals(minhaVez)) {
                    // Já é a nossa vez — devolver o último tabuleiro em cache
                    String ult = (String) sessao.getAttribute(keyTabuleiro);
                    out.print(ult != null ? ult
                        : "{\"estado\": \"ND\", \"linhas\": [], \"caixas\": [], \"minhaVez\": true}");
                    return;
                }

                if (Boolean.TRUE.equals(pendente)) {
                    // Já há outro pedido HTTP bloqueado em obter() TCP — devolver cache
                    // (evita lançar múltiplos obter() em paralelo para o mesmo jogo)
                    String ult = (String) sessao.getAttribute(keyTabuleiro);
                    out.print(ult != null ? ult
                        : "{\"estado\": \"ND\", \"linhas\": [], \"caixas\": [], \"minhaVez\": false}");
                    return;
                }

                // Bloquear em obter() TCP até o adversário jogar
                sessao.setAttribute(keyPendente, true);
                log(sessao, adversario, "ESTADO a bloquear em obter() TCP...");
                Element tabuleiro = meuStub.obter(); // bloqueia aqui até resposta do servidor
                String json = gerarJson(tabuleiro, true);
                sessao.setAttribute(keyVez,       true);
                sessao.setAttribute(keyTabuleiro, json);
                sessao.setAttribute(keyPendente,  false);
                log(sessao, adversario, "ESTADO obter() respondeu! estado=" + tabuleiro.getAttribute("estado"));
                out.print(json);
            }

        } catch (Exception e) {
            System.err.println("[GAME][" + username + " vs " + adversario + "] ERRO: " + e.getMessage());

            // Suprimir stack trace para erros de ligação esperados (abandono, reset, etc.)
            String msgParaLog = e.getMessage() != null ? e.getMessage() : "";
            boolean erroEsperado = e instanceof java.net.SocketException
                || e instanceof java.io.IOException
                || msgParaLog.contains("cancelada") || msgParaLog.contains("anulada")
                || msgParaLog.contains("closed") || msgParaLog.contains("reset");
            if (!erroEsperado) e.printStackTrace();

            // Determinar se o erro significa perda de ligação TCP
            String msgErro = e.getMessage() != null ? e.getMessage() : "";
            boolean ligacaoPerdida = e instanceof java.net.SocketException
                || e instanceof java.io.IOException
                || (e.getCause() instanceof java.net.SocketException)
                || msgErro.contains("cancelada") || msgErro.contains("cancelled")
                || msgErro.contains("closed")    || msgErro.contains("reset")
                || msgErro.contains("anulada");

            if (ligacaoPerdida) {
                // Marcar o jogo como terminado na sessão para que pedidos paralelos
                // não tentem reutilizar o socket morto nem entrem em loop.
                // A guarda "== meuStub" evita apagar o estado de uma nova partida
                // que possa ter sido iniciada entretanto (condição de corrida).
                if (sessao.getAttribute(keyStub) == meuStub) {
                    sessao.setAttribute(keyObter,     true);
                    sessao.setAttribute(keyVez,       true);
                    sessao.setAttribute(keyPendente,  false);
                    sessao.setAttribute(keyTabuleiro, "{\"estado\": \"FIM\", \"linhas\": [], \"caixas\": []}");
                    sessao.removeAttribute(keyStub); // permite iniciar nova partida com o mesmo adversário
                }
                out.print("{\"estado\": \"FIM\", \"linhas\": [], \"caixas\": []}");
            } else {
                // Erro inesperado — limpar sessão e informar o browser
                if (sessao.getAttribute(keyStub) == meuStub) {
                    sessao.removeAttribute(keyStub);
                    sessao.removeAttribute(keyVez);
                    sessao.removeAttribute(keyObter);
                    sessao.removeAttribute(keyTabuleiro);
                    sessao.removeAttribute(keyPendente);
                }
                String msg = (e.getMessage() != null)
                    ? e.getMessage().replace("\"", "'") : "Erro desconhecido";
                out.print("{\"erro\": \"" + msg + "\"}");
            }
        }
        out.flush();
    }

    /**
     * Converte um elemento XML <tabuleiro> numa String JSON para o browser.
     *
     * Formato produzido:
     * {
     *   "estado": "ND",
     *   "minhaVez": true,
     *   "linhas": [{"id":"h-0-0"}, ...],
     *   "caixas": [{"dono":"X","x":1,"y":1}, ...]
     * }
     *
     * Os identificadores de linha seguem o formato "h-lin-col" (horizontal)
     * ou "v-col-lin" (vertical), consistentes com o HTML do jogo.jsp.
     *
     * @param tab      elemento XML do tabuleiro
     * @param minhaVez indica se é a vez do jogador que recebe este JSON
     * @return String JSON pronta a enviar ao browser
     */
    private String gerarJson(Element tab, Boolean minhaVez) {
        String estado = tab.getAttribute("estado");
        StringBuilder json = new StringBuilder();
        json.append("{\"estado\": \"").append(estado).append("\"");
        if (minhaVez != null)
            json.append(", \"minhaVez\": ").append(minhaVez);
        json.append(", \"linhas\": [");

        // Converter cada <linha x1 y1 x2 y2> no identificador visual correspondente
        NodeList l = tab.getElementsByTagName("linha");
        for (int i = 0; i < l.getLength(); i++) {
            Element el = (Element) l.item(i);
            int x1 = Integer.parseInt(el.getAttribute("x1"));
            int y1 = Integer.parseInt(el.getAttribute("y1"));
            int x2 = Integer.parseInt(el.getAttribute("x2"));
            int y2 = Integer.parseInt(el.getAttribute("y2"));
            // Linha horizontal: y1 == y2
            String id = (y1 == y2)
                ? "h-" + (y1-1) + "-" + (Math.min(x1,x2)-1)
                : "v-" + (x1-1) + "-" + (Math.min(y1,y2)-1);
            if (i > 0) json.append(",");
            json.append("{\"id\":\"").append(id).append("\"}");
        }

        json.append("], \"caixas\": [");
        NodeList c = tab.getElementsByTagName("caixa");
        for (int i = 0; i < c.getLength(); i++) {
            Element el = (Element) c.item(i);
            if (i > 0) json.append(",");
            json.append("{\"dono\":\"").append(el.getAttribute("dono")).append("\",")
                .append("\"x\":").append(el.getAttribute("x")).append(",")
                .append("\"y\":").append(el.getAttribute("y")).append("}");
        }

        json.append("]}");
        return json.toString();
    }
}