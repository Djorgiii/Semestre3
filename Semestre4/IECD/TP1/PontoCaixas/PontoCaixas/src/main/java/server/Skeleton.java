package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import user.User;
import util.XMLDoc;

/**
 * Classe que implementa a adaptação do servidor ao protocolo. 
 * Suporta a interação do cliente com o servidor, 
 * Converte mensagens em XML para ações/objetos e vice-versa.
 * * @author Engº Porfírio Filipe
 * (Adaptado para Pontos e Caixas)
 */
public class Skeleton {
    
    // **Métodos:**

    /**
     * Valida se o documento XML recebido representa um método válido.
     */
    private static void validXSD(final Document doc) throws Exception {
        try {
            // Valida o documento contra o XSD "metodos.xsd".
            XMLDoc.validDocXSD(doc, XMLDoc.getContexto() + "metodos-srv.xsd");
        } catch (SAXException | IOException e) {
            throw new Exception("Recebeu mensagem inválida: " + e.getLocalizedMessage());
        }
    }
    
    // Lê a próxima linha/mensagem e devolve num Document
    private static Document getNext(BufferedReader is) throws Exception {
        // Lê a linha que contém a mensagem.
        String line = is.readLine();
        registaLog("Servidor{" + line + "}");
        Document d = XMLDoc.parseString(line); 
        // Valida o schema XSD da mensagem.
        validXSD(d); // Descomenta quando o teu XSD estiver atualizado
        return d;
    }

    
    /**
     * Método que atende a chamada Registar Conta.
     */
    public static void runRegistar(Socket sk, Document docPedido) throws Exception {
        PrintWriter os = new PrintWriter(sk.getOutputStream(), true);
        Element regEl = getMethod(docPedido, "registar");

        // Extrair os novos atributos enviados pelo cliente
        String username = regEl.getAttribute("nickname");
        String senha = regEl.getAttribute("senha");
        String first = regEl.getAttribute("firstnames");
        String last = regEl.getAttribute("lastnames");
        String gender = regEl.getAttribute("gender");
        String birth = regEl.getAttribute("birthdate");
        String nac = regEl.getAttribute("nacionalidade");
        String foto = regEl.getElementsByTagName("photography").item(0).getTextContent();

        // 1. Carregar o users.xml
        String ficheiroUsers = XMLDoc.getContexto() + "users.xml";
        Document docUsers = XMLDoc.parseFile(ficheiroUsers);
        
        // 2. Verificar se o username já existe
        String xpathQuery = "//user[username='" + username + "']";
        NodeList users = XMLDoc.getXPath(xpathQuery, docUsers);
        
        if (users != null && users.getLength() > 0) {
            System.out.println("❌ Erro: O username '" + username + "' já está registado!");
            os.println("<metodo><registar resposta='Erro: Username já existe'/></metodo>");
            return;
        }

        // 3. Criar o utilizador
        Element root = docUsers.getDocumentElement();
        Element novoUser = docUsers.createElement("user");
        
        // Agora preenchemos com os dados REAIS usando a função addTag
        addTag(docUsers, novoUser, "userid", java.util.UUID.randomUUID().toString());
        // ISO_INSTANT garante que a data fica no formato exato que o XSD quer (ex: 2024-03-28T12:39:12Z)
        addTag(docUsers, novoUser, "updated", java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).format(java.time.format.DateTimeFormatter.ISO_INSTANT));
        addTag(docUsers, novoUser, "blocked", "false");
        addTag(docUsers, novoUser, "profile", "1");
        addTag(docUsers, novoUser, "username", username);
        addTag(docUsers, novoUser, "firstnames", first);   
        addTag(docUsers, novoUser, "lastnames", last);     
        addTag(docUsers, novoUser, "email", username + "@mail.pt");
        addTag(docUsers, novoUser, "gender", gender);      
        addTag(docUsers, novoUser, "birthdate", birth);    
        addTag(docUsers, novoUser, "photography", foto);
        addTag(docUsers, novoUser, "nationality", nac.toUpperCase());
        addTag(docUsers, novoUser, "password", XMLDoc.SHA256(senha));

        // 4. Junta o novo utilizador à base de dados e grava
        root.appendChild(novoUser);
        XMLDoc.gravarLock(docUsers, ficheiroUsers, XMLDoc.gerarNomeFBackupVersao(ficheiroUsers));
        
        // ---> 5. ATUALIZAR A MEMÓRIA RAM DO SERVIDOR! <---
        User._load(); // Diz à classe User para ler o ficheiro XML de novo!
        
