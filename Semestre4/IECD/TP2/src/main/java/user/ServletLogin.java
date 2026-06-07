package user;

/**
 * ServletLogin — trata a autenticação dos jogadores na versão web.
 *
 * Recebe as credenciais do formulário login.jsp (POST), calcula o hash SHA-256
 * da password, pesquisa o utilizador em users.xml e, em caso de sucesso,
 * cria uma sessão HTTP com os atributos necessários para o jogo:
 *   - username  — nome de utilizador
 *   - userid    — identificador UUID
 *   - corFundo  — cor de fundo preferida do tabuleiro
 *   - password  — password em texto simples (necessária para o Stub.iniciar())
 *
 * Em caso de erro, redireciona para login.jsp com mensagem de erro.
 *
 * Endpoint: POST /ServletLogin
 */

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

@WebServlet("/ServletLogin")
public class ServletLogin extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // 1. Receber os dados do formulário (login.jsp)
        String userDigitado = request.getParameter("username");
        String passDigitada = request.getParameter("password");

        try {
            // 2. Converter a password digitada para SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(passDigitada.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) { sb.append(String.format("%02x", b)); }
            String senhaHash = sb.toString();

            // 3. Abrir o ficheiro users.xml
            String xmlPath = getServletContext().getRealPath("/users.xml");
            File xmlFile = new File(xmlPath);

            if (!xmlFile.exists()) {
                request.setAttribute("erro", "Erro: Base de dados (users.xml) não encontrada!");
                request.getRequestDispatcher("login.jsp").forward(request, response);
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            NodeList listaUsers = doc.getElementsByTagName("user");

            boolean autenticado = false;
            String userIdLogado = "";
            String corFundoLogado = "#FFFFFF"; // Cor por defeito (branco) caso falhe a leitura

            // 4. Procurar o utilizador no XML
            for (int i = 0; i < listaUsers.getLength(); i++) {
                Element u = (Element) listaUsers.item(i);
                String xmlUsername = u.getElementsByTagName("username").item(0).getTextContent();
                String xmlPassword = u.getElementsByTagName("password").item(0).getTextContent();

                // Compara o username e o Hash da password
                if (xmlUsername.equals(userDigitado) && xmlPassword.equals(senhaHash)) {
                    autenticado = true;
                    userIdLogado = u.getElementsByTagName("userid").item(0).getTextContent();
                    
                    // --- NOVA LEITURA DA COR AQUI ---
                    if (u.getElementsByTagName("corFundo").getLength() > 0) {
                        corFundoLogado = u.getElementsByTagName("corFundo").item(0).getTextContent().trim();
                    }
                    
                    break; // Utilizador encontrado, não precisamos de procurar mais
                }
            }

            // 5. Decisão Final
            if (autenticado) {
                // Cria a sessão na memória do Tomcat (O utilizador está logado!)
                HttpSession sessao = request.getSession();
                sessao.setAttribute("username", userDigitado);
                sessao.setAttribute("userid", userIdLogado);
                
                // --- GUARDA A COR NA SESSÃO DO TOMCAT ---
                sessao.setAttribute("corFundo", corFundoLogado);
                sessao.setAttribute("password", passDigitada);
                
                // Redireciona para o Lobby para escolher adversário!
                response.sendRedirect("lobby.jsp"); 
            } else {
                // Volta para o login e mostra a mensagem de erro a vermelho
                request.setAttribute("erro", "❌ Nome de utilizador ou palavra-passe incorretos!");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }

        } catch (Exception e) {
            request.setAttribute("erro", "❌ Erro interno do servidor: " + e.getMessage());
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}