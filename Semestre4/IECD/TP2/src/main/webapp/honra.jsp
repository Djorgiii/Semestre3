<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, org.w3c.dom.*, javax.xml.parsers.*"%>
<%
    /*
     * honra.jsp — quadro de honra (hall of fame).
     *
     * Lê o ficheiro resultados.xml e agrega as estatísticas de cada jogador:
     *   - número total de jogos (incluindo AB e TO)
     *   - número de vitórias
     *   - tempo médio por jogo
     *
     * Ordenação: 1º por número de vitórias (descendente);
     *            em caso de empate, por menor tempo médio (ascendente).
     *
     * Para cada jogador, lê também a fotografia e a bandeira da nacionalidade
     * a partir do users.xml e nationalities.xml para enriquecer a tabela.
     *
     * Req. 4 — Quadro de honra com fotografias e bandeiras.
     */

    // Verificação de sessão — redirecionar para login se não autenticado
    if (session.getAttribute("username") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // --- Estrutura auxiliar para agregar estatísticas por jogador ---
    class EstatJogador {
        String username;
        int vitorias    = 0;
        int jogos       = 0;
        long tempoTotal = 0; // segundos totais em jogos

        EstatJogador(String u) { this.username = u; }

        double tempoMedio() {
            return jogos == 0 ? 0 : (double) tempoTotal / jogos;
        }
    }

    Map<String, EstatJogador> mapa = new LinkedHashMap<>();
    String erro = null;

    try {
        // Ler resultados.xml
        String pathRes  = application.getRealPath("/resultados.xml");
        String pathUsers = application.getRealPath("/users.xml");

        File fRes = new File(pathRes);
        if (fRes.exists() && fRes.length() > 50) {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document docRes    = db.parse(fRes);

            NodeList jogos = docRes.getElementsByTagName("jogo");
            for (int i = 0; i < jogos.getLength(); i++) {
                Element jogo = (Element) jogos.item(i);

                // Extrair dados de cada jogo registado
                String jogX    = jogo.getElementsByTagName("jogadorX").item(0).getTextContent();
                String jogO    = jogo.getElementsByTagName("jogadorO").item(0).getTextContent();
                String venc    = jogo.getElementsByTagName("vencedor").item(0).getTextContent();
                long duracao   = Long.parseLong(
                    jogo.getElementsByTagName("duracaoSegundos").item(0).getTextContent());

                // Inicializar entradas no mapa se for a primeira vez que este jogador aparece
                mapa.putIfAbsent(jogX, new EstatJogador(jogX));
                mapa.putIfAbsent(jogO, new EstatJogador(jogO));

                EstatJogador eX = mapa.get(jogX);
                EstatJogador eO = mapa.get(jogO);

                // Todos os jogos contam (AB e TO inclusive) para o total e tempo médio
                eX.jogos++;  eX.tempoTotal += duracao;
                eO.jogos++;  eO.tempoTotal += duracao;

                // Atribuir vitória ao vencedor (X ou O); EM, TO, AB não atribuem vitória
                if ("X".equals(venc))       eX.vitorias++;
                else if ("O".equals(venc))  eO.vitorias++;
                // EM, TO, AB: ninguém ganha
            }
        }

        // Ordenar: 1º mais vitórias, 2º menor tempo médio (em caso de empate)
        List<EstatJogador> lista = new ArrayList<>(mapa.values());
        lista.sort((a, b) -> {
            if (b.vitorias != a.vitorias) return b.vitorias - a.vitorias;
            return Double.compare(a.tempoMedio(), b.tempoMedio());
        });

        // Ler fotos e bandeiras do users.xml
        Map<String, String> fotos      = new HashMap<>();
        Map<String, String> bandeiras  = new HashMap<>();
        Map<String, String> nomes      = new HashMap<>();

        File fUsers = new File(pathUsers);
        if (fUsers.exists()) {
            Document docUsers = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(fUsers);
            NodeList users = docUsers.getElementsByTagName("user");

            for (int i = 0; i < users.getLength(); i++) {
                Element u    = (Element) users.item(i);
                String uname = u.getElementsByTagName("username").item(0).getTextContent();

                // Foto (pode não existir)
                NodeList fotoNL = u.getElementsByTagName("photography");
                if (fotoNL.getLength() > 0 && !fotoNL.item(0).getTextContent().isBlank())
                    fotos.put(uname, fotoNL.item(0).getTextContent().trim());

                // Nacionalidade para a bandeira
                NodeList natNL = u.getElementsByTagName("nationality");
                if (natNL.getLength() > 0)
                    bandeiras.put(uname, natNL.item(0).getTextContent().trim());

                // Nome completo
                String first = u.getElementsByTagName("firstnames").item(0).getTextContent();
                String last  = u.getElementsByTagName("lastnames").item(0).getTextContent();
                nomes.put(uname, first + " " + last);
            }
        }

        // Guardar na request para usar no HTML abaixo
        request.setAttribute("lista",     lista);
        request.setAttribute("fotos",     fotos);
        request.setAttribute("bandeiras", bandeiras);
        request.setAttribute("nomes",     nomes);

    } catch (Exception e) {
        erro = "Erro ao carregar o quadro de honra: " + e.getMessage();
    }

    List<EstatJogador>    lista    = (List<EstatJogador>)    request.getAttribute("lista");
    Map<String, String>   fotos    = (Map<String, String>)   request.getAttribute("fotos");
    Map<String, String>   bandeiras= (Map<String, String>)   request.getAttribute("bandeiras");
    Map<String, String>   nomes    = (Map<String, String>)   request.getAttribute("nomes");
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
        th { background: #319795; color: white; padding: 14px 16px; text-align: left; font-size: 0.9em; }
        td { padding: 14px 16px; border-bottom: 1px solid #edf2f7; vertical-align: middle; }
        tr:last-child td { border-bottom: none; }
        tr:hover td { background: #f0fff4; }
        .pos { font-weight: bold; font-size: 1.1em; width: 40px; text-align: center; }
        .pos-1 { color: #d69e2e; }
        .pos-2 { color: #718096; }
        .pos-3 { color: #c05621; }
        .foto { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; border: 2px solid #e2e8f0; }
        .foto-default { width: 44px; height: 44px; border-radius: 50%; background: #e2e8f0; display: flex; align-items: center; justify-content: center; font-size: 1.2em; color: #718096; }
        .bandeira { width: 28px; height: 20px; object-fit: cover; border-radius: 3px; border: 1px solid #e2e8f0; }
        .vitorias { font-weight: bold; font-size: 1.15em; color: #319795; }
        .btn-back { margin-top: 24px; padding: 10px 22px; background: #319795; color: white; border-radius: 8px; text-decoration: none; font-weight: bold; }
        .erro { color: #e53e3e; background: #fff5f5; padding: 16px; border-radius: 8px; border-left: 4px solid #e53e3e; }
        .vazio { text-align: center; color: #a0aec0; padding: 40px; }
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
                <tr>
                    <th>#</th>
                    <th>Foto</th>
                    <th>Jogador</th>
                    <th>País</th>
                    <th>Vitórias</th>
                    <th>Jogos</th>
                    <th>Tempo Médio</th>
                </tr>
            </thead>
            <tbody>
            <% if (lista == null || lista.isEmpty()) { %>
                <tr><td colspan="7" class="vazio">Ainda não há jogos registados.</td></tr>
            <% } else {
                int pos = 0;
                for (Object obj : lista) {
                    // Cast dinâmico via reflexão para evitar problemas com classes locais
                    String username  = (String) obj.getClass().getField("username").get(obj);
                    int vitorias     = (int)    obj.getClass().getField("vitorias").get(obj);
                    int jogosTotal   = (int)    obj.getClass().getField("jogos").get(obj);
                    double tMedio    = (double) obj.getClass().getMethod("tempoMedio").invoke(obj);
                    pos++;

                    String foto      = fotos     != null ? fotos.get(username)     : null;
                    String natCode   = bandeiras != null ? bandeiras.get(username) : null;
                    String nomeComp  = nomes     != null ? nomes.get(username)     : username;
                    String posClass  = pos == 1 ? "pos-1" : (pos == 2 ? "pos-2" : (pos == 3 ? "pos-3" : ""));
                    int minutos      = (int) tMedio / 60;
                    int segundos     = (int) tMedio % 60;
            %>
                <tr>
                    <td class="pos <%= posClass %>">
                        <%= pos == 1 ? "🥇" : (pos == 2 ? "🥈" : (pos == 3 ? "🥉" : pos)) %>
                    </td>
                    <td>
                        <% if (foto != null && !foto.isBlank()) { %>
                            <img class="foto" src="data:image/jpeg;base64,<%= foto %>" alt="<%= username %>">
                        <% } else { %>
                            <div class="foto-default">👤</div>
                        <% } %>
                    </td>
                    <td>
                        <strong><%= username %></strong><br>
                        <span style="font-size:0.85em; color:#718096;"><%= nomeComp %></span>
                    </td>
                    <td>
                        <% if (natCode != null && !natCode.isBlank()) { %>
                            <img class="bandeira"
                                 src="https://flagcdn.com/w40/<%= natCode.toLowerCase() %>.png"
                                 alt="<%= natCode %>"
                                 title="<%= natCode %>">
                        <% } else { %>
                            —
                        <% } %>
                    </td>
                    <td class="vitorias"><%= vitorias %></td>
                    <td><%= jogosTotal %></td>
                    <td><%= String.format("%dm %02ds", minutos, segundos) %></td>
                </tr>
            <% } } %>
            </tbody>
        </table>
    </div>
    <% } %>

    <a href="lobby.jsp" class="btn-back">← Voltar ao Lobby</a>
</body>
</html>
