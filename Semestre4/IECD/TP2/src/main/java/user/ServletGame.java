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

    private void log(HttpSession s, String msg) {
        String user = (String) s.getAttribute("username");
        Boolean vez = (Boolean) s.getAttribute("minhaVez");
        Boolean pendente = (Boolean) s.getAttribute("obterPendente");
        System.out.println("[GAME][" + user + "] minhaVez=" + vez + " obterPendente=" + pendente + " | " + msg);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String caminhoBase = getServletContext().getRealPath("/");
        if (caminhoBase != null && !caminhoBase.endsWith(java.io.File.separator)) caminhoBase += java.io.File.separator;
        util.XMLDoc.setContextoReal(caminhoBase);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession sessao = request.getSession();
        String username = (String) sessao.getAttribute("username");
        String password  = (String) sessao.getAttribute("password");

        if (username == null || password == null) {
            out.print("{\"erro\": \"Nao autenticado\"}"); return;
        }

        Stub meuStub = (Stub) sessao.getAttribute("stubPontos");

        if (meuStub == null) {
            try {
                Socket socket = new Socket("localhost", 25565);
                meuStub = new Stub(socket);
                char meuSimbolo = meuStub.iniciar(username, password);
                sessao.setAttribute("stubPontos", meuStub);
                sessao.setAttribute("minhaVez", (meuSimbolo == 'X'));
                sessao.setAttribute("primeiroObterFeito", false);
                sessao.setAttribute("ultimoTabuleiro", null);
                sessao.setAttribute("obterPendente", false);
                log(sessao, "INIT - simbolo=" + meuSimbolo);
            } catch (Exception e) {
                System.err.println("[GAME] Falha TCP: " + e.getMessage());
                out.print("{\"erro\": \"Falha de comunicacao\"}"); return;
            }
        }

        String acao = request.getParameter("acao");
        log(sessao, "PEDIDO acao=" + acao);

        try {
            Boolean primeiroObterFeito = (Boolean) sessao.getAttribute("primeiroObterFeito");
            if (primeiroObterFeito != null && !primeiroObterFeito) {
                log(sessao, "HANDSHAKE - a fazer obter() inicial");
                Element tabuleiro = meuStub.obter();
                sessao.setAttribute("primeiroObterFeito", true);
                boolean vezHandshake = (Boolean) sessao.getAttribute("minhaVez");
                String json = gerarJson(tabuleiro, vezHandshake);
                sessao.setAttribute("ultimoTabuleiro", json);
                log(sessao, "HANDSHAKE - concluido, estado=" + tabuleiro.getAttribute("estado") + " minhaVez=" + vezHandshake);
                out.print(json); return;
            }

            if ("jogar".equals(acao)) {
                boolean minhaVez = (Boolean) sessao.getAttribute("minhaVez");
                log(sessao, "JOGAR - minhaVez=" + minhaVez);

                if (!minhaVez) {
                    out.print("{\"erro\": \"Espera a tua vez!\"}"); return;
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

                log(sessao, "JOGAR - coord=" + coord);
                Element tabuleiro = meuStub.jogar(coord);
                String estado = tabuleiro.getAttribute("estado");
                log(sessao, "JOGAR - resposta estado=" + estado);

                if ("IV".equals(estado)) {
                    // Jogada inválida: ainda é a nossa vez
                    out.print(gerarJson(tabuleiro, true));
                } else if ("BO".equals(estado)) {
                    String jsonBo = gerarJson(tabuleiro, true);
                    sessao.setAttribute("ultimoTabuleiro", jsonBo);
                    out.print(jsonBo);
                } else {
                    sessao.setAttribute("minhaVez", false);
                    sessao.setAttribute("obterPendente", false);
                    String jsonNormal = gerarJson(tabuleiro, false);
                    sessao.setAttribute("ultimoTabuleiro", jsonNormal);
                    log(sessao, "JOGAR - vez passou para adversario");
                    out.print(jsonNormal);
                }

            } else if ("estado".equals(acao)) {
                boolean minhaVez = (Boolean) sessao.getAttribute("minhaVez");
                Boolean obterPendente = (Boolean) sessao.getAttribute("obterPendente");

                log(sessao, "ESTADO - minhaVez=" + minhaVez + " obterPendente=" + obterPendente);

                if (minhaVez) {
                    String ult = (String) sessao.getAttribute("ultimoTabuleiro");
                    log(sessao, "ESTADO - ja e a minha vez, devolvo cache");
                    out.print(ult != null ? ult : "{\"estado\": \"ND\", \"linhas\": [], \"caixas\": []}");
                    return;
                }

                if (Boolean.TRUE.equals(obterPendente)) {
                    String ult = (String) sessao.getAttribute("ultimoTabuleiro");
                    log(sessao, "ESTADO - obter ja pendente, devolvo cache");
                    out.print(ult != null ? ult : "{\"estado\": \"ND\", \"linhas\": [], \"caixas\": []}");
                    return;
                }

                sessao.setAttribute("obterPendente", true);
                log(sessao, "ESTADO - a bloquear em obter() TCP...");
                Element tabuleiro = meuStub.obter();
                String json = gerarJson(tabuleiro);
                String estado = tabuleiro.getAttribute("estado");
                String jsonEstado = gerarJson(tabuleiro, true);
                sessao.setAttribute("minhaVez", true);
                sessao.setAttribute("ultimoTabuleiro", jsonEstado);
                sessao.setAttribute("obterPendente", false);
                log(sessao, "ESTADO - obter() respondeu! estado=" + estado + " -> agora e a minha vez");
                out.print(jsonEstado);
            }

        } catch (Exception e) {
            System.err.println("[GAME][" + username + "] EXCECAO em acao=" + acao + ": " + e.getMessage());
            e.printStackTrace();
            sessao.setAttribute("obterPendente", false);
            String msg = (e.getMessage() != null) ? e.getMessage().replace("\"", "'") : "Erro desconhecido";
            out.print("{\"erro\": \"" + msg + "\"}");
        }
        out.flush();
    }

    private String gerarJson(Element tab) {
        return gerarJson(tab, null);
    }

    private String gerarJson(Element tab, Boolean minhaVez) {
        String estado = tab.getAttribute("estado");
        StringBuilder json = new StringBuilder();
        json.append("{\"estado\": \"").append(estado).append("\"");
        if (minhaVez != null) {
            json.append(", \"minhaVez\": ").append(minhaVez);
        }
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