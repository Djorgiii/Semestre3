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
 * Converte mensagens em XML para acções/objetos e vice-versa.
 * 
 * @author Engº Porfírio Filipe
 */
public class Skeleton {
    // **Métodos:**

    /**
     * Valida se o documento XML recebido representa um método válido.
     *
     * @param doc Documento XML a ser validado.
     * @throws Exception Se o documento não for válido.
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
    	String line=is.readLine();
    	registaLog("Servidor{"+line+"}");
        Document d = XMLDoc.parseString(line); 
        // Valida o schema XSD da mensagem.
        validXSD(d);
        return d;
    }
	/**
	 * Método que atende a chamada Iniciar.
	 * @param sk 		Circuto virtual estabelecido com o jogador
	 * @param simbolo 	Simbolo do jogador
	 * @throws Exception 	Em caso de erro
	 */
    public static void runIniciar(Socket sk, char simbolo) throws Exception {
	// estes streamms não podem ser fechados porque fecham o socket
	// Stream para ler dados do socket.
	BufferedReader is = new BufferedReader(new InputStreamReader(sk.getInputStream()));
	// Stream para escrever dados no socket.
	PrintWriter os = new PrintWriter(sk.getOutputStream(), true);
        System.out.println("   Jogador '"+simbolo+"': "+sk);
        Document x = getNext(is);
        // Trata o jogador
        // Extrai o nome e senha do jogador X.
        String Nome 	= 	getMethod(x,"iniciar").getAttribute("nickname");
        String Senha 	= 	getMethod(x,"iniciar").getAttribute("senha");
        System.out.println("   Jogador '"+simbolo+"': " + Nome + "/" + Senha);
        User jg = User._authenticate(Nome, Senha);
        if(jg == null)
            throw new Exception("Falhou a autenticação do utilizador '"+Nome+"'!");
        System.out.println("Autenticação/login do utilizador '"+Nome+"' realizado com sucesso!");
        Document d=XMLDoc.parseString(jg.toXMLString(simbolo));
        Node jogador = d.getElementsByTagName("jogador").item(0);
        Node cloneElement = x.importNode(jogador, true);
        x.getElementsByTagName("iniciar").item(0).appendChild(cloneElement);
        // Envia a mensagem de "iniciar" para o jogador, 
        // com o seu símbolo que confirma o login bem sucedido.
        String msg=XMLDoc.documentToString(x);
        os.println(msg);
    }
    
	/**
	 * Método que atende a chamada Obter.
	 * @param is 			entrada
	 * @param os 			saida
	 * @param simbolo 		simbolo do jogador
	 * @param sk 			dados da ligação para efeitos de registo
	 * @param jogo 			representa o jogo atual
	 * @throws Exception 	em caso de erro
	 */
    public static void runObter(BufferedReader is, PrintWriter os, char simbolo, Socket sk, JogoXML jogo) throws Exception {
        Document obter = getNext(is);
        
        // Verifica a existencia do elemento "obter" na mensagem.
        getMethod(obter, "obter");

        // Envia a mensagem "obter" para o jogador, 
        // com o tabuleiro atualizado indicando o estado atual do jogo.
        os.println("<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>");
    }
	/**
	 * Método que atende a chamada Jogar.
	 * @param is 			entrada
	 * @param os 			saida
	 * @param simbolo 		simbolo do jogador
	 * @param sk 			dados da ligação para efeitos de registo
	 * @param jogo 			representa o jogo atual
	 * @return 				o jogo atualizado
	 * @throws Exception 	em caso de erro
	 */
    public static JogoXML runJogar(BufferedReader is, PrintWriter os, char simbolo, Socket sk, JogoXML jogo) throws Exception {
        Document jogar = getNext(is);

        // Obtém o elemento "jogar" da mensagem.
        Element jogada = getMethod(jogar, "jogar");

        // Extrai a jogada do jogador.
        String jogadaStr = jogada.getAttribute("jogada");
        short jogadaNum = Short.parseShort(jogadaStr);

        // Concretiza a jogada.
        jogo.joga(jogadaNum, simbolo);

        // Envia a mesma mensagem recebida como resposta "jogar" para o jogador.
        os.println(XMLDoc.documentToString(jogar));   	
    	return jogo;
    }
    /**
     * Obtém o elemento do documento XML que representa o método especificado.
     *
     * @param doc 			Documento XML.
     * @param Method 		Nome do método a ser obtido.
     * @throws Exception 	Se o método não for encontrado no documento.
     * @return 				Elemento XML que representa o método.
     */
    private static Element getMethod(final Document doc, final String Method) throws Exception {
        NodeList items = doc.getElementsByTagName(Method);
        if (items.getLength() != 1) {
            throw new Exception("Erro de lógica: espera método '" + Method + "'!");
        }
        return ((Element) doc.getElementsByTagName(Method).item(0));
    }
    
    /**
     * @param ficheiro de destino
     * @param texto	a adicionar o ficheiro
     * @throws IOException se houver erro
     */
    private static void adicionarStringFicheiro(String ficheiro, String texto) throws IOException {
        try (
            // Cria um PrintWriter para escrever no ficheiro.
            PrintWriter escritor = new PrintWriter(new BufferedWriter(new FileWriter(ficheiro, true)))) {

            // Escreve a string no ficheiro.
            escritor.println(texto);
        }
    }
    
    /**
     * @param evento 		mensagem a resgistar
     * @throws IOException	em caso de erro
     */
    private static void registaLog(String evento) throws IOException {
    	adicionarStringFicheiro(XMLDoc.getContexto()+"protocolo.log", LocalDateTime.now()+" - "+evento.replaceAll("\n",""));
    }
}
