package util;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.*;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* load & save */
import org.w3c.dom.ls.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/* XML Transformation */
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.OutputKeys;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author Eng. Porfírio Filipe
 *
 */
/**
 * Classe para manipulação de documentos XML
 */
/**
 * 
 */
/**
 * 
 */
public class XMLDoc {

	/**
	 * Obtém uma string representando o conteúdo de um documento DOM.
	 * 
	 * Este método converte um documento DOM (Document Object Model) em uma string,
	 * permitindo a manipulação do conteúdo XML como texto.
	 *
	 * @param xmlDoc O documento DOM a ser convertido em string.
	 * @return A string que representa o conteúdo do documento DOM.
	 * @throws TransformerFactoryConfigurationError Se ocorrer um erro ao criar um
	 *                                              transformador.
	 * @throws TransformerException                 Se ocorrer um erro durante a
	 *                                              transformação do documento.
	 *
	 *  @example
	 *
	 *  Document documentoXml = ...; 
	 *  // Objeto Document, contendo o conteúdo XML String
	 *  conteudoXmlString = documentToString(documentoXml);
	 *  // Imprime o conteúdo do documento XML como string
	 *  System.out.println(conteudoXmlString);
	 *  
	 */
	public static final String documentToString(Document xmlDoc)
			throws TransformerFactoryConfigurationError, TransformerException {
		if (xmlDoc == null) {
			// Se o documento DOM for nulo, retorna nulo
			return null;
		}

		// Cria um StringWriter para armazenar a string resultante
		Writer out = new StringWriter();

		// Cria um Transformer para realizar a conversão do documento DOM para string
		Transformer tf = TransformerFactory.newInstance().newTransformer();

		tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		tf.setOutputProperty(OutputKeys.VERSION, "1.0");
		if (xmlDoc.getXmlEncoding() != null)
			tf.setOutputProperty(OutputKeys.ENCODING, xmlDoc.getXmlEncoding());
		else
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		tf.setOutputProperty(OutputKeys.INDENT, "no");
		tf.setOutputProperty("{http://util.apache.org/xslt}indent-amount", "2");

		// Realiza a transformação do documento DOM para a string
		tf.transform(new DOMSource(xmlDoc), new StreamResult(out));

		// Retorna a string contendo o conteúdo do documento DOM
		return out.toString();
	}

	/**
	 * Gera um nome de ficheiro de backup no formato "caminho_dataHoraAtual.bak".
	 * 
	 * @param caminho O caminho base para o ficheiro de backup.
	 * @return O nome completo do ficheiro de backup.
	 */
	public static String gerarNomeFBackupInstant(String caminho) {
		// Obtém a data e hora atuais como um objeto Instant.
		Instant dataHoraAtual = Instant.now();

		// Converte o Instant para um objeto ZonedDateTime na zona horária padrão do
		// sistema.
		ZonedDateTime dataHoraLocal = dataHoraAtual.atZone(ZoneId.systemDefault());

		// Extrai os componentes de data e hora do ZonedDateTime.
		int ano = dataHoraLocal.getYear();
		int mes = dataHoraLocal.getMonthValue();
		int dia = dataHoraLocal.getDayOfMonth();
		int hora = dataHoraLocal.getHour();
		int minuto = dataHoraLocal.getMinute();
		int segundo = dataHoraLocal.getSecond();
		int milissegundo = dataHoraLocal.getNano() / 1_000_000;

		// podia ser usado System.currentTimeMillis() mas assim podemos ir até aos nano
		// segundos
		// Constrói o nome do ficheiro de backup usando formatação de string.
		return String.format("%s-%04d%02d%02d%02d%02d%02d%03d."+obterExtensaoFicheiro(caminho), removerExtensao(caminho), ano, mes, dia, hora,
				minuto, segundo, milissegundo);
	}

	/**
	 * Gera o nome do ficheiro de backup em conformidade com a versão existente
	 * 
	 * @param nomeFicheiro nome do ficheiro original
	 * @return nome do ficheiro para fazer backup
	 */
	public static String gerarNomeFBackupVersao(String nomeFicheiro) {
		// Obtém o número da versão do ficheiro mais recente
		int numeroVersaoFicheiroMaisRecente = obterNumeroVersao(nomeFicheiro);

		// Incrementa o número da versão para obter o número da versão seguinte
		int numeroVersaoSeguinte = numeroVersaoFicheiroMaisRecente + 1;

		// Gera o nome do ficheiro da versão seguinte
		String nomeFicheiroVersaoSeguinte = removerExtensao(nomeFicheiro) + "(" + numeroVersaoSeguinte + ")" + ".util";

		// Retorna o nome do ficheiro da versão seguinte
		return nomeFicheiroVersaoSeguinte;
	}

	/**
	 * Devolve a pasta de contexto do projeto corrente
	 * 
	 * @return o caminho de referência para a pasta que contem documentos util
	 */
	public static final String getContexto() {
		String contexto = "WebContent/";
		File f = new File(contexto);
		if (!(f.exists() && f.isDirectory())) {
			contexto = "src/main/webapp/";
		}
		return contexto;
	}
	
	
	/**
	 * @return o caminho absoluto da pasta atual
	 */
	public static final String getAbsPath() {
	    File file = new File(".");
	    String absolutePath = file.getAbsolutePath();
	    // remove o ponto
	    absolutePath = absolutePath.substring(0, absolutePath.length() - 1); 
	    // Definir a diretoria de trabalho no ambito do projeto eclipse
	    String workingDir =  absolutePath + File.separator; 
	    return workingDir;
	}

