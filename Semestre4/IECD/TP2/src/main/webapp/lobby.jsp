<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, java.util.*, org.w3c.dom.*, javax.xml.parsers.*"%>
<%
    // 1. VERIFICAÇÃO DE SEGURANÇA (O Cão de Guarda da Sessão)
    // Se alguém tentar aceder ao lobby sem fazer login, é expulso para o login!
    String meuUsername = (String) session.getAttribute("username");
    String minhaCor = (String) session.getAttribute("corFundo");
    
    String corDestaque = (minhaCor != null && !minhaCor.isBlank()) ? minhaCor : "#319795";
    if (meuUsername == null) {
        response.sendRedirect("login.jsp?msgErro=" + java.net.URLEncoder.encode("Acesso negado! Inicia sessão primeiro.", "UTF-8"));
        return; // Pára a execução da página aqui
    }

    // 2. LER OS ADVERSÁRIOS DO XML
    List<Element> adversarios = new ArrayList<>();
    try {
        String xmlPath = application.getRealPath("/users.xml");
        File xmlFile = new File(xmlPath);
        if (xmlFile.exists()) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
            NodeList nl = doc.getElementsByTagName("user");
            for (int i = 0; i < nl.getLength(); i++) {
                Element u = (Element) nl.item(i);
                String oppName = u.getElementsByTagName("username").item(0).getTextContent();
                // Adiciona à lista apenas se NÃO for o próprio jogador
                if (!oppName.equals(meuUsername)) {
                    adversarios.add(u);
                }
            }
        }
    } catch (Exception e) {
        // Ignora erros de leitura para não quebrar a página
    }
%>

<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <title>Lobby - Pontos e Caixas</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f7fafc; color: #2d3748; padding: 40px; display: flex; flex-direction: column; align-items: center; }
        .header-card { background: white; padding: 20px 40px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); margin-bottom: 30px; width: 100%; max-width: 800px; display: flex; justify-content: space-between; align-items: center; border-top: 5px solid <%= minhaCor != null ? minhaCor : "#319795" %>; }
        .lobby-container { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); width: 100%; max-width: 800px; }
        h1, h2 { color: #2d3748; margin: 0; }
        .btn { padding: 8px 16px; border-radius: 6px; text-decoration: none; font-size: 0.9em; font-weight: bold; cursor: pointer; transition: 0.2s; border: none; }
        .btn-play { background: #319795; color: white; }
        .btn-play:hover { background: #287e7c; }
        .btn-logout { background: #e2e8f0; color: #4a5568; }
        .btn-logout:hover { background: #cbd5e0; }
        .player-list { list-style: none; padding: 0; margin: 0; }
        .player-item { display: flex; justify-content: space-between; align-items: center; padding: 15px; border-bottom: 1px solid #edf2f7; }
        .player-item:last-child { border-bottom: none; }
        .player-info { display: flex; align-items: center; gap: 15px; }
        .color-dot { width: 20px; height: 20px; border-radius: 50%; border: 2px solid #cbd5e0; }
    </style>
</head>
<body>

    <div class="header-card">
        <div>
            <h1>Olá, <%= meuUsername %>! 🎮</h1>
            <p style="margin: 5px 0 0 0; color: #718096; font-size: 0.9em;">A tua cor de jogo está ativa.</p>
        </div>
        <div style="display: flex; gap: 15px; align-items: center;">
            <div class="color-dot" style="background-color: <%= minhaCor %>;" title="A tua cor"></div>
            <a href="honra.jsp" class="btn" style="background:#d69e2e;color:white;">🏆 Quadro de Honra</a>
            <a href="perfil.jsp" class="btn" style="background:#319795;color:white;">✏️ O Meu Perfil</a>
            <a href="logout.jsp" class="btn btn-logout">Sair</a>
        </div>
    </div>

    <div class="lobby-container">
        <h2 style="margin-bottom: 20px; border-bottom: 2px solid #edf2f7; padding-bottom: 10px;">Escolhe um Adversário</h2>

        <!-- Pesquisa por nome completo — filtra a lista em tempo real -->
        <div style="margin-bottom: 20px;">
            <input type="text" id="campoPesquisa" placeholder="Procurar jogador pelo nome completo..."
                autocomplete="new-password"
                style="width: 100%; padding: 10px 14px; border: 2px solid #e2e8f0; border-radius: 8px;
                       font-size: 1em; box-sizing: border-box; outline: none; transition: border 0.2s;"
                onfocus="this.style.borderColor=corDestaque"
                onblur="this.style.borderColor='#e2e8f0'"
                oninput="filtrar(this.value)">
        </div>

        <!-- Lista completa (filtrada por JS) -->
        <ul class="player-list" id="listaJogadores">
            <% if (adversarios.isEmpty()) { %>
                <li style="text-align: center; color: #a0aec0; padding: 20px;">Não há outros jogadores registados de momento.</li>
            <% } else { 
                for (Element adv : adversarios) { 
                    String nomeAdv    = adv.getElementsByTagName("username").item(0).getTextContent();
                    String firstnames = adv.getElementsByTagName("firstnames").getLength() > 0 ? adv.getElementsByTagName("firstnames").item(0).getTextContent().trim() : "";
                    String lastnames  = adv.getElementsByTagName("lastnames").getLength()  > 0 ? adv.getElementsByTagName("lastnames").item(0).getTextContent().trim()  : "";
                    String nomeCompleto = (firstnames + " " + lastnames).trim();
                    if (nomeCompleto.isEmpty()) nomeCompleto = nomeAdv;
                    String natAdv  = adv.getElementsByTagName("nationality").getLength() > 0 ? adv.getElementsByTagName("nationality").item(0).getTextContent() : "";
                    String corAdv  = adv.getElementsByTagName("corFundo").getLength()    > 0 ? adv.getElementsByTagName("corFundo").item(0).getTextContent()    : "#FFFFFF";
            %>
                <li class="player-item" data-nome="<%= nomeCompleto.toLowerCase() %> <%= nomeAdv.toLowerCase() %>">
                    <div class="player-info">
                        <div class="color-dot" style="background-color: <%= corAdv %>;" title="Cor do adversário"></div>
                        <span>
                            <strong><%= nomeCompleto %></strong>
                            <span style="color:#a0aec0; font-size:0.85em;"> (@<%= nomeAdv %>)</span>
                        </span>
                        <span style="color: #718096; font-size: 0.85em;"><%= natAdv %></span>
                    </div>
                    <a href="jogo.jsp?adversario=<%= nomeAdv %>" class="btn btn-play" target="_blank">Desafiar ⚔️</a>
                </li>
            <%  } 
               } %>
        </ul>
    </div>

    <script>
        const corDestaque = "<%= corDestaque %>";

        function filtrar(termo) {
            const t = termo.trim().toLowerCase();
            document.querySelectorAll("#listaJogadores .player-item").forEach(item => {
                item.style.display = t === "" || item.dataset.nome.includes(t) ? "" : "none";
            });
        }
    </script>


</body>
</html>