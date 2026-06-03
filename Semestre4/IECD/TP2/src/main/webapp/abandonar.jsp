<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="client.Stub"%>
<%
    // Identificar qual o jogo a abandonar pelo adversário
    String adversario = request.getParameter("adversario");
    if (adversario != null && !adversario.isBlank()) {
        String keyStub = "stub_" + adversario;

        // Fechar o Stub TCP em background (pode estar bloqueado em obter())
        Stub stub = (Stub) session.getAttribute(keyStub);
        if (stub != null) {
            new Thread(() -> {
                try { stub.close(); } catch (Exception e) { /* ignora */ }
            }).start();
        }

        // Limpar todos os atributos desta partida da sessão
        session.removeAttribute(keyStub);
        session.removeAttribute("vez_"       + adversario);
        session.removeAttribute("obterFeito_"+ adversario);
        session.removeAttribute("tabuleiro_" + adversario);
        session.removeAttribute("pendente_"  + adversario);
    }

    response.sendRedirect("lobby.jsp");
%>
