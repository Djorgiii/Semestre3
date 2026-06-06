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

@WebServlet("/ServletGame")
public class ServletGame extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Prefixo das chaves de sessão — cada jogo tem chave única baseada no adversário
    private static final String PREF_STUB     = "stub_";
    private static final String PREF_VEZ      = "vez_";
    private static final String PREF_OBTER    = "obterFeito_";
    private static final String PREF_TABULEIRO= "tabuleiro_";
    private static final String PREF_PENDENTE = "pendente_";

    private void log(HttpSession s, String adv, String msg) {
        String user = (String) s.getAttribute("username");
        Boolean vez = (Boolean) s.getAttribute(PREF_VEZ + adv);
        System.out.println("[GAME][" + user + " vs " + adv + "] vez=" + vez + " | " + msg);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String caminhoBase = getServletContext().getRealPath("/");
        if (caminhoBase != null && !caminhoBase.endsWith(java.io.File.separator))
            caminhoBase += java.io.File.separator;
        util.XMLDoc.setContextoReal(caminhoBase);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession sessao = request.getSession();
        String username = (String) sessao.getAttribute("username");
        String password = (String) sessao.getAttribute("password");

        if (username == null || password == null) {
            out.print("{\"erro\": \"Nao autenticado\"}");
            return;
        }

        // Identificador único desta partida — nome do adversário
        String adversario = request.getParameter("adversario");
        if (adversario == null || adversario.isBlank()) {
            out.print("{\"erro\": \"Adversario nao especificado\"}");
            return;
        }

        // Chaves de sessão específicas desta partida
        String keyStub      = PREF_STUB      + adversario;
        String keyVez       = PREF_VEZ       + adversario;
        String keyObter     = PREF_OBTER     + adversario;
        String keyTabuleiro = PREF_TABULEIRO + adversario;
        String keyPendente  = PREF_PENDENTE  + adversario;

        Stub meuStub = (Stub) sessao.getAttribute(keyStub);

        // ----------------------------------------------------------------
        // INICIALIZAÇÃO desta partida (se ainda não existe stub para este adversário)
        // ----------------------------------------------------------------
        if (meuStub == null) {
            try {
            	String servidorHost = getServletContext().getInitParameter("servidorHost");
            	String servidorPortoStr = getServletContext().getInitParameter("servidorPorto");
            	if (servidorHost == null || servidorHost.isBlank()) servidorHost = "localhost";
            	int servidorPorto = (servidorPortoStr != null) ? Integer.parseInt(servidorPortoStr) : 25565;
            	Socket socket = new Socket(servidorHost, servidorPorto);
            	meuStub = new Stub(socket);
                char meuSimbolo = meuStub.iniciar(username, password);
                sessao.setAttribute(keyStub,      meuStub);
                sessao.setAttribute(keyVez,        (meuSimbolo == 'X'));
                sessao.setAttribute(keyObter,      false);
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
            // HANDSHAKE INICIAL
            // ----------------------------------------------------------------
            Boolean primeiroObterFeito = (Boolean) sessao.getAttribute(keyObter);
            if (primeiroObterFeito != null && !primeiroObterFeito) {
                log(sessao, adversario, "HANDSHAKE - a fazer obter() inicial");
                Element tabuleiro = meuStub.obter();
                sessao.setAttribute(keyObter, true);
                boolean vezHandshake = (Boolean) sessao.getAttribute(keyVez);
                String json = gerarJson(tabuleiro, vezHandshake);
                sessao.setAttribute(keyTabuleiro, json);
                log(sessao, adversario, "HANDSHAKE concluido, estado=" + tabuleiro.getAttribute("estado"));
                out.print(json);
                return;
            }

            // ----------------------------------------------------------------
            // JOGAR
            // ----------------------------------------------------------------
            if ("jogar".equals(acao)) {
                boolean minhaVez = (Boolean) sessao.getAttribute(keyVez);
                if (!minhaVez) {
                    out.print("{\"erro\": \"Espera a tua vez!\"}");
                    return;
                }

                String linhaID = request.getParameter("linha");
                String[] partes = linhaID.split("-");
                String coord;
                if (partes[0].equals("h")) {
                    int lin = Integer.parseInt(partes[1]);
                    int col = Integer.parseInt(partes[2]);
                    coord = (col+1) + " " + (lin+1) + " " + (col+2) + " " + (lin+1);
                } else {
                    int col = Integer.parseInt(partes[1]);
                    int lin = Integer.parseInt(partes[2]);
                    coord = (col+1) + " " + (lin+1) + " " + (col+1) + " " + (lin+2);
                }

                log(sessao, adversario, "JOGAR coord=" + coord);
                Element tabuleiro = meuStub.jogar(coord);
                String estado = tabuleiro.getAttribute("estado");
                log(sessao, adversario, "JOGAR resposta estado=" + estado);

                if ("IV".equals(estado)) {
                    out.print(gerarJson(tabuleiro, true));
                } else if ("BO".equals(estado)) {
                    String json = gerarJson(tabuleiro, true);
                    sessao.setAttribute(keyTabuleiro, json);
                    out.print(json);
                } else {
                    sessao.setAttribute(keyVez, false);
                    sessao.setAttribute(keyPendente, false);
                    String json = gerarJson(tabuleiro, false);
                    sessao.setAttribute(keyTabuleiro, json);
                    log(sessao, adversario, "JOGAR vez passou para adversario");
                    out.print(json);
                }

            // ----------------------------------------------------------------
            // ESTADO (polling do jogador passivo)
            // ----------------------------------------------------------------
            } else if ("estado".equals(acao)) {
                boolean minhaVez  = (Boolean) sessao.getAttribute(keyVez);
                Boolean pendente  = (Boolean) sessao.getAttribute(keyPendente);

                if (minhaVez) {
                    // Já é a nossa vez — devolve cache
                    String ult = (String) sessao.getAttribute(keyTabuleiro);
                    out.print(ult != null ? ult
                        : "{\"estado\": \"ND\", \"linhas\": [], \"caixas\": [], \"minhaVez\": true}");
                    return;
                }

                if (Boolean.TRUE.equals(pendente)) {
                    // Já há um obter() em curso — devolve cache
                    String ult = (String) sessao.getAttribute(keyTabuleiro);
                    out.print(ult != null ? ult
                        : "{\"estado\": \"ND\", \"linhas\": [], \"caixas\": [], \"minhaVez\": false}");
                    return;
                }

                // Bloqueia até o adversário jogar
                sessao.setAttribute(keyPendente, true);
                log(sessao, adversario, "ESTADO a bloquear em obter() TCP...");
                Element tabuleiro = meuStub.obter();
                String json = gerarJson(tabuleiro, true);
                sessao.setAttribute(keyVez,       true);
                sessao.setAttribute(keyTabuleiro, json);
                sessao.setAttribute(keyPendente,  false);
                log(sessao, adversario, "ESTADO obter() respondeu! estado=" + tabuleiro.getAttribute("estado"));
                out.print(json);
            }

        } catch (Exception e) {
            System.err.println("[GAME][" + username + " vs " + adversario + "] ERRO: " + e.getMessage());
            e.printStackTrace();
            sessao.setAttribute(keyPendente, false);
            String msg = (e.getMessage() != null)
                ? e.getMessage().replace("\"", "'") : "Erro desconhecido";
            out.print("{\"erro\": \"" + msg + "\"}");
        }
        out.flush();
    }

    private String gerarJson(Element tab, Boolean minhaVez) {
        String estado = tab.getAttribute("estado");
        StringBuilder json = new StringBuilder();
        json.append("{\"estado\": \"").append(estado).append("\"");
        if (minhaVez != null)
            json.append(", \"minhaVez\": ").append(minhaVez);
        json.append(", \"linhas\": [");

        NodeList l = tab.getElementsByTagName("linha");
        for (int i = 0; i < l.getLength(); i++) {
            Element el = (Element) l.item(i);
            int x1 = Integer.parseInt(el.getAttribute("x1"));
            int y1 = Integer.parseInt(el.getAttribute("y1"));
            int x2 = Integer.parseInt(el.getAttribute("x2"));
            int y2 = Integer.parseInt(el.getAttribute("y2"));
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