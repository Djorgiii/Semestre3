package client;

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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.MyImage;
import util.XMLDoc;

/**
 * Classe que implementa a adaptação do cliente/jogador ao protocolo. 
 * Suporta a interação do cliente com o servidor, 
 * Converte mensagens em XML para acções/objetos e vice-versa.
 * 
 * @author Engº Porfírio Filipe
 */
public class Stub implements AutoCloseable {

	// **Atributos:**

	// Stream para ler dados do socket.
	private BufferedReader is = null;
	// Stream para escrever dados no socket.
	private PrintWriter os = null;
	// dados do jogador atual, conhecido após autenticação
        private Document registo=null;

	// **Construtor:**

	/**
	 * Construtor que recebe um socket e inicia os streams de entrada e saída.
	 *
	 * @param sk Socket 	Ligação ao servidor.
	 * @throws IOException 	Se ocorrer um erro ao iniciar os streams.
	 */
	public Stub(Socket sk) throws IOException {
		// Cria um BufferedReader para ler do socket.
		is = new BufferedReader(new InputStreamReader(sk.getInputStream()));
		// Cria um PrintWriter para escrever no socket.
		os = new PrintWriter(sk.getOutputStream(), true);
	}
	
	/**
	 * Fecha a comunicação com o servidor
	 */
	@Override
	public void close() {
		// Fecha o BufferedReader.
		try {
			is.close();
		} catch (IOException e) {// ignora erro
		}
		// Fecha o PrintWriter.
		os.close();
	}

	/**
	 * 📝 Converte o tabuleiro do jogo em XML para uma string formatada para ser
	 * apresentada na consola.
	 * Usa ✖️ e ⭕ para as jogadas e números para as casas vazias.
	 * @param tabuleiro O elemento XML <tabuleiro> que contém as <casa>
	 * @return String formatada para exibição na consola
	 */
	public String tabuleiroToTXT(final Element tabuleiro) {
	    // 🧱 Usamos StringBuilder para uma construção de String mais eficiente e rápida
	    StringBuilder sb = new StringBuilder();
	    
	    // 🗂️ Obtém a lista de todos os nós <casa> dentro do elemento tabuleiro
	    NodeList casas = tabuleiro.getElementsByTagName("casa");
	    
	    int contador = 1; // 📍 Contador para identificar as quadrículas de 1 a 9

	    // 🔄 Ciclo para percorrer as 3 linhas do tabuleiro
	    for (int i = 0; i < 3; i++) {
	        // 🔄 Ciclo para percorrer as 3 colunas de cada linha
	        for (int j = 0; j < 3; j++) {
	            
	            // 🔍 Extrai o atributo "simbolo" da casa correspondente ao índice atual
	            // O índice na NodeList é (contador - 1) pois a lista começa em 0
	            String attr = ((Element) casas.item(contador - 1)).getAttribute("simbolo");
	            char simbolo = (attr.length() > 0) ? attr.charAt(0) : ' ';

	            // 🎨 Lógica de conversão visual para emojis pesados
	            if (simbolo == 'X') {
	                sb.append(" ✖️ "); // Estilo semelhante ao emoji de multiplicação
	            } else if (simbolo == 'O') {
	                sb.append(" ⭕ "); // Círculo vermelho oco para contraste
	            } else {
	                // 🔢 Se a casa estiver vazia, mostra o seu número (1 a 9)
	                // Os espaços extra garantem que o alinhamento se mantém com os emojis
	                sb.append(" ").append(contador).append("  ");
	            }

	            // 📏 Adiciona a barra vertical separadora, exceto na última coluna
	            if (j < 2) {
	                sb.append("|");
	            }
	            
	            contador++; // Próxima casa...
	        }
	        
	        sb.append("\n"); // 📑 Salto de linha após completar uma fila
	        
	        // ➖ Adiciona a linha horizontal separadora entre as filas
	        if (i < 2) {
	            sb.append("------------\n");
	        }
	    }
	    
	    // ✨ Retorna o tabuleiro finalizado pronto a ser impresso
	    return sb.toString();
	}

