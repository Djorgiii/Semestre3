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
 * XMLDoc — utilitário central para operações sobre documentos XML.
 *
 * Agrupa todas as operações de baixo nível necessárias ao sistema:
 *   - Parsing de XML (de ficheiro ou de String)
 *   - Validação contra XSD ou DTD
 *   - Serialização (Document → String ou ficheiro)
 *   - Pesquisa por XPath (retorna NodeList, inteiro ou String)
 *   - Transformações XSLT
 *   - Gravação segura com lock de ficheiro e backup automático por versão
 *   - Gestão do contexto do sistema de ficheiros (CLI vs Tomcat)
 *   - Hash SHA-256 de strings (usado para guardar passwords)
 *   - Utilitários de strings para nomes de ficheiro e entidades XML
 *
 * O contexto (getContexto/setContextoReal) resolve automaticamente o caminho
 * correcto conforme o ambiente de execução: Eclipse CLI ou Tomcat.
 */
public class XMLDoc {

	/** Serializa um Document DOM para String XML formatada. */
	public static final String documentToString(Document xmlDoc)
			throws TransformerFactoryConfigurationError, TransformerException {
		if (xmlDoc == null) {
			return null;
		}

		Writer out = new StringWriter();

		Transformer tf = TransformerFactory.newInstance().newTransformer();

		tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		tf.setOutputProperty(OutputKeys.VERSION, "1.0");
		if (xmlDoc.getXmlEncoding() != null)
			tf.setOutputProperty(OutputKeys.ENCODING, xmlDoc.getXmlEncoding());
		else
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		tf.setOutputProperty("{http://util.apache.org/xslt}indent-amount", "2");

		tf.transform(new DOMSource(xmlDoc), new StreamResult(out));

		return out.toString();
	}

	/** Gera um nome de ficheiro de backup com timestamp completo (até milissegundos). */
	public static String gerarNomeFBackupInstant(String caminho) {
		Instant dataHoraAtual = Instant.now();

		ZonedDateTime dataHoraLocal = dataHoraAtual.atZone(ZoneId.systemDefault());

		int ano = dataHoraLocal.getYear();
		int mes = dataHoraLocal.getMonthValue();
		int dia = dataHoraLocal.getDayOfMonth();
		int hora = dataHoraLocal.getHour();
		int minuto = dataHoraLocal.getMinute();
		int segundo = dataHoraLocal.getSecond();
		int milissegundo = dataHoraLocal.getNano() / 1_000_000;

		return String.format("%s-%04d%02d%02d%02d%02d%02d%03d."+obterExtensaoFicheiro(caminho), removerExtensao(caminho), ano, mes, dia, hora,
				minuto, segundo, milissegundo);
	}

	/** Gera um nome de ficheiro de backup com número de versão incremental (ex: users(3).util). */
	public static String gerarNomeFBackupVersao(String nomeFicheiro) {
		int numeroVersaoFicheiroMaisRecente = obterNumeroVersao(nomeFicheiro);

		int numeroVersaoSeguinte = numeroVersaoFicheiroMaisRecente + 1;

		String nomeFicheiroVersaoSeguinte = removerExtensao(nomeFicheiro) + "(" + numeroVersaoSeguinte + ")" + ".util";

		return nomeFicheiroVersaoSeguinte;
	}

	
	private static String contextoReal = null;

	/** Define o caminho real da webapp quando executado no Tomcat (chamado pelo JSP). */
	public static void setContextoReal(String path) {
	    contextoReal = path;
	}
	
