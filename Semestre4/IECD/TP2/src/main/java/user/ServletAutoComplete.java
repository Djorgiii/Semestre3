package user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/ServletAutoComplete")
public class ServletAutoComplete extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Configurar a resposta para ser em XML puro (Requisito 6)
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String query = request.getParameter("q");
        
        // Iniciar a construção do documento XML de resposta
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<resultados>");

        // Se a pesquisa estiver vazia ou for muito curta, devolvemos a lista vazia
        if (query != null && query.trim().length() >= 1) {
            String termoPesquisa = query.trim().toLowerCase();
            
            try {
                // 2. Abrir e ler a base de dados XML
                String xmlPath = getServletContext().getRealPath("/users.xml");
                File xmlFile = new File(xmlPath);

                if (xmlFile.exists()) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(xmlFile);
                    
                    NodeList listaUsers = doc.getElementsByTagName("user");

                    // 3. Procurar utilizadores pelo nome completo (Requisito 2)
                    for (int i = 0; i < listaUsers.getLength(); i++) {
                        Element u = (Element) listaUsers.item(i);
                        
                        String firstnames = u.getElementsByTagName("firstnames").item(0).getTextContent();
                        String lastnames = u.getElementsByTagName("lastnames").item(0).getTextContent();
                        String username = u.getElementsByTagName("username").item(0).getTextContent();
                        
                        // Junta o nome completo para testar
                        String nomeCompleto = firstnames + " " + lastnames;
                        
                        // Se o nome completo contiver o texto que o jogador digitou...
                        if (nomeCompleto.toLowerCase().contains(termoPesquisa)) {
                            // Cria um nó XML para este utilizador encontrado
                            out.println("  <jogador>");
                            out.println("    <username>" + username + "</username>");
                            out.println("    <nomeCompleto>" + nomeCompleto + "</nomeCompleto>");
                            out.println("  </jogador>");
                        }
                    }
                }
            } catch (Exception e) {
                // Em caso de erro, o XML devolve vazio para não quebrar o frontend
                System.err.println("Erro no AutoComplete: " + e.getMessage());
            }
        }
        
        out.println("</resultados>");
        out.close();
    }
}