	/**
	 * Converte o estado do jogo (VO, VX, EM, IV e ND) para uma string 
	 * que representa a mensagem a ser apresentada ao jogador.
	 *
	 * @param  valor contem a abreviatura do estado do jogo.
	 * @return String com a mensagem a ser apresentada ao jogador.
	 */
	public String estadoToTXT(final String valor) {
		switch (valor) {
		case "VO":
			return "Vitória do O!";
		case "VX":
			return "Vitória do X!";
		case "EM":
			return "Empate.";
		case "IV":
			return "Jogada inválida!";
		default:
			return ""; //nada a registar
		}
	}
	
    /**
     * Valida se o documento XML recebido representa um método válido.
     *
     * @param doc Documento XML a ser validado.
     * @throws Exception Se o documento não for válido.
     */
    private void validXSD(final Document doc) throws Exception {
        try {
            // Valida o documento contra o XSD "metodos.xsd".
            XMLDoc.validDocXSD(doc, XMLDoc.getContexto() + "metodos-cli.xsd");
        } catch (SAXException | IOException e) {
            throw new Exception("Recebeu mensagem inválida: " + e.getLocalizedMessage());
        }
    }
    
    /**
     * @param ficheiro de destino
     * @param texto	a adicionar o ficheiro
     * @throws IOException se houver erro
     */
    private static void adicionarStringFicheiro(String ficheiro, String texto) {
        try (
            // Cria um PrintWriter para escrever no ficheiro.
            PrintWriter escritor = new PrintWriter(new BufferedWriter(new FileWriter(ficheiro, true)))) {

            // Escreve a string no ficheiro.
            escritor.println(texto);
        } catch (IOException e) {
            // Ignora o erro, pode haver divergencia no caminho relativo/absoluto do ficheiro
	}
    }
    
    /**
     * @param evento 		mensagem a resgistar
     * @throws IOException	em caso de erro
     */
    private static void registaLog(String evento) {
	adicionarStringFicheiro(XMLDoc.getContexto()+"protocolo.log", LocalDateTime.now()+" - "+evento.replaceAll("\n",""));
    }
    
    private Element getElement(String tag) {
	NodeList itens = registo.getElementsByTagName(tag);
	if(itens.getLength()==1)
	    return (Element)itens.item(0);
	return null;
    }
    
