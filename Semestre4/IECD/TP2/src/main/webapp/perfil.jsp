<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
    perfil.jsp — página de edição do perfil do jogador.

    Permite ao jogador:
      - Alterar a senha de acesso.
      - Actualizar a fotografia de perfil (upload de imagem convertida para Base64).
      - Definir a cor de fundo preferida para o ecrã de jogo.

    A alteração é enviada ao servidor TCP via Stub.alterar() e gravada no users.xml.
    A cor de fundo é guardada localmente no users.xml e carregada na sessão HTTP
    no próximo login.
--%>
<%@ page import="java.io.*, java.util.*, org.w3c.dom.*, javax.xml.parsers.*"%>
<%
    // Verificação de sessão — redireciona para login se não autenticado
    String meuUsername = (String) session.getAttribute("username");
    String meuUserid   = (String) session.getAttribute("userid");
    if (meuUsername == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Mensagens de feedback vindas do UserServlet
    String msgSucesso = request.getParameter("msgSucesso") != null
        ? java.net.URLDecoder.decode(request.getParameter("msgSucesso"), "UTF-8") : null;
    String msgErroSessao = (String) session.getAttribute("mensagemErroSessao");
    if (msgErroSessao != null) session.removeAttribute("mensagemErroSessao");

    // Ler os dados actuais do utilizador a partir do users.xml
    String fotoBase64  = "";
    String corFundo    = "#ffffff";
    String firstnames  = "";
    String lastnames   = "";
    String email       = "";
    String gender      = "";
    String birthdate   = "";
    String nationality = "";
    String blocked     = "false";
    String profile     = "1";

    try {
        String xmlPath = application.getRealPath("/users.xml");
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(new File(xmlPath));
        NodeList users = doc.getElementsByTagName("user");

        for (int i = 0; i < users.getLength(); i++) {
            Element u = (Element) users.item(i);
            NodeList idNL = u.getElementsByTagName("userid");
            if (idNL.getLength() > 0 && idNL.item(0).getTextContent().equals(meuUserid)) {
                // Ler todos os campos do utilizador
                NodeList nl;
                nl = u.getElementsByTagName("firstnames");  if (nl.getLength() > 0) firstnames  = nl.item(0).getTextContent();
                nl = u.getElementsByTagName("lastnames");   if (nl.getLength() > 0) lastnames   = nl.item(0).getTextContent();
                nl = u.getElementsByTagName("email");       if (nl.getLength() > 0) email       = nl.item(0).getTextContent();
                nl = u.getElementsByTagName("gender");      if (nl.getLength() > 0) gender      = nl.item(0).getTextContent();
                nl = u.getElementsByTagName("birthdate");   if (nl.getLength() > 0) birthdate   = nl.item(0).getTextContent();
                nl = u.getElementsByTagName("nationality"); if (nl.getLength() > 0) nationality = nl.item(0).getTextContent();
                nl = u.getElementsByTagName("blocked");     if (nl.getLength() > 0) blocked     = nl.item(0).getTextContent();
                nl = u.getElementsByTagName("profile");     if (nl.getLength() > 0) profile     = nl.item(0).getTextContent();
                nl = u.getElementsByTagName("corFundo");    if (nl.getLength() > 0) corFundo    = nl.item(0).getTextContent().trim();
                nl = u.getElementsByTagName("photography"); if (nl.getLength() > 0) fotoBase64  = nl.item(0).getTextContent().trim();
                break;
            }
        }
    } catch (Exception e) {
        msgErroSessao = "Erro ao carregar perfil: " + e.getMessage();
    }
%>
<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <title>O Meu Perfil - Pontos e Caixas</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f7fafc; display: flex; justify-content: center; padding: 40px; color: #2d3748; }
        .card { background: white; padding: 35px; border-radius: 12px; width: 520px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); border-top: 6px solid <%= corFundo %>; }
        h2 { margin-top: 0; color: #2d3748; }
        .form-group { margin-bottom: 16px; }
        label { display: block; font-size: 0.85em; font-weight: bold; margin-bottom: 5px; color: #4a5568; }
        input[type="text"], input[type="email"], input[type="password"], input[type="date"], input[type="color"] {
            width: 100%; padding: 10px; border: 1px solid #cbd5e0; border-radius: 6px;
            box-sizing: border-box; font-size: 14px; background: #f7fafc;
        }
        input[type="color"] { height: 42px; padding: 2px; cursor: pointer; }
        .radio-group { display: flex; gap: 20px; margin-top: 5px; }
        .radio-group label { font-weight: normal; font-size: 14px; cursor: pointer; display: flex; align-items: center; gap: 5px; }
        .foto-preview { width: 90px; height: 90px; border-radius: 50%; object-fit: cover; border: 3px solid <%= corFundo %>; display: block; margin: 0 auto 16px; }
        .foto-default { width: 90px; height: 90px; border-radius: 50%; background: #e2e8f0; display: flex; align-items: center; justify-content: center; font-size: 2.5em; margin: 0 auto 16px; }
        .btn-gravar { width: 100%; padding: 12px; background: #319795; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 16px; font-weight: bold; margin-top: 8px; }
        .btn-gravar:hover { background: #2c7a7b; }
        .btn-voltar { display: block; text-align: center; margin-top: 14px; color: #319795; text-decoration: none; font-size: 0.9em; }
        .alert-sucesso { background: #c6f6d5; color: #276749; padding: 12px; border-radius: 6px; margin-bottom: 16px; font-weight: bold; border-left: 4px solid #38a169; }
        .alert-erro { background: #fed7d7; color: #742a2a; padding: 12px; border-radius: 6px; margin-bottom: 16px; font-weight: bold; border-left: 4px solid #e53e3e; }
        .secao { font-size: 0.8em; font-weight: bold; color: #a0aec0; text-transform: uppercase; letter-spacing: 1px; margin: 20px 0 10px; border-bottom: 1px solid #edf2f7; padding-bottom: 4px; }
        .campo-bloqueado { background: #edf2f7 !important; color: #718096; cursor: not-allowed; }
    </style>
</head>
<body>
<div class="card">
    <h2>👤 O Meu Perfil</h2>
    <p style="color:#718096; font-size:0.9em; margin-top:-10px;"><%= meuUsername %></p>

    <% if (msgSucesso != null) { %>
        <div class="alert-sucesso">✅ <%= msgSucesso %></div>
    <% } %>
    <% if (msgErroSessao != null) { %>
        <div class="alert-erro">⚠️ <%= msgErroSessao %></div>
    <% } %>

    <%-- Foto de perfil actual --%>
    <% if (!fotoBase64.isBlank()) { %>
        <img class="foto-preview" src="data:image/jpeg;base64,<%= fotoBase64 %>" alt="Foto de perfil" id="fotoPreview">
    <% } else { %>
        <div class="foto-default" id="fotoPreview">👤</div>
    <% } %>

    <%-- O formulário submete para o UserServlet com action=save e o userid --%>
    <form action="UserServlet?action=save" method="post" enctype="multipart/form-data">
        <%-- Campos ocultos necessários para o UserServlet actualizar (não criar) --%>
        <input type="hidden" name="userid"    value="<%= meuUserid %>">
        <input type="hidden" name="blocked"   value="<%= blocked %>">
        <input type="hidden" name="profile"   value="<%= profile %>">

        <div class="secao">Dados Pessoais</div>

        <div style="display:flex; gap:12px;">
            <div class="form-group" style="flex:1;">
                <label>Nomes Próprios *</label>
                <input type="text" name="firstnames" value="<%= firstnames %>" required>
            </div>
            <div class="form-group" style="flex:1;">
                <label>Apelidos *</label>
                <input type="text" name="lastnames" value="<%= lastnames %>" required>
            </div>
        </div>

        <div class="form-group">
            <label>Email *</label>
            <input type="email" name="email" value="<%= email %>" required>
        </div>

        <div style="display:flex; gap:12px;">
            <div class="form-group" style="flex:1;">
                <label>Data de Nascimento</label>
                <input type="date" name="birthdate" value="<%= birthdate %>">
            </div>
            <div class="form-group" style="flex:1;">
                <label>Nacionalidade</label>
                <%-- O UserServlet espera o formato [PT] — formatar o valor guardado --%>
                <input type="text" name="nationality"
                       value="<%= nationality.isBlank() ? "" : "[" + nationality + "]" %>"
                       placeholder="Ex: [PT]"
                       pattern=".*\[[A-Za-z]{2,3}\].*"
                       title="Usa o formato [PT], [BR], etc.">
            </div>
        </div>

        <div class="form-group">
            <label>Género</label>
            <div class="radio-group">
                <label><input type="radio" name="gender" value="M" <%= "M".equals(gender) ? "checked" : "" %>> Masculino</label>
                <label><input type="radio" name="gender" value="F" <%= "F".equals(gender) ? "checked" : "" %>> Feminino</label>
                <label><input type="radio" name="gender" value="X" <%= "X".equals(gender) ? "checked" : "" %>> Outro</label>
            </div>
        </div>

        <div class="secao">Personalização</div>

        <div class="form-group">
            <label>Cor do Tabuleiro de Jogo *</label>
            <input type="color" name="corFundo" value="<%= corFundo %>"
                   oninput="document.querySelector('.card').style.borderTopColor=this.value">
        </div>

        <div class="form-group">
            <label>Nova Fotografia de Perfil (deixa em branco para manter a actual)</label>
            <input type="file" name="photoFile" accept="image/*"
                   onchange="previewFoto(this)">
        </div>

        <div class="secao">Segurança</div>

        <div class="form-group">
            <label>Nova Password (deixa em branco para não alterar)</label>
            <input type="password" name="password" placeholder="Mínimo 8 caracteres" minlength="8">
        </div>

        <%-- Username não é editável --%>
        <div class="form-group">
            <label>Username (não pode ser alterado)</label>
            <input type="text" name="username" value="<%= meuUsername %>"
                   class="campo-bloqueado" readonly>
        </div>

        <button type="submit" class="btn-gravar">💾 Guardar Alterações</button>
    </form>

    <a href="lobby.jsp" class="btn-voltar">← Voltar ao Lobby</a>
</div>

<script>
    // Pré-visualização da nova foto antes de submeter
    function previewFoto(input) {
        if (input.files && input.files[0]) {
            let reader = new FileReader();
            reader.onload = function(e) {
                let prev = document.getElementById("fotoPreview");
                // Substituir div por img ou actualizar img existente
                if (prev.tagName === "DIV") {
                    let img = document.createElement("img");
                    img.className = "foto-preview";
                    img.id = "fotoPreview";
                    img.src = e.target.result;
                    prev.replaceWith(img);
                } else {
                    prev.src = e.target.result;
                }
            };
            reader.readAsDataURL(input.files[0]);
        }
    }
</script>
</body>
</html>
