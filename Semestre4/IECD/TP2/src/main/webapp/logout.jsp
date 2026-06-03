<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Destrói a sessão do utilizador corretamente no servidor
    session.invalidate();
    
    // Manda de volta para o login
    response.sendRedirect("login.jsp?msgSucesso=" + java.net.URLEncoder.encode("Sessão terminada com sucesso.", "UTF-8"));
%>