<%@ page isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-pt">
<head>
    <meta charset="UTF-8">
    <title>Erro</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Courier New', monospace;
            background: #0d0d0d;
            color: #e0e0e0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }

        .container {
            max-width: 640px;
            width: 100%;
        }

        .code-block {
            background: #1a1a1a;
            border: 1px solid #333;
            border-radius: 6px;
            overflow: hidden;
        }

        .code-header {
            background: #222;
            border-bottom: 1px solid #333;
            padding: 10px 16px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .dot {
            width: 12px;
            height: 12px;
            border-radius: 50%;
        }
        .dot-red   { background: #ff5f57; }
        .dot-yellow{ background: #febc2e; }
        .dot-green { background: #28c840; }

        .code-title {
            margin-left: 8px;
            font-size: 12px;
            color: #888;
            letter-spacing: 0.5px;
        }

        .code-body {
            padding: 28px 24px;
        }

        .line {
            display: flex;
            gap: 16px;
            margin-bottom: 6px;
            font-size: 14px;
            line-height: 1.6;
        }

        .ln {
            color: #444;
            min-width: 20px;
            text-align: right;
            user-select: none;
        }

        .kw  { color: #c792ea; }
        .str { color: #c3e88d; }
        .num { color: #f78c6c; }
        .cmt { color: #546e7a; font-style: italic; }
        .fn  { color: #82aaff; }
        .var { color: #89ddff; }
        .txt { color: #e0e0e0; }

        .divider {
            border: none;
            border-top: 1px solid #2a2a2a;
            margin: 20px 0;
        }

        .meta {
            display: grid;
            grid-template-columns: auto 1fr;
            gap: 6px 16px;
            font-size: 13px;
            align-items: start;
        }

        .meta-key {
            color: #546e7a;
            white-space: nowrap;
        }

        .meta-val {
            color: #e0e0e0;
            word-break: break-word;
        }

        .meta-val.error-detail {
            color: #ff5f57;
        }

        .back-btn {
            display: inline-block;
            margin-top: 20px;
            padding: 8px 18px;
            font-family: 'Courier New', monospace;
            font-size: 13px;
            color: #0d0d0d;
            background: #e0e0e0;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            transition: background 0.15s;
        }
        .back-btn:hover { background: #fff; }
    </style>
</head>
<body>
<%
    Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    String mensagem    = (String)  request.getAttribute("jakarta.servlet.error.message");
    String uri         = (String)  request.getAttribute("jakarta.servlet.error.request_uri");
    Throwable excecao  = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

    if (statusCode == null) statusCode = 500;
    if (mensagem   == null || mensagem.isEmpty()) mensagem = "Erro inesperado no servidor.";
    if (uri        == null) uri = "desconhecido";
%>
<div class="container">
    <div class="code-block">

        <!-- barra estilo terminal -->
        <div class="code-header">
            <span class="dot dot-red"></span>
            <span class="dot dot-yellow"></span>
            <span class="dot dot-green"></span>
            <span class="code-title">servidor.log</span>
        </div>

        <!-- "código" decorativo que mostra o erro -->
        <div class="code-body">
            <div class="line"><span class="ln">1</span><span><span class="kw">throw</span> <span class="kw">new</span> <span class="fn">HttpException</span><span class="txt">(</span><span class="num"><%= statusCode %></span><span class="txt">);</span></span></div>
            <div class="line"><span class="ln">2</span><span class="cmt">// <%= mensagem %></span></div>
            <div class="line"><span class="ln">3</span><span><span class="var">uri</span> <span class="txt">=</span> <span class="str">"<%= uri %>"</span><span class="txt">;</span></span></div>
            <% if (excecao != null) { %>
            <div class="line"><span class="ln">4</span><span class="cmt">// <%= excecao.toString() %></span></div>
            <% } %>

            <hr class="divider">

            <div class="meta">
                <span class="meta-key">ESTADO</span>
                <span class="meta-val error-detail"><%= statusCode %></span>

                <span class="meta-key">CAMINHO</span>
                <span class="meta-val"><%= uri %></span>

                <span class="meta-key">DETALHE</span>
                <span class="meta-val"><%= mensagem %></span>

                <% if (excecao != null) { %>
                <span class="meta-key">EXCEÇÃO</span>
                <span class="meta-val error-detail"><%= excecao.toString() %></span>
                <% } %>
            </div>

            <a class="back-btn" href="javascript:history.back()">← Voltar</a>
        </div>
    </div>
</div>
</body>
</html>