    private String getText(String tag) {
	Element el = getElement(tag);
	if(el==null)
	    return "";
	else
	    return el.getTextContent();
    }
    /**
     * Mostra todos os dados do jogador autenticado
     */
    public void print() {
	if(registo==null) {
		System.out.println("Não existe jogador autenticado!");
		return;
	}
	System.out.println("------------ Jogador '"+getElement("jogador").getAttribute("simbolo")+"' ------------");
	System.out.println("Identificador (UUID): " + getText("userid"));
	System.out.println("Última atualização em: " + getText("updated"));
	System.out.println("Perfil: " + getText("profile"));
	System.out.println("Nome de utilizador: " + getText("username"));
	System.out.println("Nome completo: " + getText("full-name"));
	String email=getText("email");
	String gender=getText("gender");
	String birthdate=getText("birthdate");
	String nationality=getText("pt-nationality");
	if(gender.equals("M"))
	    nationality=getText("pt-male");
	else if(gender.equals("F"))
	    nationality=getText("pt-female");
	String flag=getText("flag");
	String photography=getText("photography");
	
	if (!email.isBlank())
	    System.out.println("Endereço de email: " + getText("email"));
	if (!gender.isBlank())
	    System.out.println("Género: " + gender);
	if (!birthdate.isBlank()) {
	    System.out.println("Data de nascimento: " + birthdate);
	    System.out.println("Idade: " + getText("age"));}
	if (!nationality.isBlank()) 
	    System.out.println("Nacionalidade: " + nationality);
	System.out.println("--------------------------------------------------------");
	
	if (!photography.isBlank()) {
	    MyImage p = new MyImage();
	    p.setBase64(photography);
	    try {
		p.view();
	    } catch (Exception e) {
		// e.printStackTrace();
	    }
	}
	if (!flag.isBlank()) {
	    MyImage f = new MyImage();
	    f.setBase64(flag);
	    try {
		f.view();
	    } catch (Exception e) {
		// e.printStackTrace();
	    }
	}
    }
	/**
	 * Realiza a autenticação do jogador no servidor.
	 *
	 * @param user Nome do jogador.
	 * @param pass Senha do jogador.
	 * @return Símbolo do jogador (O ou X).
	 * @throws Exception   Se ocorrer um erro durante a autenticação.
	 * @throws IOException Se ocorrer um erro de comunicação com o servidor.
	 */
	public char iniciar(final String user, final String pass) throws IOException, Exception {

		// **1. Enviar a mensagem de autenticação para o servidor:**
		// Envia a string de acordo com o formato indicado no XSD.
		os.println("<metodo><iniciar nickname='" + user 
					   + "' senha='" + pass + "'/></metodo>");

		// **2. Receber a resposta do servidor e analisar o XML:**
		// Recebe a resposta do servidor.
		String resposta = is.readLine();
		registaLog("Cliente{"+resposta+"}");
		if(resposta==null)
			throw new Exception("Ligação ao servidor cancelada remotamente!");
		// Processa a resposta como um documento XML.
		registo = XMLDoc.parseString(resposta);
		validXSD(registo);
		
		// **3. Obter o elemento "jogador" da resposta:**
		// Obtém a lista de elementos "jogador".
		NodeList jogadores = registo.getElementsByTagName("jogador");
	
		// **4. Vai retornar o símbolo do jogador:**
		// Obtem o conteúdo do atributo 'símbolo' do elemento "jogador".
		// Retorna o símbolo do jogador.
		return ((Element)jogadores.item(0)).getAttribute("simbolo").charAt(0);
	}

	/**
	 * Obtém o tabuleiro do jogo a partir do servidor.
	 *
	 * @return Elemento XML que representa o tabuleiro do jogo.
	 * @throws Exception   Se ocorrer um erro durante a comunicação com o servidor.
	 * @throws IOException Se ocorrer um erro de leitura do socket.
	 */
	public Element obter() throws IOException, Exception {
		// Envia a mensagem "obter" para o servidor.
		os.println("<metodo><obter/></metodo>");
		// Recebe a resposta do servidor e processa o XML.
		String resposta=is.readLine();
		registaLog("Cliente{"+resposta+"}");
		if(resposta==null)
			throw new Exception("Ligação ao servidor cancelada remotamente!");
		Document d = XMLDoc.parseString(resposta);
		validXSD(d);
		// Obtém o elemento "tabuleiro" da resposta.
		Element tabuleiro = (Element) d.getElementsByTagName("tabuleiro").item(0);
		// Retorna o elemento "tabuleiro".
		return tabuleiro;
	}

	/**
	 * Realiza uma jogada.
	 *
	 * @param numero Número da jogada (1 a 9).
	 * @throws IOException Se ocorrer um erro de comunicação com o servidor.
	 * @throws Exception   Se ocorrer um erro ao processar a resposta do servidor.
	 */
	public void jogar(final short numero) throws IOException, Exception {
		// Verifica se o número da casa do jogo é válido.
		if (numero<0 || numero>9) 
			throw new Exception("Número da casa do jogo inválido!");
		// Envia a mensagem com a jogada para o servidor.
		os.println("<metodo><jogar jogada='" + numero + "'/></metodo>");
		// Recebe a resposta do servidor e retorna o estado.
		String resposta=is.readLine();
		registaLog("Cliente{"+resposta+"}");
		if(resposta==null)
			throw new Exception("Ligação ao servidor cancelada remotamente!");
		Document d = XMLDoc.parseString(resposta);  // consome a linha!
		validXSD(d);
	}
}
