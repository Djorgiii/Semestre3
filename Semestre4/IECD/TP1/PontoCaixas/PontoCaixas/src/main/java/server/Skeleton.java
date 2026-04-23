package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
     * Método que atende a chamada Iniciar.
     */
    public static void runIniciar(Socket sk, char simbolo) throws Exception {
        // Estes streams não podem ser fechados porque fecham o socket
        BufferedReader is = new BufferedReader(new InputStreamReader(sk.getInputStream()));
        PrintWriter os = new PrintWriter(sk.getOutputStream(), true);
        
        System.out.println("   Jogador '" + simbolo + "': " + sk);
        Document x = getNext(is);
        
        // Extrai o nome e senha do jogador.
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
        
        // Envia a mensagem de "iniciar" para o jogador, 
        // com o seu símbolo que confirma o login bem sucedido.
        String msg = XMLDoc.documentToString(x);
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

        // Obtém o elemento "jogar" da mensagem.
        Element jogadaEl = getMethod(jogar, "jogar");

        // Extrai a jogada do jogador em formato String (ex: "1 1 1 2")
        String jogadaStr = jogadaEl.getAttribute("jogada");
        String[] partes = jogadaStr.trim().split("\\s+");

        try {
            if (partes.length == 4) {
                int[] coords = new int[4];
                for (int i = 0; i < 4; i++) {
                    coords[i] = Integer.parseInt(partes[i]);
                }
                
                // Concretiza a jogada no novo sistema de Pontos e Caixas
                // NOTA: O teu JogoXML precisa de estar atualizado para aceitar int[]
                jogo.joga(coords, simbolo);
                
            } else {
                throw new Exception("Formato de coordenadas inválido.");
            }
        } catch (Exception e) {
            System.err.println("Erro na jogada do Skeleton: " + e.getMessage());
            // Define o estado como Inválido se houver erro (requer que tenhas um setter no JogoXML)
            // jogo.setEstado("IV"); 
        }

        // Envia a mesma mensagem recebida como resposta "jogar" para o jogador.
        os.println(XMLDoc.documentToString(jogar));       
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