        System.out.println("✅ Utilizador '" + username + "' registado com sucesso!");
        os.println("<metodo><registar resposta='Sucesso'/></metodo>");
    }

    // Método auxiliar apenas para encurtar o código acima
    private static void addTag(Document doc, Element parent, String name, String value) {
        Element e = doc.createElement(name);
        e.setTextContent(value);
        parent.appendChild(e);
    }    
    /**
     * Método que atende a chamada Alterar Perfil.
     */
    public static void runAlterar(Socket sk, Document docPedido) throws Exception {
        PrintWriter os = new PrintWriter(sk.getOutputStream(), true);
        
        // Usa o docPedido em vez de fazer getNext()
        Document x = docPedido; 
        
        Element alterarEl = getMethod(x, "alterar");
        String username = alterarEl.getAttribute("nickname");
        String novaSenha = alterarEl.getAttribute("senha");
        
        String novaFoto = null;
        if (alterarEl.getElementsByTagName("photography").getLength() > 0) {
            novaFoto = alterarEl.getElementsByTagName("photography").item(0).getTextContent();
        }
        
        String ficheiroUsers = XMLDoc.getContexto() + "users.xml";
        Document docUsers = XMLDoc.parseFile(ficheiroUsers);
        
        String xpathQuery = "//user[username='" + username + "']";
        NodeList users = XMLDoc.getXPath(xpathQuery, docUsers);
        
        if (users != null && users.getLength() > 0) {
            Element userNode = (Element) users.item(0);
            
            if (novaSenha != null && !novaSenha.isEmpty()) {
                userNode.getElementsByTagName("password").item(0).setTextContent(XMLDoc.SHA256(novaSenha));
            }
            if (novaFoto != null && !novaFoto.isEmpty()) {
                userNode.getElementsByTagName("photography").item(0).setTextContent(novaFoto);
            }
            
            XMLDoc.gravarLock(docUsers, ficheiroUsers, XMLDoc.gerarNomeFBackupVersao(ficheiroUsers));
            
            os.println("<metodo><alterar resposta='Sucesso'/></metodo>");
            System.out.println("✅ Perfil do utilizador '" + username + "' atualizado com sucesso!");
            
        } else {
            os.println("<metodo><alterar resposta='Erro: Utilizador não encontrado'/></metodo>");
        }
    }
    /**
     * Método que atende a chamada Iniciar.
     */
    /**
     * Método que atende a chamada Iniciar.
     */
    public static void runIniciar(Socket sk, char simbolo, Document docPedido) throws Exception {
        PrintWriter os = new PrintWriter(sk.getOutputStream(), true);
        
        System.out.println("   Jogador '" + simbolo + "': " + sk);
        Document x = docPedido;
        
        String Nome  = getMethod(x, "iniciar").getAttribute("nickname");
        String Senha = getMethod(x, "iniciar").getAttribute("senha");
        
        System.out.println("   Jogador '" + simbolo + "': " + Nome + " / " + Senha);
        User jg = User._authenticate(Nome, Senha);
        
        if(jg == null)
            throw new Exception("Falhou a autenticação do utilizador '" + Nome + "'!");
            
        System.out.println("Autenticação/login do utilizador '" + Nome + "' realizado com sucesso!");
        
        Document d = XMLDoc.parseString(jg.toXMLString(simbolo));
        Node jogador = d.getElementsByTagName("jogador").item(0);
        Node cloneElement = x.importNode(jogador, true);
        x.getElementsByTagName("iniciar").item(0).appendChild(cloneElement);
        
        // CORREÇÃO: Removemos as quebras de linha para o is.readLine() do cliente não encravar!
        String msg = XMLDoc.documentToString(x).replaceAll("\\r\\n|\\r|\\n", "");
        os.println(msg);
    }
    
    /**
     * Método que atende a chamada Obter.
     */
    public static void runObter(BufferedReader is, PrintWriter os, char simbolo, Socket sk, JogoXML jogo) throws Exception {
        Document obter = getNext(is);
        
        // Verifica a existência do elemento "obter" na mensagem.
        getMethod(obter, "obter");

        // Envia a mensagem "obter" para o jogador, 
        // com o tabuleiro atualizado indicando o estado atual do jogo.
        os.println("<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>");
    }

    /**
     * Método que atende a chamada Jogar.
     */
    public static JogoXML runJogar(BufferedReader is, PrintWriter os, char simbolo, Socket sk, JogoXML jogo) throws Exception {
        Document jogar = getNext(is);

        Element jogadaEl = getMethod(jogar, "jogar");
        String jogadaStr = jogadaEl.getAttribute("jogada");
        String[] partes = jogadaStr.trim().split("\\s+");

        try {
            if (partes.length == 4) {
                int[] coords = new int[4];
                for (int i = 0; i < 4; i++) {
                    coords[i] = Integer.parseInt(partes[i]);
                }
                jogo.joga(coords, simbolo);
            } else {
                throw new Exception("Formato de coordenadas inválido.");
            }
        } catch (Exception e) {
            System.err.println("Erro na jogada do Skeleton: " + e.getMessage());
        }

        // CORREÇÃO: Removemos as quebras de linha aqui também!
        String msg = XMLDoc.documentToString(jogar).replaceAll("\\r\\n|\\r|\\n", "");
        os.println(msg);       
        return jogo;
    }

    /**
     * Obtém o elemento do documento XML que representa o método especificado.
     */
    private static Element getMethod(final Document doc, final String Method) throws Exception {
        NodeList items = doc.getElementsByTagName(Method);
        if (items.getLength() != 1) {
            throw new Exception("Erro de lógica: espera método '" + Method + "'!");
        }
        return ((Element) doc.getElementsByTagName(Method).item(0));
    }
    
    /**
     * Adiciona texto a um ficheiro de log.
     */
    private static void adicionarStringFicheiro(String ficheiro, String texto) throws IOException {
        try (PrintWriter escritor = new PrintWriter(new BufferedWriter(new FileWriter(ficheiro, true)))) {
            escritor.println(texto);
        }
    }
    
    /**
     * Regista eventos no log do protocolo.
     */
    private static void registaLog(String evento) throws IOException {
        adicionarStringFicheiro(XMLDoc.getContexto() + "protocolo.log", LocalDateTime.now() + " - " + evento.replaceAll("\n", ""));
    }
}