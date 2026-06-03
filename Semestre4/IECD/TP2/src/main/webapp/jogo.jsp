<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String meuUsername = (String) session.getAttribute("username");
    String minhaCor = (String) session.getAttribute("corFundo");
    
    if (meuUsername == null) {
        response.sendRedirect("login.jsp?msgErro=" + java.net.URLEncoder.encode("Inicia sessão para jogar.", "UTF-8"));
        return;
    }
    if (minhaCor == null) minhaCor = "#2d3748";

    String adversario = request.getParameter("adversario");
    if (adversario == null || adversario.isEmpty()) {
        adversario = "Adversário Desconhecido";
    }
%>
<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <title>A Jogar: <%= meuUsername %> vs <%= adversario %></title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f7fafc; display: flex; flex-direction: column; align-items: center; padding: 20px; color: #2d3748; }
        .game-header { text-align: center; margin-bottom: 20px; }
        .vs-badge { background: #e2e8f0; padding: 5px 15px; border-radius: 20px; font-weight: bold; font-size: 0.9em; margin: 0 10px; }
        .board-container { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 8px 16px rgba(0,0,0,0.1); border-top: 6px solid <%= minhaCor %>; }
        svg { background: #fff; display: block; margin: 0 auto; }
        
        .dot { fill: #4a5568; }
        .line-hover { stroke: transparent; stroke-width: 20; cursor: pointer; }
        .line-hover:hover { stroke: <%= minhaCor %>; opacity: 0.3; }
        .line-drawn { stroke-width: 4; stroke-linecap: round; }
        
        .btn-leave { margin-top: 20px; padding: 10px 20px; background: #e53e3e; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; text-decoration: none; }
    </style>
</head>
<body>

    <div class="game-header">
        <h1>Pontos e Caixas</h1>
        <div style="font-size: 1.2em; margin-top: 10px;">
            <span style="color: <%= minhaCor %>; font-weight: bold;">@<%= meuUsername %></span>
            <span class="vs-badge">VS</span>
            <span style="font-weight: bold;">@<%= adversario %></span>
        </div>
        <p id="status-jogo" style="font-size: 0.85em; color: #718096; font-weight: bold;">A aguardar jogadas...</p>
    </div>

    <div class="board-container">
        <svg width="400" height="400" id="gameBoard">
            <% 
                int espacamento = 100, margem = 50, tamanhoGrelha = 4;

                // 1. HORIZONTAIS
                for (int linha = 0; linha < tamanhoGrelha; linha++) {
                    for (int col = 0; col < tamanhoGrelha - 1; col++) {
                        int x1 = margem + (col * espacamento), y1 = margem + (linha * espacamento);
                        String idLinha = "h-" + linha + "-" + col;
                        %>
                        <line x1="<%= x1 %>" y1="<%= y1 %>" x2="<%= x1 + espacamento %>" y2="<%= y1 %>" 
                              class="line-hover" id="<%= idLinha %>" onclick="fazerJogada('<%= idLinha %>')" />
                        <%
                    }
                }

                // 2. VERTICAIS
                for (int col = 0; col < tamanhoGrelha; col++) {
                    for (int linha = 0; linha < tamanhoGrelha - 1; linha++) {
                        int x1 = margem + (col * espacamento), y1 = margem + (linha * espacamento);
                        String idLinha = "v-" + col + "-" + linha;
                        %>
                        <line x1="<%= x1 %>" y1="<%= y1 %>" x2="<%= x1 %>" y2="<%= y1 + espacamento %>" 
                              class="line-hover" id="<%= idLinha %>" onclick="fazerJogada('<%= idLinha %>')" />
                        <%
                    }
                }

                // 3. PONTOS
                for (int i = 0; i < tamanhoGrelha; i++) {
                    for (int j = 0; j < tamanhoGrelha; j++) {
                        int cx = margem + (j * espacamento), cy = margem + (i * espacamento);
                        %>
                        <circle cx="<%= cx %>" cy="<%= cy %>" r="6" class="dot" />
                        <%
                    }
                }
            %>
        </svg>
    </div>

    <a href="lobby.jsp" class="btn-leave">Abandonar Partida</a>

    <script>
        const MINHA_COR      = "<%= minhaCor %>";
        const COR_ADVERSARIO = "#a0aec0";
        const COR_CAIXA_X    = "#319795";
        const COR_CAIXA_O    = "#e53e3e";

        let jogoTerminado = false;

        // Desenha uma linha nova com a cor indicada (só se ainda não foi desenhada)
        function desenharLinha(id, cor) {
            let el = document.getElementById(id);
            if (el && el.className.baseVal !== "line-drawn") {
                el.className.baseVal = "line-drawn";
                el.style.stroke = cor;
                el.onclick = null;
            }
        }

        // Aplica o estado vindo do servidor ao tabuleiro (linhas + caixas)
        // linhasMinha: se true, as novas linhas são minhas; se false, são do adversário
        function aplicarEstado(dados, linhasSaoMinhas) {
            // 1. Desenhar linhas novas com a cor correcta
            if (dados.linhas) {
                dados.linhas.forEach(linha => {
                    let el = document.getElementById(linha.id);
                    if (el && el.className.baseVal !== "line-drawn") {
                        // Linha nova — determinar cor
                        let cor = linhasSaoMinhas ? MINHA_COR : COR_ADVERSARIO;
                        desenharLinha(linha.id, cor);
                    }
                    // Se já estava desenhada, não toca (preserva a cor original)
                });
            }

            // 2. Desenhar caixas fechadas
            if (dados.caixas) {
                dados.caixas.forEach(caixa => {
                    let idCaixa = "caixa-" + caixa.x + "-" + caixa.y;
                    if (!document.getElementById(idCaixa)) {
                        // Calcular posição central da caixa no SVG
                        let esp = 100, mar = 50;
                        let cx = mar + (caixa.x - 1) * esp + esp / 2;
                        let cy = mar + (caixa.y - 1) * esp + esp / 2;
                        let cor = (caixa.dono === "X") ? COR_CAIXA_X : COR_CAIXA_O;

                        let rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
                        rect.setAttribute("id", idCaixa);
                        rect.setAttribute("x", cx - 35);
                        rect.setAttribute("y", cy - 35);
                        rect.setAttribute("width", 70);
                        rect.setAttribute("height", 70);
                        rect.setAttribute("fill", cor);
                        rect.setAttribute("opacity", "0.35");
                        document.getElementById("gameBoard").insertBefore(rect, document.getElementById("gameBoard").firstChild);

                        let txt = document.createElementNS("http://www.w3.org/2000/svg", "text");
                        txt.setAttribute("x", cx);
                        txt.setAttribute("y", cy + 6);
                        txt.setAttribute("text-anchor", "middle");
                        txt.setAttribute("fill", cor);
                        txt.setAttribute("font-size", "22");
                        txt.setAttribute("font-weight", "bold");
                        txt.textContent = caixa.dono;
                        document.getElementById("gameBoard").appendChild(txt);
                    }
                });
            }

            // 3. Verificar estado do jogo
            let status = document.getElementById("status-jogo");
            const estado = dados.estado;
            if (estado === "VX") {
                status.innerText = "🏆 Vitória do Jogador X!";
                status.style.color = COR_CAIXA_X;
                jogoTerminado = true;
            } else if (estado === "VO") {
                status.innerText = "🏆 Vitória do Jogador O!";
                status.style.color = COR_CAIXA_O;
                jogoTerminado = true;
            } else if (estado === "EM") {
                status.innerText = "🤝 Empate!";
                status.style.color = "#718096";
                jogoTerminado = true;
            } else if (estado === "IV") {
                status.innerText = "❌ Jogada inválida! Tenta outra.";
                status.style.color = "#e53e3e";
            } else if (estado === "BO") {
                status.innerText = "🔥 Bónus! Fechaste uma caixa — joga outra vez!";
                status.style.color = MINHA_COR;
            }
        }

        // Envia a jogada e atualiza o tabuleiro com a resposta
        function fazerJogada(idLinha) {
            if (jogoTerminado) return;
            let linhaEl = document.getElementById(idLinha);
            if (!linhaEl || linhaEl.className.baseVal !== "line-hover") return;

            // Feedback visual imediato
            linhaEl.className.baseVal = "line-drawn";
            linhaEl.style.stroke = MINHA_COR;
            linhaEl.onclick = null;
            document.getElementById("status-jogo").innerText = "A enviar jogada...";

            fetch("ServletGame?acao=jogar&linha=" + idLinha)
                .then(r => r.json())
                .then(dados => {
                    if (dados.erro) {
                        // Desfazer feedback visual se deu erro
                        linhaEl.className.baseVal = "line-hover";
                        linhaEl.style.stroke = "";
                        linhaEl.onclick = () => fazerJogada(idLinha);
                        document.getElementById("status-jogo").innerText = "⚠️ " + dados.erro;
                        return;
                    }
                    aplicarEstado(dados, true); // linhas desta resposta são minhas
                    if (!jogoTerminado) {
                        document.getElementById("status-jogo").innerText = "⏳ Vez do adversário...";
                        setTimeout(verificarEstado, 1000);
                    }
                })
                .catch(() => {
                    document.getElementById("status-jogo").innerText = "⚠️ Erro de ligação.";
                });
        }

        // Polling: pergunta ao servidor se o adversário já jogou
        function verificarEstado() {
            if (jogoTerminado) return;
            fetch("ServletGame?acao=estado")
                .then(r => r.json())
                .then(dados => {
                    if (dados.erro) {
                        setTimeout(verificarEstado, 2000);
                        return;
                    }
                    if (dados.estado === "SUA_VEZ") {
                        // Ainda não jogou — continua à espera
                        setTimeout(verificarEstado, 1500);
                        return;
                    }
                    // O adversário jogou — as linhas novas são dele
                    aplicarEstado(dados, false);
                    if (!jogoTerminado && dados.minhaVez === true) {
                        document.getElementById("status-jogo").innerText = "✅ A tua vez!";
                    }
                })
                .catch(() => setTimeout(verificarEstado, 3000));
        }

        // Arranque: pede o estado inicial (handshake TCP)
        fetch("ServletGame?acao=estado")
            .then(r => r.json())
            .then(dados => {
                aplicarEstado(dados, false); // arranque: linhas existentes são do histórico
                // O servidor diz explicitamente se é a nossa vez neste momento
                if (dados.minhaVez === true) {
                    document.getElementById("status-jogo").innerText = "✅ A tua vez!";
                } else {
                    document.getElementById("status-jogo").innerText = "⏳ Vez do adversário...";
                    setTimeout(verificarEstado, 1500);
                }
            });
    </script>

</body>
</html>