	/**
     * Devolve o caminho base para os ficheiros XML/XSD do sistema.
     * Usa o caminho real do Tomcat se disponível; caso contrário tenta
     * os caminhos padrão do Eclipse (WebContent/ ou src/main/webapp/).
     */
    public static final String getContexto() {
	    // Se o JSP já nos deu o caminho real do Tomcat, usamos esse!
	    if (contextoReal != null) {
	        return contextoReal;
	    }
	    
	    // Fallback para quando se corre fora do Tomcat (ex: no CLI)
	    String contexto = "WebContent/";
	    File f = new File(contexto);
	    if (!(f.exists() && f.isDirectory())) {
	        contexto = "src/main/webapp/";
	    }
	    return contexto;
	}
	
	
	public static final String getAbsPath() {
	    File file = new File(".");
	    String absolutePath = file.getAbsolutePath();
	    absolutePath = absolutePath.substring(0, absolutePath.length() - 1); 
	    String workingDir =  absolutePath + File.separator; 
	    return workingDir;
	}

	/**
     * Executa uma expressão XPath sobre um documento e devolve os nós resultantes.
     *
     * @param expression expressão XPath (ex: "/users/user[username='Alice']")
     * @param doc        documento XML onde pesquisar
     * @return NodeList com os nós encontrados (pode estar vazia)
     */
    public static final NodeList getXPath(final String expression, final Document doc) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes;
		nodes = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
		return nodes;
	}

	/**
     * Executa uma expressão XPath numérica (ex: count(...)) sobre um documento.
     *
     * @param expression expressão XPath que devolve um número
     * @param doc        documento XML onde pesquisar
     * @return resultado como inteiro
     */
    public static final int getXPathN(final String expression, final Document doc) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		return ((Double) xpath.evaluate(expression, doc, XPathConstants.NUMBER)).intValue();
	}

	public static String getXPathV(final String expression, final Document doc) throws XPathExpressionException {

		XPathFactory factory = XPathFactory.newInstance();

		XPathExpression xpathExpression = factory.newXPath().compile(expression);

		NodeList nodes = (NodeList) xpathExpression.evaluate(doc, XPathConstants.NODESET);

		if (nodes.getLength() == 0) {
			return null;
		}

		return nodes.item(0).getNodeValue();
	}

	/**
     * Grava um Document XML no disco de forma segura.
     * Antes de escrever, renomeia o ficheiro original para o nome de backup
     * (preservação em caso de falha — Req. 5). Usa FileLock para garantir
     * exclusividade de escrita em ambiente concorrente (múltiplos jogos).
     *
     * @param documento        documento XML a gravar
     * @param ficheiroOriginal caminho do ficheiro de destino
     * @param ficheiroBackup   caminho para o backup do ficheiro anterior
     */
    public synchronized static void gravarLock(Document documento, String ficheiroOriginal, String ficheiroBackup)
			throws TransformerFactoryConfigurationError, TransformerException, IOException {

		renomear(ficheiroOriginal, ficheiroBackup);

		String stXML = documentToString(documento);

		FileChannel fileChannel = FileChannel.open(Paths.get(ficheiroOriginal), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE);

		try (FileLock lock = fileChannel.lock()) { // faz unlock automaticamente

			ByteBuffer byteBuffer = ByteBuffer.wrap(stXML.getBytes());

			while (byteBuffer.hasRemaining())
				fileChannel.write(byteBuffer);

		} finally {
			fileChannel.close();
		}
	}

	public synchronized static void gravarSinc(Document documento, String origem, String nomeFicheiroBackup)
			throws Exception {
		renomear(origem, nomeFicheiroBackup);
		writeDocument(documento, origem);
	}

	public static final ArrayList<String> listarDocumentos(String pasta, String xsdFile) {
		ArrayList<String> result = new ArrayList<String>();

		File folder = new File(pasta);

		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().toLowerCase().endsWith(".util")) {
				try {
					validDocXSD(pasta + listOfFiles[i].getName(), xsdFile);
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

	public static final String listarFicheiros(String pasta) {

		String result = "<?util version='1.0' encoding='UTF-8' standalone='yes'?>\n";
		result = result + "<ficheiros>\n";

		File folder = new File(pasta);

		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isFile()) {

				result = result + "<ficheiro>" + listOfFiles[i].getName() + "</ficheiro>\n";
			}
		}

		return result + "</ficheiros>\n";
	}


	public static void main(String[] args) {
		demo1();
	}

	public static String obterNomeFicheiro(String caminhoFicheiro) {
		return removerExtensao(removerCaminho(caminhoFicheiro));
	}
	

	public static String obterExtensaoFicheiro(String caminhoFicheiro) {

		if (caminhoFicheiro == null || caminhoFicheiro.isEmpty()) 
			return "";

		int indicePonto = caminhoFicheiro.lastIndexOf(".");

		if (indicePonto == -1) 
			return caminhoFicheiro;

		return caminhoFicheiro.substring(indicePonto+1);
	}

	public static int obterNumeroVersao(String nomeFicheiro) {
		ArrayList<String> listaVersoes = obterListaVersoes(nomeFicheiro);
		if (listaVersoes.isEmpty()) {
			return 0;
		}

		Collections.sort(listaVersoes, Collections.reverseOrder());

		String nomeFicheiroVersaoMaisRecente = listaVersoes.get(0);

		String regex = "\\((\\d+)\\)";
		Matcher matcher = Pattern.compile(regex).matcher(nomeFicheiroVersaoMaisRecente);

		if (matcher.find())
			return Integer.parseInt(matcher.group(1));


		return 0;
	}

	/**
     * Carrega e faz parse de um ficheiro XML do disco.
     *
     * @param fileName caminho absoluto do ficheiro XML
     * @return Document DOM ou null se o ficheiro não existir ou for inválido
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
     * Faz parse de uma String XML e devolve o Document DOM resultante.
     * Usado para processar as mensagens do protocolo TCP.
     *
     * @param xmlStr String com conteúdo XML bem formado
     * @return Document DOM
     * @throws Exception se o XML estiver mal formado
     */
    public static Document parseString(String xmlStr) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = factory.newDocumentBuilder();

		InputSource is = new InputSource(new StringReader(xmlStr));

		return builder.parse(is);
	}

	public static final void prettyPrint(Document xml) {
		Transformer tf;
		try {
			tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");

			Writer out = new StringWriter();
			tf.transform(new DOMSource(xml), new StreamResult(out));

			System.out.println(out.toString());
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			e.printStackTrace();
		}
	}


	public static String removerCaminho(String caminhoFicheiro) {

		if (caminhoFicheiro == null || caminhoFicheiro.isEmpty()) {
			return "";
		}

		String nomeFicheiroComExtensao = new File(caminhoFicheiro).getName();

		return nomeFicheiroComExtensao;
	}

	public static String removerNaoAlfa(String palavra) {

		String regex = "[^a-zA-ZÁÀÃÂÉÈÍÌÒÓÕÔÙÚáàãâäëéèêíìîóòõôúùç\\s]";

		return palavra.replaceAll(regex, "");
	}

	public static String removerExtensao(String caminhoFicheiro) {

		if (caminhoFicheiro == null || caminhoFicheiro.isEmpty()) {
			return "";
		}

		int indicePonto = caminhoFicheiro.lastIndexOf(".");

		if (indicePonto == -1) {
			return caminhoFicheiro;
		}

		return caminhoFicheiro.substring(0, indicePonto);
	}


	public static final void removerXSD(Document xml) {
		Element raiz = xml.getDocumentElement();
		raiz.removeAttribute("xsi:noNamespaceSchemaLocation");
	}

	/**
     * Renomeia um ficheiro no disco. Usado internamente pelo gravarLock
     * para criar o backup antes de escrever a nova versão.
     *
     * @param ficheiroAntigo caminho do ficheiro a renomear
     * @param ficheiroNovo   novo nome/caminho do ficheiro
     */
    public static void renomear(String ficheiroAntigo, String ficheiroNovo) {
		File ficheiroBack = new File(ficheiroNovo);

		File original = new File(ficheiroAntigo);
		if (original.exists())
			original.renameTo(ficheiroBack);
	}

	public static final void seriaDocumento(final Document DOMtree, final String targetFileName) throws IOException {
		FileOutputStream FOS = null;
		DOMImplementationLS DOMiLS = null;

		if ((DOMtree.getFeature("Core", "3.0") != null) && (DOMtree.getFeature("LS", "3.0") != null)) {
			DOMiLS = (DOMImplementationLS) (DOMtree.getImplementation()).getFeature("LS", "3.0");
			System.out.println("[Usando DOM Load and Save]");
		} else {
			System.out.println("[DOM Load and Save não suportado]");
			System.exit(0);
		}
		LSOutput LSO = DOMiLS.createLSOutput();
		LSO.setEncoding("UTF-8");

		FOS = new FileOutputStream(targetFileName);
		LSO.setByteStream((OutputStream) FOS);

		LSSerializer LSS = DOMiLS.createLSSerializer();

		boolean ser = LSS.write(DOMtree, LSO);

		if (ser)
			System.out.println("\n[Seriação concluída!]");
		else
			System.out.println("[Seriação falhou!]");

		FOS.close();
	}

	public static final String seriaDocumento(final Document DOMtree) throws IOException {

	    StringWriter stringWriter = new StringWriter();

	    DOMImplementationLS DOMiLS = (DOMImplementationLS) (DOMtree.getImplementation()).getFeature("LS", "3.0");

	    LSOutput LSO = DOMiLS.createLSOutput();
	    LSO.setCharacterStream(stringWriter);
	    LSO.setEncoding("UTF-8");

	    LSSerializer LSS = DOMiLS.createLSSerializer();

	    LSS.write(DOMtree, LSO);

	    return stringWriter.toString();
	}

	public static String stringFromFile(String filename) throws FileNotFoundException {
		File file = new File(filename);

		if (!file.exists()) {
			throw new FileNotFoundException("Ficheiro não encontrado: " + filename);
		}

		StringBuilder sb = new StringBuilder();

		Scanner scanner = new Scanner(file);

		while (scanner.hasNextLine()) {
			sb.append(scanner.nextLine()).append("\n");
		}

		scanner.close();

		return sb.toString();
	}

	public static void stringToFile(String str, String filename) throws IOException {
		FileWriter file = new FileWriter(filename);

		file.write(str);

		file.close();
	}

	
	/** Calcula e formata o tempo decorrido desde um instante até agora. */
    public static final String tempoDif(LocalDateTime inicio) {
		return tempoDif(inicio,LocalDateTime.now());
	}

	public static final String tempoDif(LocalDateTime inicio, LocalDateTime fim) {

		long diferencaMili = Duration.between(inicio, fim).toMillis();


		long dias = diferencaMili / (1000 * 60 * 60 * 24);
		long horas = (diferencaMili % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minutos = (diferencaMili % (1000 * 60 * 60)) / (1000 * 60);
		long segundos = (diferencaMili % (1000 * 60)) / 1000;
		long milissegundos = (diferencaMili % 1000);

		return "Demorou: " + ((dias == 0) ? "" : "Dias(" + dias + ") ") + ((horas == 0) ? "" : "Horas(" + horas + ") ")
				+ ((minutos == 0) ? "" : "Minutos(" + minutos + ") ")
				+ ((segundos == 0) ? "" : "Segundos(" + segundos + ") ")
				+ ((milissegundos == 0) ? "" : "Milissegundos(" + milissegundos + ") ");

	}


	public static Document transfDoc(Document xml, Document xslt)
			throws TransformerException, ParserConfigurationException, FactoryConfigurationError {

		Source xmlSource = new DOMSource(xml);
		Source xsltSource = new DOMSource(xslt);


		DOMResult result = new DOMResult();


		TransformerFactory transFact = TransformerFactory.newInstance();

		Transformer trans = transFact.newTransformer(xsltSource);


		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

		trans.setOutputProperty(OutputKeys.VERSION, "1.0");

		if (xml.getXmlEncoding() != null) {
			trans.setOutputProperty(OutputKeys.ENCODING, xml.getXmlEncoding());
		} else {
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		}

		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		trans.setOutputProperty("{http://util.apache.org/xslt}indent-amount", "2");

		trans.transform(xmlSource, result);

		Document resultDoc = (Document) result.getNode();

		return resultDoc;
	}


	public static final Document transfDoc(Document xml, String xsltFileName) throws TransformerException,
			ParserConfigurationException, FactoryConfigurationError, SAXException, IOException {
		return transfDoc(xml, parseFile(xsltFileName));
	}

	public static final void transfDoc(Document xml, String xsltFileName, PrintStream targetStream)
			throws TransformerException {
		Source input = new DOMSource(xml);
		Source xsl = new StreamSource(xsltFileName);

		Result output = new StreamResult(targetStream);

		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(xsl);

		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.VERSION, "1.0");

		if (xml.getXmlEncoding() != null)
			transformer.setOutputProperty(OutputKeys.ENCODING, xml.getXmlEncoding());
		else
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty("{http://util.apache.org/xslt}indent-amount", "2");

		transformer.transform(input, output);
	}


	public static void transfDoc(Document xml, String xsltFileName, String targetFileName) throws TransformerException {
		Source input = new DOMSource(xml);
		Source xsl = new StreamSource(xsltFileName);

		Result output = new StreamResult(targetFileName);

		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(xsl);

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		transformer.transform(input, output);
	}


	public static final void transfDoc(String xmlFilePath, String xsltFilePath, String targetFilePath)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		transfDoc(parseFile(xmlFilePath), xsltFilePath, targetFilePath);
	}


	public static final boolean validDocDTD(String xmlFileName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		try {
			factory.newDocumentBuilder().parse(new File(xmlFileName));
			return true;
		} catch (SAXException | ParserConfigurationException | IOException e) {
			System.out.println("Validation error: " + e.getLocalizedMessage());
		}
		return false;
	}


	public static final void validDocDTD(String xmlFileName, String vFileName) throws SAXException, IOException {
		validDoc(xmlFileName, vFileName, XMLConstants.XML_DTD_NS_URI);
	}

	/**
     * Valida um Document DOM contra um ficheiro XSD.
     * Lança SAXException se o documento violar o schema.
     *
     * @param xmlDoc    documento a validar
     * @param vFileName caminho do ficheiro XSD
     */
    public static void validDocXSD(Document xmlDoc, String vFileName) throws SAXException, IOException {
		validDoc(xmlDoc, vFileName, XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}


	public static void validDocXSD(String xmlFileName, String vFileName) throws SAXException, IOException {
		validDoc(xmlFileName, vFileName, XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}

	public static final void writeDocument(final Document input, final OutputStream output) {
		try {
			DOMSource domSource = new DOMSource(input);
			StreamResult resultStream = new StreamResult(output);
			TransformerFactory transformFactory = TransformerFactory.newInstance();


			Transformer transformer = transformFactory.newTransformer();

			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
			if (input.getXmlEncoding() != null)
				transformer.setOutputProperty(OutputKeys.ENCODING, input.getXmlEncoding());
			else
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://util.apache.org/xslt}indent-amount", "2");

			try {
				transformer.transform(domSource, resultStream);
			} catch (javax.xml.transform.TransformerException e) {

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static final void writeDocument(final Document input, final String output) {
		try (OutputStream vaiFechar=new FileOutputStream(output)) {
			writeDocument(input,vaiFechar);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
     * Escapa os caracteres especiais XML de uma string (< > & ' ").
     * Útil ao inserir texto livre em atributos ou conteúdo de elementos XML.
     *
     * @param str string com possíveis caracteres especiais
     * @return string com entidades XML escapadas
     */
    public static String xmlEntitiesFromCharacters(String str) {
		Map<Character, String> entidades = new HashMap<>();
		entidades.put('&', "&amp;");
		entidades.put('\'', "&apos;");
		entidades.put('\"', "&quot;");
		entidades.put('<', "&lt;");
		entidades.put('>', "&gt;");

		StringBuilder sb = new StringBuilder();

		for (char c : str.toCharArray()) {
			String entidade = entidades.get(c);
			if (entidade != null) {
				sb.append(entidade);
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	/**
     * Converte entidades XML (&amp; &lt; etc.) de volta para os caracteres originais.
     *
     * @param str string com entidades XML
     * @return string com os caracteres originais
     */
    public static String xmlEntitiesToCharacters(String str) {
		Map<String, String> entidades = new HashMap<>();
		entidades.put("&amp;", "&");
		entidades.put("&apos;", "'");
		entidades.put("&quot;", "\"");
		entidades.put("&lt;", "<");
		entidades.put("&gt;", ">");

		for (Map.Entry<String, String> entry : entidades.entrySet()) {
			str = str.replace(entry.getKey(), entry.getValue());
		}

		return str;
	}


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


	@SuppressWarnings("unused")
	private final static void demo3() {
		String contexto = getContexto();
		contexto = contexto + "X-bar-cerveja/";
		String xmlFilePath = contexto + "bc.xml";
		String xsdFilePath = contexto + "bc.xsd";
		String xsltFilePath = contexto + "list.xsl";
		String targetFilePath = contexto + "list.html";

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

	private static ArrayList<String> obterListaVersoes(String nomeFicheiro) {
		String caminhoPasta = new File(nomeFicheiro).getParent();
		if (caminhoPasta == null || caminhoPasta.isEmpty()) {
			throw new IllegalArgumentException("Caminho da pasta inválido");
		}

		if (nomeFicheiro == null || nomeFicheiro.isEmpty()) {
			throw new IllegalArgumentException("Nome do ficheiro inválido");
		}

		File pasta = new File(caminhoPasta);
		File[] ficheiros = pasta.listFiles();

		ArrayList<String> listaVersoes = new ArrayList<>();

		for (File ficheiro : ficheiros) {
			String nomeFicheiroAtual = ficheiro.getName();
			nomeFicheiro = removerExtensao(removerCaminho(nomeFicheiro));
		    String regexVersoes = "^" + nomeFicheiro + "\\(\\d+\\)."+obterExtensaoFicheiro(nomeFicheiroAtual)+"$";


			nomeFicheiroAtual = ficheiro.getName();
	        if (nomeFicheiroAtual.matches(regexVersoes)) {
	            listaVersoes.add(nomeFicheiroAtual);
	        }
		}

		return listaVersoes;
	}


	private static final void validDoc(Document document, String xsdFileName, String type)
			throws SAXException, IOException {
		removerXSD(document);
		SchemaFactory factory = SchemaFactory.newInstance(type);

		Source schemaFile = new StreamSource(new File(xsdFileName));
		Schema schema = factory.newSchema(schemaFile);


		Validator validator = schema.newValidator();

		validator.validate(new DOMSource(document));
	}


	private static final void validDoc(String xmlFileName, String vFileName, String type)
			throws SAXException, IOException {
		validDoc(parseFile(xmlFileName), vFileName, type);
	}

    public static String convertURI(String fileName){
		try {
			byte[] utf8Bytes = fileName.getBytes("UTF-8");
			String encodedFileName = URLEncoder.encode(new String(utf8Bytes), "UTF-8");
	        URI uri = URI.create(encodedFileName);
	        return uri.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
    }

    /**
     * Calcula o hash SHA-256 de uma string e devolve-o em hexadecimal minúsculo.
     * Usado para guardar e verificar passwords dos utilizadores.
     *
     * @param str string a codificar (ex: password em texto simples)
     * @return hash SHA-256 de 64 caracteres hexadecimais
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