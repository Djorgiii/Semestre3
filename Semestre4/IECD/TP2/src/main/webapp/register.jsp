<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Apanha a mensagem de erro vinda do Servlet, se existir
    String mensagemErro = "";
    if (request.getParameter("msgErro") != null) {
        mensagemErro = java.net.URLDecoder.decode(request.getParameter("msgErro"), "UTF-8");
    }
%>
<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <title>Registo de Jogador - Pontos e Caixas</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; display: flex; justify-content: center; padding: 40px; color: #2d3748; }
        .card { background: white; padding: 35px; border-radius: 12px; width: 450px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        h2 { text-align: center; color: #319795; margin-top: 0; }
        .form-group { margin-bottom: 15px; }
        label { display: block; font-size: 0.85em; font-weight: bold; margin-bottom: 5px; color: #4a5568; }
        input[type="text"], input[type="email"], input[type="password"], input[type="date"], input[type="file"], input[type="color"] { 
            width: 100%; padding: 10px; border: 1px solid #cbd5e0; border-radius: 6px; box-sizing: border-box; font-size: 14px;
        }
        .radio-group { display: flex; gap: 20px; align-items: center; margin-top: 5px; }
        .radio-group label { font-weight: normal; font-size: 14px; cursor: pointer; display: flex; align-items: center; gap: 5px; }
        button { width: 100%; padding: 12px; background: #319795; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 16px; font-weight: bold; margin-top: 10px; transition: background 0.2s; }
        button:hover { background: #2c7a7b; }
        .alert-danger { background: #fed7d7; color: #742a2a; padding: 12px; border-radius: 6px; margin-bottom: 20px; font-weight: bold; border-left: 4px solid #e53e3e; font-size: 0.9em; }
    </style>
</head>
<body>
    <div class="card">
        <h2>Criar Nova Conta 🎮</h2>
        
        <% if (!mensagemErro.isEmpty()) { %> 
            <div class="alert-danger">
                ⚠️ <%= mensagemErro %>
            </div> 
        <% } %>

        <form action="UserServlet?action=save" method="post" enctype="multipart/form-data">
            
            <div class="form-group">
                <label>Username *</label>
                <input type="text" name="username" placeholder="Ex: jorge_99" required maxlength="10" pattern="[a-zA-Z0-9_-]{4,10}">
            </div>

            <div style="display: flex; gap: 10px;">
                <div class="form-group" style="flex: 1;">
                    <label>Nome Próprio *</label>
                    <input type="text" name="firstnames" placeholder="Ex: Jorge" required>
                </div>
                <div class="form-group" style="flex: 1;">
                    <label>Apelidos *</label>
                    <input type="text" name="lastnames" placeholder="Ex: Gonçalves" required>
                </div>
            </div>

            <div class="form-group">
                <label>Email *</label>
                <input type="email" name="email" placeholder="email@isel.pt" required>
            </div>

            <div class="form-group">
                <label>Data de Nascimento *</label>
                <input type="date" name="birthdate" required>
            </div>

            <div class="form-group">
                <label>Nacionalidade *</label>
                <input type="text" name="nationality" list="listaPaises" placeholder="Ex: [PT] Portugal" required pattern=".*\[[A-Za-z]{2,3}\].*" title="Tem de incluir o código do país entre parênteses retos. Ex: [PT]">
                <datalist id="listaPaises">
                    <option value="[PT] Portugal"></option>
                    <option value="[BR] Brasil"></option>
                    <option value="[ES] Espanha"></option>
                    <option value="[GB] Reino Unido"></option>
                    <option value="[US] Estados Unidos"></option>
                </datalist>
            </div>

            <div class="form-group">
                <label>Género (Opcional)</label>
                <div class="radio-group">
                    <label><input type="radio" name="gender" value="M"> Masculino</label>
                    <label><input type="radio" name="gender" value="F"> Feminino</label>
                    <label><input type="radio" name="gender" value="X"> Outro</label>
                </div>
            </div>

            <div class="form-group">
                <label>Fotografia de Perfil (Opcional)</label>
                <input type="file" name="photoFile" accept="image/*">
            </div>

            <div class="form-group">
                <label>Cor do Tabuleiro (Fundo do Jogo) *</label>
                <input type="color" name="corFundo" value="#FFFFFF" style="height: 42px; padding: 2px; cursor: pointer;" title="Escolha a cor de fundo para o seu jogo">
            </div>

			<div class="form-group">
                <label>Password *</label>
                <input type="password" 
                       name="password" 
                       placeholder="Mínimo 8 carateres" 
                       required 
                       minlength="8" 
                       title="A password tem de ter pelo menos 8 carateres.">
            </div>
            
            <button type="submit">Finalizar Registo</button>
            <p style="text-align: center; margin-top: 20px; font-size: 0.9em;">
                Já tens conta? <a href="login.jsp" style="color: #319795; font-weight: bold; text-decoration: none;">Inicia sessão aqui</a>
            </p>
        </form>
    </div>
</body>
</html>