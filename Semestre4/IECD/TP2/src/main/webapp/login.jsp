<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-PT">
<head>
    <meta charset="UTF-8">
    <title>Pontos e Caixas - Login</title>
    <style>
        body { 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
            background-color: #f0f2f5; 
            display: flex; 
            justify-content: center; 
            align-items: center; 
            height: 100vh; 
            margin: 0; 
        }
        .login-container { 
            background: white; 
            padding: 40px; 
            border-radius: 12px; 
            box-shadow: 0 4px 15px rgba(0,0,0,0.1); 
            text-align: center;
            width: 300px;
        }
        .login-container h2 { margin-top: 0; color: #333; }
        input[type="text"], input[type="password"] { 
            width: 100%; 
            padding: 12px; 
            margin: 10px 0; 
            border: 1px solid #ccc; 
            border-radius: 6px; 
            box-sizing: border-box; /* Garante que o padding não estraga a largura */
        }
        button { 
            width: 100%; 
            padding: 12px; 
            background-color: #0056b3; 
            color: white; 
            border: none; 
            border-radius: 6px; 
            cursor: pointer; 
            font-size: 16px; 
            font-weight: bold;
            margin-top: 15px;
        }
        button:hover { background-color: #004494; }
        .erro { color: #d9534f; margin-top: 15px; font-weight: bold; }
    </style>
</head>
<body>

    <div class="login-container">
        <h2>🎮 Pontos & Caixas</h2>
        <p>Acesso Web</p>
        
        <form action="ServletLogin" method="POST">
            <input type="text" name="username" placeholder="Nome de Utilizador" required>
            <input type="password" name="password" placeholder="Palavra-passe" required>
            <button type="submit">Entrar</button>
        </form>
        
        <div style="margin-top: 20px; font-size: 0.9em; color: #666;">
    		Ainda não tens conta? 
    		<a href="register.jsp" style="color: #0056b3; font-weight: bold; text-decoration: none;">Regista-te aqui</a>
		</div>

        <% 
            String msgErro = (String) request.getAttribute("erro");
            if(msgErro != null) { 
        %>
            <div class="erro"><%= msgErro %></div>
        <% } %>
    </div>

</body>
</html>