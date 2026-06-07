# Pontos e Caixas — TP2 (Parte Web)

## Infraestruturas Computacionais Distribuídas
**Instituto Superior de Engenharia de Lisboa**
Licenciatura em Engenharia Informática e Multimédia

---

## Grupo

| Nome | Número de Aluno |
|---|---|
| Jorge Gonçalves | 52345 |
| Helena Nina | 52708 |
| Margarida Andrade | 52146 |

**Data de entrega:** 7 de Junho de 2026

---

## Descrição

Extensão web do jogo multiplayer **Pontos e Caixas** desenvolvido na 1ª parte do trabalho. Implementa uma solução para a World Wide Web que coexiste com a versão de consola/GUI e partilha o mesmo servidor TCP.

## Tecnologias

- Java 25 / Jakarta EE (JSP + Servlets)
- Apache Tomcat 11
- Protocolo TCP com mensagens XML/XSD
- HTML5 / CSS3 / JavaScript

## Requisitos implementados

1. Perfil do jogador com fotografia e cor de fundo preferida
2. AutoComplete para pesquisa de oponentes por nome completo
3. Jogos simultâneos com limite de 30 segundos por jogada
4. Quadro de honra com fotografias e bandeiras ordenado por vitórias
5. Preservação do quadro de honra em caso de falha (backup automático)
6. Protocolo XML/XSD
7. Compatibilidade com jogadores em modo Consola/GUI e Web em simultâneo
8. Registo de auditoria das mensagens em `protocolo.log`

## Estrutura relevante

```
TP2/
├── src/
│   ├── client/       Stub.java
│   ├── server/       Servidor.java, ServidorJogo.java, Skeleton.java
│   └── user/         ServletGame.java, UserServlet.java
├── webapp/
│   ├── login.jsp, lobby.jsp, jogo.jsp
│   ├── perfil.jsp, honra.jsp, abandonar.jsp, erro.jsp
│   ├── WEB-INF/
│   │   └── web.xml
│   ├── metodos-cli.xsd   protocolo cliente → servidor
│   ├── metodos-srv.xsd   protocolo servidor → cliente
│   ├── nationalities.xsd, nationality.xsd
│   ├── user.xsd, users.xsd
│   ├── resultados.xsd
│   ├── nationalities.xml, users.xml, resultados.xml
│   └── readme.md
```

## Ficheiros de dados (gerados em runtime)
 
| Ficheiro | Descrição |
|---|---|
| `users.xml` | Perfis dos jogadores (validado por `users.xsd`) |
| `resultados.xml` | Histórico de jogos (validado por `resultados.xsd`) |
| `protocolo.log` | Log de auditoria das mensagens TCP |
 
## Configuração (web.xml)
 
| Parâmetro | Valor por omissão |
|---|---|
| `servidorHost` | `localhost` Pode ser outro mediante alteração |
| `servidorPorto` | `25565` Pode ser outro mediante alteração |