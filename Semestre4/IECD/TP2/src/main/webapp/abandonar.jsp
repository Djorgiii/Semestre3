<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="client.Stub"%>
<%
    /*
     * abandonar.jsp — abandono de uma partida em curso.
     *
     * Recebe o parâmetro "adversario" que identifica qual a partida a abandonar.
     * Fecha o Stub TCP em background (pode estar bloqueado em obter()) para
     * não bloquear a resposta HTTP, limpa todos os atributos da sessão
     * relativos a essa partida e redireciona para o lobby.
     *
     * O fecho do socket provoca uma excepção no ServidorDedicado, que notifica
     * o adversário com o estado "AB" (abandono) antes de terminar.
     */

    // Identificar qual o jogo a abandonar pelo nome do adversário
    String adversario = request.getParameter("adversario");
    if (adversario != null && !adversario.isBlank()) {
        String keyStub = "stub_" + adversario;

        // Fechar o Stub TCP numa Thread separada para não bloquear caso esteja
        // preso em obter() à espera de uma jogada do adversário
        Stub stub = (Stub) session.getAttribute(keyStub);
        if (stub != null) {
            new Thread(() -> {
                try { stub.close(); } catch (Exception e) { /* ignora */ }
            }).start();
        }

        // Limpar todos os atributos desta partida da sessão HTTP
        session.removeAttribute(keyStub);
        session.removeAttribute("vez_"        + adversario);
        session.removeAttribute("obterFeito_" + adversario);
        session.removeAttribute("tabuleiro_"  + adversario);
        session.removeAttribute("pendente_"   + adversario);
    }

    // Redirecionar para o lobby após o abandono
    response.sendRedirect("lobby.jsp");
%>
