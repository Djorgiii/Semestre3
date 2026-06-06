<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, org.w3c.dom.*, javax.xml.parsers.*"%>
<%
    // Verificação de sessão
    if (session.getAttribute("username") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Classe auxiliar dentro do JSP
    class EstatJogador {
        String username;
        int vitorias    = 0;
        int jogos       = 0;
        long tempoTotal = 0;

        EstatJogador(String u) { this.username = u; }

        double tempoMedio() {
            return jogos == 0 ? 0 : (double) tempoTotal / jogos;
        }
    }

    Map<String, EstatJogador> mapa = new LinkedHashMap<>();
    String erro = null;

    try {
        String pathRes  = application.getRealPath("/resultados.xml");
        String pathUsers = application.getRealPath("/users.xml");

        File fRes = new File(pathRes);
        if (fRes.exists() && fRes.length() > 50) {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document docRes    = db.parse(fRes);
            NodeList jogos = docRes.getElementsByTagName("jogo");
            for (int i = 0; i < jogos.getLength(); i++) {
                Element jogo = (Element) jogos.item(i);
                String jogX    = jogo.getElementsByTagName("jogadorX").item(0).getTextContent();
                String jogO    = jogo.getElementsByTagName("jogadorO").item(0).getTextContent();
                String venc    = jogo.getElementsByTagName("vencedor").item(0).getTextContent();
                long duracao   = Long.parseLong(jogo.getElementsByTagName("duracaoSegundos").item(0).getTextContent());

                mapa.putIfAbsent(jogX, new EstatJogador(jogX));
                mapa.putIfAbsent(jogO, new EstatJogador(jogO));

                EstatJogador eX = mapa.get(jogX);
                EstatJogador eO = mapa.get(jogO);
                eX.jogos++;  eX.tempoTotal += duracao;
                eO.jogos++;  eO.tempoTotal += duracao;
                if ("X".equals(venc)) eX.vitorias++;
                else if ("O".equals(venc)) eO.vitorias++;
            }
        }

        List<EstatJogador> lista = new ArrayList<>(mapa.values());
        lista.sort((a, b) -> {
            if (b.vitorias != a.vitorias) return b.vitorias - a.vitorias;
            return Double.compare(a.tempoMedio(), b.tempoMedio());
        });

        Map<String, String> fotos      = new HashMap<>();
        Map<String, String> bandeiras  = new HashMap<>();
        Map<String, String> nomes      = new HashMap<>();

        File fUsers = new File(pathUsers);
        if (fUsers.exists()) {
            Document docUsers = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fUsers);
            NodeList users = docUsers.getElementsByTagName("user");
            for (int i = 0; i < users.getLength(); i++) {
                Element u = (Element) users.item(i);
                String uname = u.getElementsByTagName("username").item(0).getTextContent();
                NodeList fotoNL = u.getElementsByTagName("photography");
                if (fotoNL.getLength() > 0 && !fotoNL.item(0).getTextContent().isBlank())
                    fotos.put(uname, fotoNL.item(0).getTextContent().trim());
                NodeList natNL = u.getElementsByTagName("nationality");
                if (natNL.getLength() > 0)
                    bandeiras.put(uname, natNL.item(0).getTextContent().trim());
                String first = u.getElementsByTagName("firstnames").item(0).getTextContent();
                String last  = u.getElementsByTagName("lastnames").item(0).getTextContent();
                nomes.put(uname, first + " " + last);
            }
        }

        request.setAttribute("lista", lista);
        request.setAttribute("fotos", fotos);
        request.setAttribute("bandeiras", bandeiras);
        request.setAttribute("nomes", nomes);
    } catch (Exception e) {
        erro = "Erro ao carregar o quadro de honra: " + e.getMessage();
    }

    List<EstatJogador> lista = (List<EstatJogador>) request.getAttribute("lista");
    Map<String, String> fotos = (Map<String, String>) request.getAttribute("fotos");
    Map<String, String> bandeiras = (Map<String, String>) request.getAttribute("bandeiras");
    Map<String, String> nomes = (Map<String, String>) request.getAttribute("nomes");
%>
<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <title>Quadro de Honra - Pontos e Caixas</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f7fafc; padding: 40px; color: #2d3748; display: flex; flex-direction: column; align-items: center; }
        h1 { color: #2d3748; margin-bottom: 30px; }
        .card { background: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); width: 100%; max-width: 860px; overflow: hidden; }
        table { width: 100%; border-collapse: collapse; }
        th { background: #319795; color: white; padding: 14px 16px; text-align: left; }
        td { padding: 14px 16px; border-bottom: 1px solid #edf2f7; }
        .pos { font-weight: bold; width: 40px; text-align: center; }
        .foto { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; }
        .btn-back { margin-top: 24px; padding: 10px 22px; background: #319795; color: white; border-radius: 8px; text-decoration: none; }
        .erro { color: #e53e3e; background: #fff5f5; padding: 16px; border-radius: 8px; }
    </style>
</head>
<body>
    <h1>🏆 Quadro de Honra</h1>
    <% if (erro != null) { %>
        <div class="erro"><%= erro %></div>
    <% } else { %>
    <div class="card">
        <table>
            <thead>
                <tr><th>#</th><th>Foto</th><th>Jogador</th><th>País</th><th>Vitórias</th><th>Jogos</th><th>Tempo Médio</th></tr>
            </thead>
            <tbody>
            <% if (lista == null || lista.isEmpty()) { %>
                <tr><td colspan="7" style="text-align:center;">Ainda não há jogos.</td></tr>
            <% } else {
                int pos = 0;
                for (Object item : lista) {
                    EstatJogador player = (EstatJogador) item; // Acesso direto e seguro
                    pos++;
                    String foto = fotos != null ? fotos.get(player.username) : null;
                    String nat = bandeiras != null ? bandeiras.get(player.username) : null;
                    String nomeComp = nomes != null ? nomes.get(player.username) : player.username;
            %>
                <tr>
                    <td class="pos"><%= pos %></td>
                    <td>
                        <% if (foto != null) { %><img class="foto" src="data:image/jpeg;base64,<%= foto %>"><% } else { %>👤<% } %>
                    </td>
                    <td><strong><%= player.username %></strong><br><%= nomeComp %></td>
                    <td><%= (nat != null) ? nat : "—" %></td>
                    <td><%= player.vitorias %></td>
                    <td><%= player.jogos %></td>
                    <td><%= String.format("%dm %02ds", (int)player.tempoMedio()/60, (int)player.tempoMedio()%60) %></td>
                </tr>
            <% } } %>
            </tbody>
        </table>
    </div>
    <% } %>
    <a href="lobby.jsp" class="btn-back">← Voltar ao Lobby</a>
</body>
</html>