	/**
	 * Devolve uma lista de nós obtida pela expressão xPath indicada
	 * 
	 * @param expression xpath
	 * @param doc        documento XML
	 * @return lista de nós
	 * @throws XPathExpressionException em caso de erro
	 */
	public static final NodeList getXPath(final String expression, final Document doc) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes;
		nodes = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
		return nodes;
	}

	/**
	 * Devolve um inteiro obtido pela expressão xPath indicada.
	 *
	 * @param expression expressão XPath a ser avaliada.
	 * @param doc        documento onde a expressão será aplicada.
	 * @return resultado da avaliação da expressão XPath como um numero.
	 * @throws XPathExpressionException em caso de erro
	 */
	public static final int getXPathN(final String expression, final Document doc) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		return ((Double) xpath.evaluate(expression, doc, XPathConstants.NUMBER)).intValue();
	}

	/**
	 * Executa uma expressão XPath numa árvore DOM e devolve o primeiro valor
	 * (string) encontrado.
	 *
	 * @param expression expressão XPath a ser avaliada.
	 * @param doc        documento onde a expressão será aplicada.
	 * @return valor do primeiro nó encontrado pela expressão XPath, ou `null` se
	 *         nenhum nó for encontrado.
	 *
	 * @throws XPathExpressionException Se a expressão XPath for inválida.
	 *
	 * @example
	 *
	 *    String expression = "//author/name";
	 *    Document doc = ...; 	// Obter a árvore DOM
	 *    String authorName = getXPathV(expression, doc);
	 *
	 *    if (authorName != null) 
	 *       System.out.println("Nome do autor: " + authorName);  
	 *    else 
	 *       System.out.println("Autor não encontrado.");
	 */
	public static String getXPathV(final String expression, final Document doc) throws XPathExpressionException {

		// Obter o objeto XPathFactory
		XPathFactory factory = XPathFactory.newInstance();

		// Criar um objeto XPathExpression
		XPathExpression xpathExpression = factory.newXPath().compile(expression);

		// Obter o objeto NodeList com os resultados da expressão
		NodeList nodes = (NodeList) xpathExpression.evaluate(doc, XPathConstants.NODESET);

		// Se nenhum nó for encontrado, retornar null
		if (nodes.getLength() == 0) {
			return null;
		}

		// Retornar o valor do primeiro nó
		return nodes.item(0).getNodeValue();
	}

	/**
	 * Escreve uma árvore DOM em um ficheiro de forma exclusiva.
	 * 
	 * Este método garante que apenas uma única instância do código escreve no
	 * ficheiro ao mesmo tempo, evitando a perda de dados em caso de falhas ou
	 * interrupções.
	 *
	 * @param documento        A árvore DOM a ser escrita no ficheiro.
	 * @param ficheiroOriginal O caminho completo para o ficheiro original.
	 * @param ficheiroBackup   O caminho completo para o ficheiro de backup.
	 * @throws TransformerFactoryConfigurationError em caso de erro
	 * @throws IOException                          Se ocorrer um erro ao criar ou
	 *                                              escrever no ficheiro.
	 * @throws TransformerException                 Se ocorrer um erro durante a
	 *                                              conversão da árvore DOM para string.
     *
	 *
	 * @example
	 * Document documentoXml = ...; //objeto Document, contendo a árvore DOM String
	 * ficheiroOriginal = "meu_ficheiro.xml"; 
	 * String ficheiroBackup = "meu_ficheiro_backup.xml";
	 * gravarExclusivo(documentoXml, ficheiroOriginal, ficheiroBackup);
	 * // O conteúdo da árvore DOM será escrito em "meu_ficheiro.xml" de forma segura e exclusiva.
	 */
	public synchronized static void gravarLock(Document documento, String ficheiroOriginal, String ficheiroBackup)
			throws TransformerFactoryConfigurationError, TransformerException, IOException {

		renomear(ficheiroOriginal, ficheiroBackup);

		String stXML = documentToString(documento);

		// Abre o canal do ficheiro original para escrita
		FileChannel fileChannel = FileChannel.open(Paths.get(ficheiroOriginal), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE);

		// Bloqueia o canal para escrita exclusiva
		try (FileLock lock = fileChannel.lock()) { // faz unlock automaticamente

			// Cria um buffer para armazenar a string como bytes
			ByteBuffer byteBuffer = ByteBuffer.wrap(stXML.getBytes());

			// Escreve o buffer no canal do ficheiro
			while (byteBuffer.hasRemaining())
				fileChannel.write(byteBuffer);

		} finally {
			// Fecha o canal do ficheiro
			fileChannel.close();
		}
	}

	/**
	 * Escreve arvore DOM num ficheiro com synchronized
	 * 
	 * @param documento          arvóre DOM
	 * @param origem             ficheiro original
	 * @param nomeFicheiroBackup ficheiro onde vai ser gravado o backup
	 * @throws Exception em caso de erro
	 */
	public synchronized static void gravarSinc(Document documento, String origem, String nomeFicheiroBackup)
			throws Exception {
		renomear(origem, nomeFicheiroBackup);
		// Grava o novo ficheiro XML
		writeDocument(documento, origem);
	}

	/**
	 * This method lists valid XML files within a specified folder based on an XSD
	 * schema file.
	 *
	 * @param pasta   The path to the folder containing the XML files.
	 * @param xsdFile The path to the XSD schema file used for validation.
	 * @return An ArrayList containing the names of valid XML files.
	 *
	 * @example String poemsFolder = "C:/poemas"; String poemaXSD = "poema.xsd";
	 *          ArrayList<String> validPoems = ListarDocumentos(poemsFolder,
	 *          poemaXSD);
	 *
	 *          System.out.println("Valid poems:"); for (String poem : validPoems) {
	 *          System.out.println(poem); }
	 */
	public static final ArrayList<String> listarDocumentos(String pasta, String xsdFile) {
		ArrayList<String> result = new ArrayList<String>(); // Create an ArrayList of filenames

		// Get the folder object
		File folder = new File(pasta);

		// List all files in the folder
		File[] listOfFiles = folder.listFiles();

		// Iterate through each file
		for (int i = 0; i < listOfFiles.length; i++) {
			// Check if it's a file and ends with ".util" (case-insensitive)
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().toLowerCase().endsWith(".util")) {
				// Check if the file is valid against the XSD schema
				try {
					validDocXSD(pasta + listOfFiles[i].getName(), xsdFile);
					// Add the valid file name to the result list
					result.add(listOfFiles[i].getName());
				} catch (SAXException | IOException e) {
					System.out.println("(listarDocumentos) Falhou a validação do ficheiro: '" + pasta
							+ listOfFiles[i].getName() + "'");
					System.out.println(e.getLocalizedMessage());
				}
			}
		}

		return result;
	}

	/**
	 * Este método lista os ficheiros em uma pasta e retorna um documento XML com os
	 * nomes dos ficheiros.
	 *
	 * @param pasta O caminho para a pasta que você deseja listar.
	 * @return Um documento XML com os nomes dos ficheiros na pasta.
	 *
	 * @example
	 * String pasta = "/home/usuario/pasta"; 
	 * String listaFicheiros = ListarFicheiros(pasta); 
	 * System.out.println(listaFicheiros);
	 * 
	 */
	public static final String listarFicheiros(String pasta) {

		// **Cria a string inicial do documento XML**
		String result = "<?util version='1.0' encoding='UTF-8' standalone='yes'?>\n";
		result = result + "<ficheiros>\n";

		// **Obtém um objeto File para a pasta especificada**
		File folder = new File(pasta);

		// **Obtém uma lista de ficheiros na pasta**
		File[] listOfFiles = folder.listFiles();

		// **Iterar sobre a lista de ficheiros**
		for (int i = 0; i < listOfFiles.length; i++) {

			// **Verifica se o elemento atual é um ficheiro**
			if (listOfFiles[i].isFile()) {

				// **Adiciona o nome do ficheiro ao documento XML**
				result = result + "<ficheiro>" + listOfFiles[i].getName() + "</ficheiro>\n";
			}
		}

		// **Fecha o documento XML**
		return result + "</ficheiros>\n";
	}

	/**
	 * Para teste e demonstração do funcionamento desta classe
	 * 
	 * @param args não usado
	 */
	public static void main(String[] args) {
		demo1();
	}

	/**
	 * Obtém o nome do ficheiro sem a extensão e sem o caminho.
	 *
	 * @param caminhoFicheiro O caminho completo do ficheiro.
	 * @return O nome do ficheiro sem a extensão e sem o caminho.
	 *
	 * @example String caminhoFicheiro = "/home/usuario/Documentos/meu_ficheiro.txt"; 
	 * 			String nomeFicheiro = obterNomeFicheiro(caminhoFicheiro);
	 * 			// Imprime "meu_ficheiro"
	 *          System.out.println(nomeFicheiro); 
	 */
	public static String obterNomeFicheiro(String caminhoFicheiro) {
		return removerExtensao(removerCaminho(caminhoFicheiro));
	}
	
	/**
	 * Obtém a extensão do ficheiro.
	 *
	 * @param caminhoFicheiro 	O caminho completo do ficheiro.
	 * @return 					A extensão do ficheiro.
	 *
	 * @Exemplo String caminhoFicheiro =
	 *          "/home/usuario/Documentos/meu_ficheiro.txt"; 
	 *          // Imprime "txt"
	 *          System.out.println(obterExtensao(caminhoFicheiro)); 
	 */
	public static String obterExtensaoFicheiro(String caminhoFicheiro) {

		// Verifica se o caminho do ficheiro é nulo ou vazio
		if (caminhoFicheiro == null || caminhoFicheiro.isEmpty()) 
			return "";

		// Obtém o índice da última ocorrência do ponto no caminho do ficheiro
		int indicePonto = caminhoFicheiro.lastIndexOf(".");

		// Se não existir ponto no caminho do ficheiro, retorna o próprio caminho
		if (indicePonto == -1) 
			return caminhoFicheiro;

		// Retorna o nome do ficheiro sem a extensão
		return caminhoFicheiro.substring(indicePonto+1);
	}
	/**
	 * Obtém o número da versão de um ficheiro.
	 *
	 * @param nomeFicheiro nome do ficheiro a saber a versão
	 * @return o número da versão do ficheiro, ou 0 se não for possível determinar a
	 *         versão
	 *
	 * @example
	 * 
	 *         int numeroVersao = ObterNumeroVersao.obterNumeroVersao("meu_ficheiro.xml"); 
	 *         if (numeroVersao > 0) 
	 *            	System.out.println("A versão do ficheiro é: " + numeroVersao); 
	 *         else 
	 *         	System.out.println("Não foi possível determinar a versão do ficheiro."); 
	 */
	public static int obterNumeroVersao(String nomeFicheiro) {
		// Obtém a lista de versões do ficheiro
		ArrayList<String> listaVersoes = obterListaVersoes(nomeFicheiro);
		// Se a lista de versões estiver vazia, retorna 0
		if (listaVersoes.isEmpty()) {
			return 0;
		}

		// Ordena a lista de versões por ordem decrescente
		Collections.sort(listaVersoes, Collections.reverseOrder());

		// Obtém o nome do ficheiro da versão mais recente
		String nomeFicheiroVersaoMaisRecente = listaVersoes.get(0);

		// Extrai o número da versão do nome do ficheiro
		String regex = "\\((\\d+)\\)";
		Matcher matcher = Pattern.compile(regex).matcher(nomeFicheiroVersaoMaisRecente);

		// Se um número for encontrado, retorna-o
		if (matcher.find())
			return Integer.parseInt(matcher.group(1));

		// Se nenhum número for encontrado, retorna 1

		return 0;
	}

	/**
	 * Parses XML file and returns XML document.
	 * 
	 * @param fileName XML file to parse
	 * @return XML document or null if error occured
	 */
	public static final Document parseFile(final String fileName) {
		DocumentBuilder docBuilder;
		Document doc = null;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("Wrong parser configuration: " + e.getMessage());
			return null;
		}
		File sourceFile = new File(fileName);
		try {
			doc = docBuilder.parse(sourceFile);
		} catch (SAXException e) {
			System.out.println("Wrong XML file structure: " + e.getLocalizedMessage());
			return null;
		} catch (IOException e) {
			System.out.println("Could not read source file: " + e.getLocalizedMessage());
		}
		return doc;
	}

	/**
	 * Método para converter String XML em Document
	 * 
	 * @param xmlStr string com XML
	 * @return o documento XML
	 * @throws Exception em caso de erro
	 */
	public static Document parseString(String xmlStr) throws Exception {

		// Cria uma nova instância de DocumentBuilderFactory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// Cria um novo DocumentBuilder a partir da fábrica
		DocumentBuilder builder = factory.newDocumentBuilder();

		// Cria um InputSource a partir da String XML
		InputSource is = new InputSource(new StringReader(xmlStr));

		// Analisa o InputSource e retorna um objeto Document
		return builder.parse(is);
	}

	/**
	 * Este método imprime um documento XML de forma formatada.
	 *
	 * @param xml 	O documento XML a ser formatado.
	 * 
	 * @example 
	 * 		Document meuDocumentoXML = ...;
	 *      	// Obter o documento XML de alguma forma
	 *     		prettyPrint(meuDocumentoXML);
	 */
	public static final void prettyPrint(Document xml) {
		// Criar um Transformer para formatar o XML
		Transformer tf;
		try {
			tf = TransformerFactory.newInstance().newTransformer();
			// Configurar a codificação de saída como UTF-8 e ativar a indentação
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");

			// Transformar o documento em uma String formatada
			Writer out = new StringWriter();
			tf.transform(new DOMSource(xml), new StreamResult(out));

			// Imprimir a String formatada no console
			System.out.println(out.toString());
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove o caminho do ficheiro, deixando apenas o nome do ficheiro com a extensão.
	 *
	 * @param caminhoFicheiro O caminho completo do ficheiro.
	 * @return O nome do ficheiro com a extensão.
	 *
	 * @example 
	 * 		String caminhoFicheiro = "/home/user/Documentos/meu_ficheiro.txt"; 
	 * 		String nomeFicheiroComExtensao = removerCaminho(caminhoFicheiro);
	 *      System.out.println(nomeFicheiroComExtensao); // Imprime "meu_ficheiro.txt"
	 */
	public static String removerCaminho(String caminhoFicheiro) {

		// Verifica se o caminho do ficheiro é nulo ou vazio
		if (caminhoFicheiro == null || caminhoFicheiro.isEmpty()) {
			return "";
		}

		// Obtém o nome do ficheiro com a extensão
		String nomeFicheiroComExtensao = new File(caminhoFicheiro).getName();

		// Retorna o nome do ficheiro com a extensão
		return nomeFicheiroComExtensao;
	}

	/**
	 * Este método remove caracteres que não estão no alfabeto de uma string.
	 *
	 * @param palavra A string da qual os caracteres inválidos serão removidos.
	 * @return A string com os caracteres inválidos removidos.
	 *
	 * @example
	 *
	 *         String frase = "Olá, mundo!"; 
	 *         String fraseAlfa = removerNaoAlfa(palavraComCaracteresInvalidos);
	 *         System.out.println(removerNaoAlfa(frase)); // Imprime "Olá mundo!"
	 */
	public static String removerNaoAlfa(String palavra) {

		// Expressão regular que define o alfabeto
		String regex = "[^a-zA-ZÁÀÃÂÉÈÍÌÒÓÕÔÙÚáàãâäëéèêíìîóòõôúùç\\s]";

		// Substitui todos os caracteres que não estão no alfabeto por uma string vazia
		return palavra.replaceAll(regex, "");
	}

	/**
	 * Remove a extensão do nome do ficheiro.
	 *
	 * @param caminhoFicheiro O caminho completo do ficheiro ou apenas o nome do
	 *                        ficheiro com a extensão.
	 * @return O nome do ficheiro sem a extensão.
	 *
	 * @example String caminhoFicheiro = "meu_ficheiro.txt"; String
	 *          nomeFicheiroSemExtensao = removerExtensao(caminhoFicheiro);
	 *          System.out.println(nomeFicheiroSemExtensao);  // Imprime "meu_ficheiro"
	 */
	public static String removerExtensao(String caminhoFicheiro) {

		// Verifica se o caminho do ficheiro é nulo ou vazio
		if (caminhoFicheiro == null || caminhoFicheiro.isEmpty()) {
			return "";
		}

		// Obtém o índice da última ocorrência do ponto no caminho do ficheiro
		int indicePonto = caminhoFicheiro.lastIndexOf(".");

		// Se não existir ponto no caminho do ficheiro, retorna o próprio caminho
		if (indicePonto == -1) {
			return caminhoFicheiro;
		}

		// Retorna o nome do ficheiro sem a extensão
		return caminhoFicheiro.substring(0, indicePonto);
	}

	/**
	 * @param xml documento util com a referência ao xsd
	 */
	public static final void removerXSD(Document xml) {
		Element raiz = xml.getDocumentElement();
		// não funciona:
		// raiz.removeAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation");
		// solução dependente de usar 'xsi'
		raiz.removeAttribute("xsi:noNamespaceSchemaLocation");
	}

	/**
	 * Renomeia o ficheiro antigo para ficheiro novo
	 * 
	 * @param ficheiroAntigo ficheiro antigo
	 * @param ficheiroNovo   ficheiro novo
	 */
	public static void renomear(String ficheiroAntigo, String ficheiroNovo) {
		File ficheiroBack = new File(ficheiroNovo);
		/*
		 * if (ficheiroNovo.exists()) // não se espera que exista 
		 * 		ficheiroNovo.delete();
		 */

		File original = new File(ficheiroAntigo);
		if (original.exists())
			original.renameTo(ficheiroBack);
	}

	/**
	 * Este método seria uma árvore DOM para um ficheiro especificado. 
	 * 
	 * Demonstra a funcionalidade de salvar uma árvore DOM sendo uma alternativa ao
	 * uso da transformação vazia.
	 *
	 * Seriação refere-se à conversão de um objeto em um fluxo de bytes que pode ser
	 * armazenado ou transmitido e posteriormente reconstruído em sua forma
	 * original.
	 *
	 * @param DOMtree        O objeto Document que representa a árvore DOM a ser
	 *                       seriada.
	 * @param targetFileName O nome do arquivo onde os dados seriados serão
	 *                       gravados.
	 *
	 * @throws java.io.IOException Se ocorrer um erro ao fechar o fluxo de saída do
	 *                             ficheiro.
	 *
	 *  @example
	 *  	 // Este código serializa a árvore DOM `document` 
	 *       // e grava no ficheiro chamado `meus_dados_seriados.xml`.
	 *
	 *     Document document = ...; // Objeto de árvore DOM
	 *     String targetFile = "meus_dados_seriados.xml";
	 *     StartSerialization(document, targetFile);
	 */
	public static final void seriaDocumento(final Document DOMtree, final String targetFileName) throws IOException {
		FileOutputStream FOS = null;
		DOMImplementationLS DOMiLS = null;

		// Testando o suporte para DOM Load and Save
		if ((DOMtree.getFeature("Core", "3.0") != null) && (DOMtree.getFeature("LS", "3.0") != null)) {
			DOMiLS = (DOMImplementationLS) (DOMtree.getImplementation()).getFeature("LS", "3.0");
			System.out.println("[Usando DOM Load and Save]");
		} else {
			System.out.println("[DOM Load and Save não suportado]");
			System.exit(0);
		}
		// Obter um objeto LSOutput
		LSOutput LSO = DOMiLS.createLSOutput();
		// LSO.setEncoding("UTF-16"); 	// Codificação padrão do Windows
		LSO.setEncoding("UTF-8"); 		// Codificação para português

		// Definindo o local para armazenar o resultado da seriação
		FOS = new FileOutputStream(targetFileName);
		LSO.setByteStream((OutputStream) FOS);
		// LSO.setByteStream(System.out); // Usar fluxo de saída para testes

		// Obter um objeto LSSerializer
		LSSerializer LSS = DOMiLS.createLSSerializer();

		// Realizar a seriação
		boolean ser = LSS.write(DOMtree, LSO);

		// Publicar o resultado
		if (ser)
			System.out.println("\n[Seriação concluída!]");
		else
			System.out.println("[Seriação falhou!]");

		FOS.close();
	}
	/**
	 * Serializa um documento XML para uma string.
	 * 
	 * Demonstra a funcionalidade de salvar uma árvore DOM sendo uma alternativa ao
	 * uso da transformação vazia.
	 *
	 * @param 	DOMtree O documento XML a ser seriado.
	 * @return 	Uma string que contém a seriação do documento XML.
	 * @throws IOException Se ocorrer um erro durante a serialização.
	 *
	 * @example
	 *
	 *		Document DOMtree = ... // Obter o documento DOM
	 *		String serDocument = seriaDocumento(DOMtree);
	 *		System.out.println(serDocument);
	 */
	public static final String seriaDocumento(final Document DOMtree) throws IOException {

	    // Criar um StringWriter para armazenar a saída
	    StringWriter stringWriter = new StringWriter();

	    // Criar um objeto DOMImplementationLS
	    DOMImplementationLS DOMiLS = (DOMImplementationLS) (DOMtree.getImplementation()).getFeature("LS", "3.0");

	    // Criar um objeto LSOutput que usa o StringBuilder como destino
	    LSOutput LSO = DOMiLS.createLSOutput();
	    LSO.setCharacterStream(stringWriter);
	    LSO.setEncoding("UTF-8");

	    // Criar um objeto LSSerializer
	    LSSerializer LSS = DOMiLS.createLSSerializer();

	    // Serializar o documento
	    LSS.write(DOMtree, LSO);

	    // Retornar a string do StringBuilder
	    return stringWriter.toString();
	}

	/**
	 * Lê um ficheiro de texto e retorna o seu conteúdo como uma string.
	 *
	 * Este método utiliza um Scanner para ler o ficheiro linha a linha e adicionar
	 * o conteúdo ao StringBuilder. O StringBuilder é então convertido para uma
	 * string e retornado.
	 *
	 * @param filename O caminho para o ficheiro a ser lido.
	 * @return Uma string que contém o conteúdo do ficheiro.
	 * @throws FileNotFoundException Se o ficheiro não for encontrado.
	 *
	 * @example
	 *
	 * 		String filename = "meu_ficheiro.txt"; 
	 * 		String content;
	 *
	 *      try { content = readFileToString(filename);
	 *            System.out.println("Conteúdo do ficheiro:");
	 *            System.out.println(content); 
	 *            } 
	 *      catch (FileNotFoundException e) {
	 *            System.out.println("Erro ao ler o ficheiro: " + e.getMessage()); 
	 *            }
	 */
	public static String stringFromFile(String filename) throws FileNotFoundException {
		// Cria um novo File para o ficheiro especificado
		File file = new File(filename);

		// Verifica se o ficheiro existe
		if (!file.exists()) {
			throw new FileNotFoundException("Ficheiro não encontrado: " + filename);
		}

		// Cria um novo StringBuilder para armazenar o conteúdo do ficheiro
		StringBuilder sb = new StringBuilder();

		// Cria um Scanner para ler o ficheiro
		Scanner scanner = new Scanner(file);

		// Lê cada linha do ficheiro e a adiciona ao StringBuilder
		while (scanner.hasNextLine()) {
			sb.append(scanner.nextLine()).append("\n");
		}

		// Fecha o scanner para liberar recursos
		scanner.close();

		// Retorna o conteúdo do ficheiro como uma string
		return sb.toString();
	}

	/**
	 * Salva uma string num ficheiro.
	 * 
	 * Este método cria um novo FileWriter para o ficheiro especificado e escreve a
	 * string no arquivo. O FileWriter é fechado após a escrita ser concluída para
	 * liberar recursos.
	 *
	 * @param str      A string a ser salva no arquivo.
	 * @param filename O nome do arquivo para salvar a string.
	 * @throws IOException Se ocorrer um erro ao criar o FileWriter ou escrever no ficheiro.
	 *
	 * @example
	 *
	 * 		String texto = "Este é o texto que será salvo no ficheiro."; 
	 * 		String nomeDoFicheiro = "meu_ficheiro.txt";
	 *      try { stringToFile(texto, nomeDoArquivo);
	 *            System.out.println("Ficheiro salvo com sucesso!"); 
	 *            } 
	 *      catch (IOException e) 
	 *      	{ System.out.println("Erro ao salvar o ficheiro: " + e.getMessage()); }
	 */
	public static void stringToFile(String str, String filename) throws IOException {
		// Cria um novo FileWriter para o ficheiro especificado
		FileWriter file = new FileWriter(filename);

		// Escreve o conteúdo no ficheiro
		file.write(str);

		// Fecha o FileWriter para liberar recursos
		file.close();
	}
	/**
	 * Calcula a diferença de tempo entre o início e agora.
	 * 
	 * @param inicio  data hora de início do período de tempo.
	 * @return string formatada que descreve o tempo que corresponde á diferença.
	 */
	
	public static final String tempoDif(LocalDateTime inicio) {
		return tempoDif(inicio,LocalDateTime.now());
	}

	/**
	 * Calcula a diferença de tempo entre duas datas e horas.
	 *
	 * @param inicio A data e hora de início.
	 * @param fim    A data e hora de fim.
	 * @return Uma string com a diferença de tempo formatada.
	 *
	 * @example
	 *         LocalDateTime inicio = LocalDateTime.of(2023, 12, 1, 10, 0, 0); 
	 *         LocalDateTime fim = LocalDateTime.of(2023, 12, 1, 12, 30, 0); 
	 *         String diferencaTempo = tempoDif(inicio, fim); 
	 *         System.out.println(diferencaTempo); // Imprime "Demorou: 2 Horas(2) Minutos(30)" 
	 */
	public static final String tempoDif(LocalDateTime inicio, LocalDateTime fim) {

		// **Calcula a diferença em milissegundos**
		long diferencaMili = Duration.between(inicio, fim).toMillis();

		// **Converte a diferença em milissegundos para dias, horas, minutos, segundos e
		// milissegundos**
		long dias = diferencaMili / (1000 * 60 * 60 * 24);
		long horas = (diferencaMili % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minutos = (diferencaMili % (1000 * 60 * 60)) / (1000 * 60);
		long segundos = (diferencaMili % (1000 * 60)) / 1000;
		long milissegundos = (diferencaMili % 1000);

		// **Retorna a string com a diferença de tempo formatada**
		return "Demorou: " + ((dias == 0) ? "" : "Dias(" + dias + ") ") + ((horas == 0) ? "" : "Horas(" + horas + ") ")
				+ ((minutos == 0) ? "" : "Minutos(" + minutos + ") ")
				+ ((segundos == 0) ? "" : "Segundos(" + segundos + ") ")
				+ ((milissegundos == 0) ? "" : "Milissegundos(" + milissegundos + ") ");

	}

	/**
	 * Esta função aplica uma transformação XSLT a um documento XML.
	 * 
	 * A transformação XSLT (Extensible Stylesheet Language Transformations) define
	 * como um documento XML de entrada pode ser transformado em um documento XML de
	 * saída.
	 *
	 * @param xml  O documento XML de entrada a ser transformado.
	 * @param xslt O documento XSLT que define a transformação.
	 * @return O documento XML resultante da transformação.
	 * @throws TransformerException         Lançada caso ocorra um erro durante a
	 *                                      transformação XSLT.
	 * @throws ParserConfigurationException Lançada caso haja um erro na
	 *                                      configuração do parser XML.
	 * @throws FactoryConfigurationError    Lançada caso haja um erro na criação da
	 *                                      fábrica TransformerFactory.
	 */
	public static Document transfDoc(Document xml, Document xslt)
			throws TransformerException, ParserConfigurationException, FactoryConfigurationError {

		// Cria fontes (sources) para o documento XML e o documento XSLT
		Source xmlSource = new DOMSource(xml);
		Source xsltSource = new DOMSource(xslt);

		// Cria um resultado (result) para armazenar o documento transformado
		DOMResult result = new DOMResult();

		// Cria uma fábrica de transformadores para suportar diferentes processadores
		// XSLT
		TransformerFactory transFact = TransformerFactory.newInstance();

		// Cria um transformador a partir do documento XSLT
		Transformer trans = transFact.newTransformer(xsltSource);

		// Define propriedades de saída para o documento transformado

		// Não omitir a declaração XML
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

		// Define a versão do XML como 1.0
		trans.setOutputProperty(OutputKeys.VERSION, "1.0");

		// Define a codificação do documento transformado
		if (xml.getXmlEncoding() != null) {
			trans.setOutputProperty(OutputKeys.ENCODING, xml.getXmlEncoding());
		} else {
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		}

		// Desabilita indentação automática
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		// Define a quantidade de indentação caso a indentação seja habilitada
		trans.setOutputProperty("{http://util.apache.org/xslt}indent-amount", "2");

		// Realiza a transformação XSLT
		trans.transform(xmlSource, result);

		// Recupera o documento transformado do resultado
		Document resultDoc = (Document) result.getNode();

		// Retorna o documento XML transformado
		return resultDoc;
	}

	/**
	 * Esta função aplica uma transformação XSLT a um documento XML usando um
	 * ficheiro XSLT externo.
	 *
	 * @param xml          O documento XML de entrada a ser transformado.
	 * @param xsltFileName O caminho completo do arquivo que contém o documento
	 *                     XSLT.
	 * @return O documento XML resultante da transformação.
	 * @throws TransformerException         Lançada caso ocorra um erro durante a
	 *                                      transformação XSLT.
	 * @throws ParserConfigurationException Lançada caso haja um erro na
	 *                                      configuração do parser XML.
	 * @throws FactoryConfigurationError    Lançada caso haja um erro na criação da
	 *                                      fábrica TransformerFactory.
	 * @throws SAXException                 Lançada caso ocorra um erro ao analisar
	 *                                      o ficheiro XSLT.
	 * @throws IOException                  Lançada caso ocorra um erro ao ler o
	 *                                      ficheiro XSLT.
	 * 
	 *  @example
	 * 
	 *  	// Suponha que tem um documento XML chamado "documento.xml" 
	 *  	// e uma transformação XSLT chamada "estilo.xslt" na mesma pasta.
	 *      Document xmlDoc = ...; // Carregua o documento XML 
	 *      String xsltPath = "estilo.xslt";
	 * 
	 *      Document transformedDoc = transfDoc(xmlDoc, xsltPath);
	 * 
	 *      // O documento XML foi transformado no Document "transformedDoc"
	 */
	public static final Document transfDoc(Document xml, String xsltFileName) throws TransformerException,
			ParserConfigurationException, FactoryConfigurationError, SAXException, IOException {
		return transfDoc(xml, parseFile(xsltFileName));
	}

	/**
	 * Transformação XML
	 * 
	 * Este método transforma um documento XML usando uma folha de estilo XSLT e
	 * escreve o resultado em um PrintStream.
	 *
	 * XSLT (Extensible Stylesheet Language Transformations) define como um
	 * documento XML de entrada pode ser transformado noutro documento XML de
	 * saída. Este método usa um StreamSource para aceder à folha de estilo XSLT a
	 * partir de um ficheiro e um StreamResult para escrever a saída transformada num PrintStream.
	 *
	 * @param xml          O objeto Document que representa o documento XML a ser
	 *                     transformado.
	 * @param xsltFileName O caminho completo para o arquivo que contém a folha de
	 *                     estilo XSLT.
	 * @param targetStream O PrintStream onde a saída transformada será escrita.
	 * @throws TransformerException Se ocorrer um erro durante a transformação XSLT.
	 *
	 * @example
	 *
	 * 		Document document = ...; // O documento XML String 
	 * 		xsltFile = "folha_de_estilo.xsl"; 
	 * 		PrintStream outputStream = System.out; // Ou qualquer outro PrintStream
	 *      transfDoc(document, xsltFile, outputStream);
	 *      
	 *      Este código transforma o documento XML usando a folha de estilo XSLT em
	 *      `folha_de_estilo.xsl` e escreve a saída transformada no PrintStream especificado.
	 */
	public static final void transfDoc(Document xml, String xsltFileName, PrintStream targetStream)
			throws TransformerException {
		// Cria fontes para o documento XML e a folha de estilo XSLT
		Source input = new DOMSource(xml);
		Source xsl = new StreamSource(xsltFileName);

		// Cria um resultado para escrever a saída transformada no PrintStream
		Result output = new StreamResult(targetStream);

		// Cria uma TransformerFactory e um objeto Transformer
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(xsl);

		// Define propriedades de saída para o documento transformado
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.VERSION, "1.0");

		if (xml.getXmlEncoding() != null)
			transformer.setOutputProperty(OutputKeys.ENCODING, xml.getXmlEncoding());
		else
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty("{http://util.apache.org/xslt}indent-amount", "2");

		// Realiza a transformação XSLT e escreve o resultado no PrintStream
		transformer.transform(input, output);
	}

	/**
	 * Transformação XML
	 * 
	 * @param xml            documento XML
	 * @param xsltFileName   documento XSL
	 * @param targetFileName documento gerado
	 * @throws TransformerException em caso de erro
	 */
	public static void transfDoc(Document xml, String xsltFileName, String targetFileName) throws TransformerException {
		Source input = new DOMSource(xml);
		Source xsl = new StreamSource(xsltFileName);

		Result output = new StreamResult(targetFileName);

		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(xsl);

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		transformer.transform(input, output);
	}

	/**
	 * Transformação XML
	 * 
	 * @param xmlFilePath    ficheiro original
	 * @param xsltFilePath   ficheiro com a transformação
	 * @param targetFilePath ficheiro com o resultado da transformação
	 * @throws ParserConfigurationException em caso de erro
	 * @throws IOException                  em caso de erro
	 * @throws SAXException                 em caso de erro
	 * @throws TransformerException         em caso de erro
	 */
	public static final void transfDoc(String xmlFilePath, String xsltFilePath, String targetFilePath)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		transfDoc(parseFile(xmlFilePath), xsltFilePath, targetFilePath);
	}

	/**
	 * Valida o ficheiro util usando a diretiva dtd nele incluida
	 * 
	 * @param xmlFileName ficheiro util que vai ser validado
	 * @return indica se teve sucesso
	 */
	public static final boolean validDocDTD(String xmlFileName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		try {
			factory.newDocumentBuilder().parse(new File(xmlFileName));
			// Validation successful, proceed with document processing
			return true;
		} catch (SAXException | ParserConfigurationException | IOException e) {
			// Validation failed, handle the errors
			System.out.println("Validation error: " + e.getLocalizedMessage());
		}
		return false;
	}

	/**
	 * Valida o ficheiro com util usando um dtd externo. (Não funciona)
	 * 
	 * O DTD está fortemente ligada ao processo de análise e tem nele um efeito
	 * significativo. Essencialmente é impossível definir a validação de DTD como um
	 * processo independente da análise. Por esse motivo, esta especificação não
	 * define a semântica para a DTD XML. Isso não proíbe os implementadores de
	 * a concretizar da maneira que considerarem adequada, mas os utilizadores são
	 * avisados ​​de que qualquer validação de DTD implementada nesta interface
	 * necessariamente se desviará da semântica da DTD XML conforme definida no XML 1.0.
	 * 
	 * @param xmlFileName 	ficheiro util que vai ser validado
	 * @param vFileName   	dtd usado na validação
	 * @throws SAXException em caso de erro
	 * @throws IOException  em caso de erro
	 */
	public static final void validDocDTD(String xmlFileName, String vFileName) throws SAXException, IOException {
		validDoc(xmlFileName, vFileName, XMLConstants.XML_DTD_NS_URI);
	}

	/**
	 * Valida o documento util usando xsd
	 * 
	 * @param xmlDoc    	documento util que vai ser validado
	 * @param vFileName 	xsd para realizar a validação
	 * @throws IOException  em caso de erro
	 * @throws SAXException em caso de erro
	 */
	public static void validDocXSD(Document xmlDoc, String vFileName) throws SAXException, IOException {
		validDoc(xmlDoc, vFileName, XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}

	/**
	 * Valida o documento util usando xsd
	 * 
	 * @param xmlFileName 	ficheiro util que vai ser validado
	 * @param vFileName   	xsd que vai ser usado na validação
	 * @throws IOException  em caso de erro
	 * @throws SAXException em caso de erro
	 */
	public static void validDocXSD(String xmlFileName, String vFileName) throws SAXException, IOException {
		validDoc(xmlFileName, vFileName, XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}

	/**
	 * Escreve arvore DOM num OutputStream
	 * 
	 * @param input  arvore DOM
	 * @param output stream usado para escrita
	 */
	public static final void writeDocument(final Document input, final OutputStream output) {
		/* implementação da escrita da arvore num ficheiro recorrendo ao XSLT */
		try {
			DOMSource domSource = new DOMSource(input);
			StreamResult resultStream = new StreamResult(output);
			TransformerFactory transformFactory = TransformerFactory.newInstance();

			// transformação vazia

			Transformer transformer = transformFactory.newTransformer();

			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
			if (input.getXmlEncoding() != null)
				transformer.setOutputProperty(OutputKeys.ENCODING, input.getXmlEncoding());
			else
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty("{http://util.apache.org/xslt}indent-amount", "2");

			try {
				transformer.transform(domSource, resultStream);
			} catch (javax.xml.transform.TransformerException e) {

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Escreve arvore DOM num ficheiro
	 * 
	 * @param input  arvore DOM
	 * @param output ficheiro usado para escrita
	 */

	public static final void writeDocument(final Document input, final String output) {
		try (OutputStream vaiFechar=new FileOutputStream(output)) {
			writeDocument(input,vaiFechar);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Substitui caracteres especiais com suas entidades XML correspondentes em uma
	 * string.
	 *
	 * @param str A string original contendo caracteres especiais
	 * @return Uma nova string com as entidades XML inseridas no lugar dos
	 *         caracteres especiais originais
	 *
	 * @example
	 *
	 * 		String textoOriginal = "Texto com <caracteres> especiais & aspas \"."; 
	 *      String textoComEntidades =
	 *      replaceCharactersWithXmlEntities(textoOriginal);
	 *      System.out.println(textoComEntidades); 
	 *      // Saída: Texto com &lt;caracteres&gt; especiais &amp; aspas &quot;.
	 */
	public static String xmlEntitiesFromCharacters(String str) {
		// Mapa para armazenar caracteres especiais e suas entidades XML correspondentes
		Map<Character, String> entidades = new HashMap<>();
		entidades.put('&', "&amp;");
		entidades.put('\'', "&apos;");
		entidades.put('\"', "&quot;");
		entidades.put('<', "&lt;");
		entidades.put('>', "&gt;");

		// StringBuilder para construir a string com as entidades inseridas
		StringBuilder sb = new StringBuilder();

		// Percorre cada caractere da string original
		for (char c : str.toCharArray()) {
			// Verifica se existe uma entidade correspondente ao caractere no mapa
			String entidade = entidades.get(c);
			if (entidade != null) {
				// Adiciona a entidade ao StringBuilder
				sb.append(entidade);
			} else {
				// Adiciona o caractere original ao StringBuilder
				sb.append(c);
			}
		}

		// Retorna a string resultante com as entidades XML inseridas
		return sb.toString();
	}

	/**
	 * Substitui entidades XML em uma string.
	 * 
	 * Entidades XML são usadas para representar caracteres especiais dentro de um
	 * documento XML. Este método fornece uma maneira de converter uma string
	 * contendo essas entidades de volta aos seus equivalentes de texto simples.
	 *
	 * @param str A String que contém entidades XML a serem substituídas.
	 * @return Uma nova String com as entidades XML substituídas por seus caracteres
	 *         correspondentes.
	 *
	 * @example
	 *
	 * String textoXml = "Esta string contém o e comercial &amp; e o símbolo de maior que &gt;."; 
	 * String textoSemEntidades = replaceXmlEntities(textoXml); 
	 * System.out.println(textoSemEntidades);
	 * // Saída: Esta string contém o e comercial & e o símbolo de maior que >.
	 * 
	 */
	public static String xmlEntitiesToCharacters(String str) {
		// Define um mapa para armazenar a entidade XML e seu caractere correspondente
		Map<String, String> entidades = new HashMap<>();
		entidades.put("&amp;", "&");
		entidades.put("&apos;", "'");
		entidades.put("&quot;", "\"");
		entidades.put("&lt;", "<");
		entidades.put("&gt;", ">");

		// Percorre cada entidade no mapa e substitui sua ocorrência na string
		for (Map.Entry<String, String> entry : entidades.entrySet()) {
			str = str.replace(entry.getKey(), entry.getValue());
		}

		// Retorna a string com todas as entidades XML substituídas
		return str;
	}

	/**
	 * Para demonstrar/testar a implementação
	 */
	private final static void demo1() {
		String contexto = getContexto();
		String poema = "Soneto Ditado na Agonia.xml";
		System.out.println("Lista de versões existentes:"+obterListaVersoes(contexto + poema));
		System.out.println("Versão: " + gerarNomeFBackupVersao(contexto + poema));
		System.out.println("Tempo: " + gerarNomeFBackupInstant(contexto + poema));

		Document doc = parseFile(contexto + poema);
		System.out.println("\n\nprettyPrint:");
		prettyPrint(doc);
		try {
			gravarLock(doc, contexto + poema, gerarNomeFBackupVersao(contexto + poema));
			// gravarSinc(doc,contexto+poema, gerarNomeFBackupVersao(contexto+poema) );
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nLista de documentos XML que são poemas válidos:\n"
					+ listarDocumentos(contexto, contexto + "xsd/poema.xsd"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Para demonstrar/testar a implementação
	 */
	@SuppressWarnings("unused")
	private final static void demo2() {
		String contexto = getContexto();
		String xmlFilePath = contexto + "poema.xml"; // ficheiro original
		String xsdFilePath = contexto + "xsd/poema.xsd"; // ficheiro com o XSD
		String xsltFilePath = contexto + "xsl/poema-to-html.xsl"; // ficheiro com a transformação
		String targetFilePath = contexto + "../html/poema.html"; // ficheiro com o resultado da transformação

		if (!(xsltFilePath.isBlank() || xsltFilePath.equals(contexto))) {
			try {
				transfDoc(xmlFilePath, xsltFilePath, targetFilePath);
				System.out.println("Transformação  para '" + targetFilePath + "' realizada com sucesso!");
			} catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
				System.out.println("Falhou a transformação de '" + xmlFilePath + "' com '" + xsltFilePath + "' para '"
						+ targetFilePath + "'!");
				System.out.println(e.getLocalizedMessage());

			}
		} else
			System.out.println("O ficheiro '" + xsltFilePath + "' com o XSLT não está definido!");

		if (!(xsdFilePath.isBlank() || xsdFilePath.equals(contexto))) {
			try {
				validDocXSD(xmlFilePath, xsdFilePath);
				System.out.println("Validação de '" + xmlFilePath + "' com XSD realizada com sucesso!");
			} catch (SAXException | IOException e) {
				System.out.println("Falhou a validação de '" + xmlFilePath + "' com XSD '" + xsdFilePath + "'!");
				e.printStackTrace();
			}
		} else
			System.out.println("O ficheiro '" + xsdFilePath + "' com o XSD não está definido!");

		if (validDocDTD(xmlFilePath))
			System.out.println("Validação de '" + xmlFilePath + "' com DTD realizada com sucesso!");
		else
			System.out.println("Falhou a validação de '" + xmlFilePath + "' com DTD nele incluido!");
	}

	/**
	 * Para demonstrar/testar a implementação
	 */
	@SuppressWarnings("unused")
	private final static void demo3() {
		String contexto = getContexto();
		contexto = contexto + "X-bar-cerveja/";
		String xmlFilePath = contexto + "bc.xml"; // ficheiro original
		String xsdFilePath = contexto + "bc.xsd"; // ficheiro com o XSD
		String xsltFilePath = contexto + "list.xsl"; // ficheiro com a transformação
		String targetFilePath = contexto + "list.html"; // ficheiro com o resultado da transformação

		if (!(xsltFilePath.isBlank() || xsltFilePath.equals(contexto)))
			try {
				transfDoc(xmlFilePath, xsltFilePath, targetFilePath);
				System.out.println("Transformação  '" + targetFilePath + "' realizada com sucesso!");
			} catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
				System.out.println("Falhou a transformação de '" + xmlFilePath + "' com '" + xsltFilePath + "' para '"
						+ targetFilePath + "'!");
				System.out.println(e.getLocalizedMessage());
			}
		else
			System.out.println("O ficheiro '" + xsltFilePath + "' com o XSLT não está definido!");

		if (!(xsdFilePath.isBlank() || xsdFilePath.equals(contexto)))
			try {
				validDocXSD(xmlFilePath, xsdFilePath);
				System.out.println("Validação de '" + xmlFilePath + "' com XSD realizada com sucesso!");

			} catch (SAXException | IOException e) {
				System.out.println("Falhou a validação de '" + xmlFilePath + "' com XSD '" + xsdFilePath + "'!");
				System.out.println(e.getLocalizedMessage());
			}
		else
			System.out.println("O ficheiro '" + xsdFilePath + "' com o XSD não está definido!");

		if (validDocDTD(xmlFilePath))
			System.out.println("Validação de '" + xmlFilePath + "' com DTD realizada com sucesso!");
		else
			System.out.println("Façhou a validação de '" + xmlFilePath + "' com DTD nele incluido!");
	}
	/**
	 * Obtém uma lista de nomes de ficheiros que representam versões do ficheiro original.
	 *
	 * @param nomeFicheiro 				O nome do ficheiro original.
	 * @return 							Uma lista de strings que contém os nomes dos ficheiros das versões.
	 * @throws IllegalArgumentException Se o nome do ficheiro ou o caminho da pasta forem inválidos.
	 *
	 * @see #removerCaminho(String)
	 * @see #removerExtensao(String)
	 *
	 * @xample:
	 * 		String nomeFicheiro = "meu_ficheiro.txt";
	 * 		ArrayList<String> listaVersoes = obterListaVersoes(nomeFicheiro);
	 * 		for (String versao : listaVersoes) 
	 *			System.out.println(versao);
	 *		// por exemplo: 
	 *		// 		meu_ficheiro(1).txt
	 *		// 		meu_ficheiro(2).txt
	 */
	private static ArrayList<String> obterListaVersoes(String nomeFicheiro) {
		// Obtém o caminho da pasta do ficheiro
		String caminhoPasta = new File(nomeFicheiro).getParent();
		// Verifica se o caminho da pasta é nulo ou vazio
		if (caminhoPasta == null || caminhoPasta.isEmpty()) {
			throw new IllegalArgumentException("Caminho da pasta inválido");
		}

		// Verifica se o nome do ficheiro é nulo ou vazio
		if (nomeFicheiro == null || nomeFicheiro.isEmpty()) {
			throw new IllegalArgumentException("Nome do ficheiro inválido");
		}

		// Obtém a lista de ficheiros na pasta
		File pasta = new File(caminhoPasta);
		File[] ficheiros = pasta.listFiles();

		// Cria um ArrayList para armazenar os nomes dos ficheiros das versões
		ArrayList<String> listaVersoes = new ArrayList<>();

			// Percorre a lista de ficheiros
		for (File ficheiro : ficheiros) {
			// Se o nome do ficheiro inicia com o nome do ficheiro original e contém um
			// número entre parênteses, adiciona-o à lista de versões
			String nomeFicheiroAtual = ficheiro.getName();
			nomeFicheiro = removerExtensao(removerCaminho(nomeFicheiro));
			// Regex para identificar ficheiros de versão
		    String regexVersoes = "^" + nomeFicheiro + "\\(\\d+\\)."+obterExtensaoFicheiro(nomeFicheiroAtual)+"$";

			/*if (nomeFicheiroAtual.startsWith(nomeFicheiro) && nomeFicheiroAtual.contains("(")
					&& nomeFicheiroAtual.contains(")"))*/
			nomeFicheiroAtual = ficheiro.getName();
	        if (nomeFicheiroAtual.matches(regexVersoes)) {
	            listaVersoes.add(nomeFicheiroAtual);
	        }
		}

		// Retorna a lista de versões
		return listaVersoes;
	}

	/**
	 * Validação de documento na árvore DOM, com XSD ou DTD conforme o indicado no
	 * parametro type (com DTD não funciona, usar validDocDTD(String xmlFileName) )
	 * 
	 * @param document    	documento que vai ser validado
	 * @param xsdFileName 	xsd que vai ser usado na validação
	 * @param type        	XMLConstants.W3C_XML_SCHEMA_NS_URI ou
	 *                    	XMLConstants.XML_DTD_NS_URI (não funciona)
	 * @throws IOException  se houver erro 
	 * @throws SAXException se houver erro
	 */
	private static final void validDoc(Document document, String xsdFileName, String type)
			throws SAXException, IOException {
		removerXSD(document);
		// create a SchemaFactory capable of understanding WXS schemas
		SchemaFactory factory = SchemaFactory.newInstance(type);

		// load a WXS schema, represented by a Schema instance
		Source schemaFile = new StreamSource(new File(xsdFileName));
		Schema schema = factory.newSchema(schemaFile);

		// create a Validator instance, which can be used to validate an instance
		// document
		Validator validator = schema.newValidator();

		// validate the DOM tree
		validator.validate(new DOMSource(document));
	}

	/**
	 * Validação de documento em ficheiro com XSD ou DTD conforme o indicado no
	 * parametro type (com DTD não funciona, validDocDTD(String xmlFileName) )
	 * 
	 * @param xmlFileName ficheiro util que vai ser validado
	 * @param vFileName   xsd usado na validação
	 * @param type        XMLConstants.W3C_XML_SCHEMA_NS_URI ou
	 *                    XMLConstants.XML_DTD_NS_URI (não funciona)
	 * @throws IOException
	 * @throws SAXException
	 */
	private static final void validDoc(String xmlFileName, String vFileName, String type)
			throws SAXException, IOException {
		validDoc(parseFile(xmlFileName), vFileName, type);
	}

    /**
     * Codifique o nome do ficheiro UTF-8 para um URI
     * @param fileName nomde do ficheiro
     * @return o URI
     */
    public static String convertURI(String fileName){
		try {
			byte[] utf8Bytes = fileName.getBytes("UTF-8");
			// Codifica o nome do ficheiro em UTF-8
			String encodedFileName = URLEncoder.encode(new String(utf8Bytes), "UTF-8");
	        // Crie um URI a partir do nome do ficheiro codificado
	        URI uri = URI.create(encodedFileName);  // "file://" 
	        // Retorna o URI
	        return uri.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
    }
    /**
     * Este método gera um hash de 256 bits, 
     * representado como uma string hexadecimal de 64 caracteres. 
     * É importante salientar que o SHA-256 é uma função unidirecional, 
     * o que significa que não é possível recuperar a string original
     * a partir do hash.
     * 
     * @param str 			original
     * @return				hash
     * @throws NoSuchAlgorithmException em caso de erro
     */
    public static String SHA256(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(str.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
  	  sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
