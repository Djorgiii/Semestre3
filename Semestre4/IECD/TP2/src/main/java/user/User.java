package user;

/**
 * User — modelo de dados de um utilizador do sistema.
 *
 * Carrega e mantém o ficheiro users.xml em memória estática (Document doc).
 * Cada instância representa um utilizador com todos os seus atributos.
 *
 * Responsabilidades principais:
 *   - Validação e encapsulamento dos dados do utilizador.
 *   - Autenticação via XPath sobre o XML (username + SHA-256 da password).
 *   - Serialização para XML (toXMLString) para envio no protocolo TCP.
 *   - Operações de gestão: alterar foto, alterar senha, bloquear/desbloquear.
 *   - Persistência: gravar o users.xml com backup (via XMLDoc.gravarLock).
 *
 * A password é sempre guardada como hash SHA-256 — nunca em texto simples.
 */
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.MyImage;
import util.XMLDoc;


public class User {
    private static String file = "users";

    private static Document doc = null;

    private UUID userId = null;

    private LocalDateTime updated = LocalDateTime.now();

    private boolean blocked = false;

    private int profile = 1;

    private String username = null;

    private String password = null;

    private String firstNames = null;

    private String lastNames = null;

    private String email = null;

    private String gender = null;

    private LocalDate birthdate = null;

    private MyImage photography = null;

    private Nationality nationality = null;

    static Scanner sc = new Scanner(System.in);

    static {
	_load();
    }

    /**
     * (Re)carrega o ficheiro users.xml em memória e valida contra o users.xsd.
     * Deve ser chamado após qualquer alteração ao ficheiro XML.
     */
    public static void _load() {
	Document d = XMLDoc.parseFile(XMLDoc.getContexto() + file + ".xml");
	try {
	    XMLDoc.validDocXSD(d, XMLDoc.getContexto() + file + ".xsd");
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	doc = d;
    }


    public User() {
	this.userId = UUID.randomUUID();
	this.updated = LocalDateTime.now();
	this.blocked = true;
	this.profile = 1;
    }


    public User(String username, String password, String firstNames, String lastNames) throws NoSuchAlgorithmException {
	this.userId = UUID.randomUUID();
	this.updated = LocalDateTime.now();
	this.blocked = true;	// quando é criado fica bloqueado
	this.profile = 1;
	setUsername(username);
	setPassword(password);
	setFirstNames(firstNames);
	setLastNames(lastNames);
    }

    public UUID getUserId() {
	return userId;
    }

    public boolean setUserId(UUID userId) {
	this.userId = userId;
	return true;
    }


    public LocalDateTime getUpdated() {
	return updated;
    }


    public boolean setUpdated(LocalDateTime updated) {
	this.updated = updated;
	return true;
    }


    public boolean isBlocked() {
	return blocked;
    }


    public boolean setBlocked(boolean blocked) {
	this.blocked = blocked;
	return true;
    }


    public int getProfile() {
	return profile;
    }


    public boolean setProfile(int profile) {
	if (profile >= 0 && profile <= 10) {
	    this.profile = profile;
	    return true;
	}
	return false;
    }


    public String getUsername() {
	return username;
    }


    public static boolean validarUserName(String username) {

	final int TAMANHO_MINIMO = 3;
	final int TAMANHO_MAXIMO = 10;

	final String REGEX_CARACTERES_VALIDOS = "^[a-zA-Z0-9_-]{" + TAMANHO_MINIMO + "," + TAMANHO_MAXIMO + "}$";

	if (username.isEmpty()) {
	    return false;
	}

	if (!username.matches(REGEX_CARACTERES_VALIDOS)) {
	    return false;
	}

	return true;
    }


    public boolean setUsername(String username) {
	if (!validarUserName(username)) {
	    return false;
	}
	this.username = username;
	return true;
    }


    public String getPassword() {
	return password;
    }


    public boolean setPassword(String password) throws NoSuchAlgorithmException {
	this.password = XMLDoc.SHA256(password);
	return true;
    }

    public String getFirstNames() {
	return firstNames;
    }


    public boolean setFirstNames(String firstNames) {
	this.firstNames = firstNames;
	return true;
    }


    public String getLastNames() {
	return lastNames;
    }


    public boolean setLastNames(String lastNames) {
	this.lastNames = lastNames;
	return true;
    }


    public String getName() {
	return capitalizar(firstNames + " " + lastNames);
    }


    public String getEmail() {
	return email;
    }


    public boolean setEmail(String email) {
	if (email != null) {
	    String regex = "^(.+)@([\\w\\-]+\\.)+([\\w\\-]+)$";
	    Pattern pattern = Pattern.compile(regex);
	    Matcher matcher = pattern.matcher(email);
	    if (!matcher.matches())
		return false;
	}
	this.email = email;
	return true;
    }


    public String getGender() {
	return gender;
    }


    public boolean setGender(String gender) {
	if (gender != null)
	    if (!gender.equals("M") && !gender.equals("F") && !gender.equals("X"))
		return false;
	this.gender = gender;
	return true;
    }


    public LocalDate getBirthdate() {
	return birthdate;
    }


    public boolean setBirthdate(LocalDate birthdate) {
	if (birthdate != null)
	    if (birthdate.isAfter(LocalDate.now()))
		return false;
	this.birthdate = birthdate;
	return true;
    }


    public int getAge() {
	if (birthdate == null)
	    return -1;
	return Period.between(birthdate, LocalDate.now()).getYears();
    }


    public MyImage getPhotography() {
	return photography;
    }


    public boolean setPhotography(String photo) {
	if (photo != null) {
	    MyImage i = new MyImage();
	    i.setBase64(photo.replaceAll("[\\s]", ""));
	    return setPhotography(i);
	}
	this.photography = null;
	return true;
    }


    public boolean setPhotography(MyImage photo) {
	this.photography = photo;
	return true;
    }

    public Nationality getNationality() {
	return nationality;
    }


    public String getPtNationality() throws XPathExpressionException, Exception {
	return nationality.pt(gender);
    }


    public boolean setNationality(Nationality nationality) {
	this.nationality = nationality;
	return true;
    }


    public boolean setNationality(String abbreviation) throws XPathExpressionException {
	this.nationality = Nationality.getByAbbreviation(abbreviation);
	return true;
    }


    public static User read() {
	User user = new User();
	do {
	    System.out.print("Nome do utilizador: ");
	    String username = sc.nextLine();
	    if (user.setUsername(username))
		break;
	    System.out.print("Nome de utilizador inválido! ");
	} while (true);

	do {
	    System.out.print("Senha para autenticação: ");
	    String password = sc.nextLine();
	    try {
		if (user.setPassword(password))
		    break;
	    } catch (NoSuchAlgorithmException e) {
		e.printStackTrace();
	    }
	    System.out.print("Senha inválida! ");
	} while (true);

	do {
	    System.out.print("Primeiros nomes do utilizador: ");
	    String firstNames = sc.nextLine();
	    if (user.setFirstNames(firstNames))
		break;
	    System.out.print("Nomes inválidos! ");
	} while (true);

	do {
	    System.out.print("Últimos nomes do utilizador: ");
	    String lastNames = sc.nextLine();
	    if (user.setLastNames(lastNames))
		break;
	    System.out.print("Nomes inválidos! ");
	} while (true);

	do {
	    System.out.print("Endereço de correio eletrónico: ");
	    String email = sc.nextLine();
	    if (user.setEmail(email))
		break;
	    System.out.print("Endereço de correio eletrónico inválido! ");
	} while (true);

	do {
	    System.out.print("Género (M, F, X): ");
	    String gender = sc.nextLine().toUpperCase();
	    if (gender == null || user.setGender(gender))
		break;
	} while (true);

	do {
	    try {
		System.out.print("Data de nascimento (YYYY-MM-DD): ");
		String birthdateStr = sc.nextLine();
		if (birthdateStr == null)
		    break;
		if (user.setBirthdate(LocalDate.parse(birthdateStr)))
		    break;
	    } catch (Exception e) {

	    } finally {
		System.err.println("Data de nascimento inválida. Tente novamente (YYYY-MM-DD): ");
	    }
	} while (true);

	do {
	    System.out.print("Indique o caminho para o ficheiro que contém a fotografia tipo pass: ");
	    String foto = sc.nextLine();
	    if (foto == null)
		break;
	    MyImage ft = new MyImage(XMLDoc.getContexto() + foto);
	    ft.view();
	    if (user.setPhotography(ft))
		break;
	} while (true);

	do {
	    System.out.print("Indique o código iso (2 letras) da nacionalidade: ");
	    String nat = sc.nextLine();
	    if (nat == null)
		break;
	    try {
		if (user.setNationality(nat))
		    break;
	    } catch (XPathExpressionException e) {
		e.printStackTrace();
	    }
	} while (true);

	return user;
    }


    public void print() throws Exception {
	System.out.println("----- Dados do Utilizador -----");
	if (blocked)
	    System.out.println("Está bloqueado!");
	else
	    System.out.println("Não está bloqueado!");
	System.out.println("Identificador (UUID): " + userId);
	System.out.println("Última atualização em: " + updated);
	System.out.println("Perfil: " + profile);
	System.out.println("Nome de utilizador: " + username);
	System.out.println("Senha: " + password);
	System.out.println("Nome completo: " + getName());
	if (email != null)
	    System.out.println("Endereço de email: " + email);
	if (gender != null)
	    System.out.println("Género: " + gender);
	if (birthdate != null) {
	    System.out.println("Data de nascimento: " + birthdate);
	    System.out.println("Idade: " + getAge());
	}
	if (!getPtNationality().equals(""))
	    System.out.println("Nacionalidade: " + getPtNationality());
	System.out.println("---------------------------------------------------");
	if (getPhotography() != null)
	    getPhotography().view();
    }


    public void toDocument() throws ParserConfigurationException {
	Element userElement = doc.createElement("user");

	Element aux = doc.createElement("userid");
	aux.setTextContent(getUserId().toString());
	userElement.appendChild(aux);

	aux = doc.createElement("updated");
	aux.setTextContent(dateTimeToXsd(LocalDateTime.now()));
	userElement.appendChild(aux);

	aux = doc.createElement("blocked");
	aux.setTextContent(isBlocked() ? "true" : "false");
	userElement.appendChild(aux);

	aux = doc.createElement("profile");
	aux.setTextContent(String.valueOf(getProfile()));
	userElement.appendChild(aux);

	aux = doc.createElement("username");
	aux.setTextContent(getUsername());
	userElement.appendChild(aux);

	aux = doc.createElement("firstNames");
	aux.setTextContent(getFirstNames());
	userElement.appendChild(aux);

	aux = doc.createElement("lastNames");
	aux.setTextContent(getLastNames());
	userElement.appendChild(aux);

	if (getEmail() != null) {
	    aux = doc.createElement("email");
	    String e = getEmail();
	    aux.setTextContent((e == null) ? "" : e);
	    userElement.appendChild(aux);
	}

	if (getGender() != null) {
	    aux = doc.createElement("gender");
	    String g = getGender();
	    aux.setTextContent((g == null) ? "" : g);
	    userElement.appendChild(aux);
	}

	if (getBirthdate() != null) {
	    aux = doc.createElement("birthdate");
	    if (getBirthdate() == null)
		aux.setTextContent("");
	    else
		aux.setTextContent(dateToXsd(getBirthdate()));
	    userElement.appendChild(aux);
	}
	if (getPhotography() != null) {
	    aux = doc.createElement("photography");
	    String foto = getPhotography().getBase64();
	    aux.setTextContent((foto == null) ? "" : foto);
	    userElement.appendChild(aux);
	}

	if (getNationality() != null) {
	    aux = doc.createElement("nationality");
	    String abr = getNationality().getAbbreviation();
	    aux.setTextContent((abr == null) ? "" : abr);
	    userElement.appendChild(aux);
	}

	aux = doc.createElement("password");
	aux.setTextContent(getPassword());
	userElement.appendChild(aux);

	NodeList uss = doc.getElementsByTagName("users");
	if (uss.getLength() != 1) {
	    System.out.println("Não encontrou o elemento raiz!");
	    return;
	}
	Node principal = uss.item(0);

	NodeList nl = doc.getElementsByTagName("userid");
	int i = 0;
	for (; i <= nl.getLength(); i++)
	    if (nl.item(i).getTextContent().equals(userId.toString()))
		break;

	principal.removeChild(nl.item(i).getParentNode());

	principal.appendChild(userElement);

	try {
	    XMLDoc.validDocXSD(doc, XMLDoc.getContexto() + file + ".xsd");
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }


    /**
     * Serializa os dados do utilizador para XML, incluindo o perfil completo
     * e os dados da nacionalidade. Usado pelo Skeleton para responder ao <iniciar>.
     *
     * @param simbolo símbolo atribuído ao jogador ('X' ou 'O')
     * @return String XML com o elemento <jogador> completo
     */
    public String toXMLString(char simbolo) throws ParserConfigurationException {
	String ret = "<jogador simbolo='"+simbolo+"'>"
		+ "<userid>"+getUserId().toString()+"</userid>"
		+ "<updated>"+dateTimeToXsd(getUpdated())+"</updated>"
		+ "<blocked>"+isBlocked()+"</blocked>"
		+ "<profile>"+getProfile()+"</profile>"
		+ "<username>"+getUsername()+"</username>"
		+ "<firstnames>"+getFirstNames()+"</firstnames>"
		+ "<lastnames>"+getLastNames()+"</lastnames>";
		if(getEmail()!=null && !getEmail().isBlank())
		    ret+="<email>"+getEmail()+"</email>";
		if(getGender()!=null && !getGender().isBlank())
		    ret+="<gender>"+getGender()+"</gender>";
		if(getGender()!=null && !getGender().isBlank())
		    ret+="<gender>"+getGender()+"</gender>";
		if(getBirthdate()!=null)
		    ret+="<birthdate>"+dateToXsd(getBirthdate())+"</birthdate>";
		if(getPhotography()!=null)
		    ret+="<photography>"+getPhotography().getBase64()+"</photography>";
		ret+="<full-nationality>";
		Nationality nat = getNationality();
		ret +=	"<abbreviation>"+nat.getAbbreviation()+"</abbreviation>"
			+ "<name>"+nat.getName()+"Austria</name>"
			+ "<official>"+nat.getOfficial()+"</official>"
			+ "<pt-name>"+nat.getPtName()+"</pt-name>"
			+ "<pt-nationality>"+nat.getPtNationality()+"</pt-nationality>"
			+ "<pt-male>"+nat.getPtMale()+"</pt-male>"
			+ "<pt-female>"+nat.getPtFemale()+"</pt-female>"
			+ "<flag>"+nat.getFlag().getBase64()+"</flag>";
		ret +=		
			"</full-nationality>"
			  + "<full-name>"+getName()+"</full-name>"
			  + "<age>"+((getAge()==-1)?"":getAge())+"</age>"
		+ "</jogador>";
	return ret;
    }


    /**
     * Preenche os campos desta instância a partir de um elemento XML <user>.
     * Usado ao carregar utilizadores do ficheiro users.xml.
     *
     * @param userElement elemento XML com os dados do utilizador
     */
    public void fromElement(Element userElement) throws Exception {
	setUserId(UUID.fromString(userElement.getElementsByTagName("userid").item(0).getTextContent()));
	setUpdated(xsdToLocalDateTime(userElement.getElementsByTagName("updated").item(0).getTextContent()));
	setBlocked(Boolean.parseBoolean(userElement.getElementsByTagName("blocked").item(0).getTextContent()));
	setProfile(Integer.parseInt(userElement.getElementsByTagName("profile").item(0).getTextContent()));
	setUsername(userElement.getElementsByTagName("username").item(0).getTextContent());
	setPassword(userElement.getElementsByTagName("password").item(0).getTextContent());
	setFirstNames(userElement.getElementsByTagName("firstnames").item(0).getTextContent());
	setLastNames(userElement.getElementsByTagName("lastnames").item(0).getTextContent());
	NodeList nl = userElement.getElementsByTagName("email");
	setEmail((nl.getLength() == 1) ? nl.item(0).getTextContent() : null);
	nl = userElement.getElementsByTagName("gender");
	setGender((nl.getLength() == 1) ? nl.item(0).getTextContent() : null);
	nl = userElement.getElementsByTagName("birthdate");
	setBirthdate((nl.getLength() == 1) ? xsdToLocalDate(nl.item(0).getTextContent()) : null);
	nl = userElement.getElementsByTagName("nationality");
	setNationality((nl.getLength() == 1) ? nl.item(0).getTextContent() : null);
	nl = userElement.getElementsByTagName("photography");
	setPhotography((nl.getLength() == 1) ? nl.item(0).getTextContent() : null);
    }


    public static LocalDate xsdToLocalDate(String xsdDate) {
	if(xsdDate==null || xsdDate.isBlank())
	    return null;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	return LocalDate.parse(xsdDate, formatter);
    }


    public static LocalDateTime xsdToLocalDateTime(String xsdDateTime) throws Exception {
	if(xsdDateTime==null || xsdDateTime.isBlank())
	    return null;
	XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(xsdDateTime);
	return LocalDateTime.ofInstant(xmlCalendar.toGregorianCalendar().toInstant(), ZoneId.systemDefault());
    }


    public static String dateToXsd(LocalDate localDate) {
	if(localDate==null)
	    return "";
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	return localDate.format(formatter);
    }


    public static String dateTimeToXsd(LocalDateTime localDateTime) {// norma ISO 8601
	if(localDateTime==null)
	    return "";
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	return localDateTime.format(formatter);
    }

    private static String capitalizar(String nome) {

	String[] palavras = nome.trim().split(" ");
	StringBuilder capitaliza = new StringBuilder();

	for (String palavra : palavras)
	    if (palavra.length() > 3)
		capitaliza.append(palavra.substring(0, 1).toUpperCase(Locale.ROOT)).append(palavra.substring(1))
			.append(" ");
	    else
		capitaliza.append(palavra).append(" ");

	return capitaliza.toString();
    }

    public static User getUser(String xpath) throws Exception {
	NodeList l = XMLDoc.getXPath(xpath, doc);
	if (l.getLength() == 1) {
	    User us = new User();
	    us.fromElement((Element) l.item(0));
	    return us;
	}
	System.out.println("Não encontrou!");
	return null;
    }


    public static User getByUserId(String userId) throws Exception {
	return getUser("/users/user[userid/text()='" + userId + "']");
    }


    public static User getByUserName(String username) throws Exception {
	return getUser("/users/user[username/text()='" + username + "']");
    }


    public static String askUserName() throws XPathExpressionException {

	System.out.println("Indique as primeiras letras do nome do utilizador: ");
	String inicio = sc.nextLine();

	String xp = "boolean('true')";
	if (inicio.length() != 0) {
	    xp = "starts-with(text(),'" + inicio + "')";
	}

	NodeList pl = XMLDoc.getXPath("/users/user/username[" + xp + "]/text()", doc);

	if (pl.getLength() == 0) {
	    return "";
	}

	if (pl.getLength() == 1) {
	    return pl.item(0).getTextContent();
	}

	List<String> lista = new ArrayList<>();
	for (int i = 0; i < pl.getLength(); i++) {
	    lista.add(pl.item(i).getTextContent());
	}
	lista.sort(Comparator.naturalOrder());
	for (int i = 0; i < lista.size(); i++) {
	    System.out.println((i + 1) + " - " + lista.get(i));
	}

	System.out.println("\nDigite o número associado ao nome do utilizador:");
	int ut = sc.nextInt();

	return lista.get(ut - 1);
    }

    private static void exemplo1() throws Exception {
	String username = askUserName();
	User ur = getByUserName(username);
	ur.print();
    }

    private static void exemplo2() throws XPathExpressionException, NoSuchAlgorithmException, SAXException, IOException,
	    TransformerFactoryConfigurationError, TransformerException {
	String username = askUserName();
	if(username==null || username.isBlank())
	    return;
	System.out.println("Indique o nome do ficheiro na pasta corrente:");
	String fich = null;
	do {
	    fich = sc.nextLine();
	} while (fich == null || fich.length() == 0);
	if (_chgFoto(username, fich))
	    System.out.println("Alteração da fotografia realizada com sucesso!");
	else
	    System.out.println("Falhou a alteração da fotografia!");
	_save();
	_load();
    }

    private static void exemplo3() throws XPathExpressionException, NoSuchAlgorithmException, SAXException, IOException,
	    TransformerFactoryConfigurationError, TransformerException {
	String nome = askUserName();
	if(nome==null || nome.isBlank())
	    return;
	System.out.println("Indique a senha antiga:");
	String senhaAntiga = null;
	do {
	    senhaAntiga = sc.nextLine();
	} while (senhaAntiga == null || senhaAntiga.length() == 0);

	System.out.println("Indique a senha nova:");
	String senhaNova = null;
	do {
	    senhaNova = sc.nextLine();
	} while (senhaNova == null || senhaNova.length() == 0);

	if (_chgPass(nome, senhaAntiga, senhaNova))
	    System.out.println("Alteração da senha realizada com sucesso!");
	else
	    System.out.println("Falhou a alteração da senha!");
	_save();
	_load();
    }

    private static void exemplo4() {
	try {
	    String nome = askUserName();
	    if(nome==null || nome.isBlank())
		return;
	    _block(nome);
	    _save();
	    _load();
	    System.out.println("Bloqueio realizado com sucesso!");
	} catch (XPathExpressionException | SAXException | IOException | TransformerFactoryConfigurationError
		| TransformerException e) {
	    e.printStackTrace();
	}
    }

    private static void exemplo5() {
	try {
	    String nome = askUserName();
	    if(nome==null || nome.isBlank())
		return;
	    _unblock(nome);
	    _save();
	    _load();
	    System.out.println("Desloqueio realizado com sucesso!");
	} catch (XPathExpressionException | SAXException | IOException | TransformerFactoryConfigurationError
		| TransformerException e) {
	    e.printStackTrace();
	}
    }

    private static void exemplo6() throws Exception {
	String nome = askUserName();
	if(nome==null || nome.isBlank())
	    return;
	System.out.println("Indique a senha do utilizador:");
	String senha = null;
	do {
	    senha = sc.nextLine();
	} while (senha == null || senha.length() == 0);

	User login = _authenticate(nome, senha);
	if (login != null) {
	    System.out.println("Autenticação (login) realizada com sucesso!");
	    login.print();
	} else
	    System.out.println("Falhou a autenticação (login)!");
    }
    
    private static void exemplo7() throws Exception {
	String nome = askUserName();
	if(nome==null || nome.isBlank())
	    return;
	User ur = getByUserName(nome);
	Document d=XMLDoc.parseString(ur.toXMLString('X'));
	System.out.println("Nacionalidade: "+d.getElementsByTagName("pt-nationality").item(0).getTextContent());
	System.out.println("Nome: "+d.getElementsByTagName("full-name").item(0).getTextContent());
    }


    public static void menu() {
	char op;
	do {
	    System.out.println();
	    System.out.println();
	    System.out.println("*** Utilizador ***");
	    System.out.println("1 - Consultar utilizador");
	    System.out.println("2 - Alterar fotografia");
	    System.out.println("3 - Alterar senha");
	    System.out.println("4 - Bloquear utilizador");
	    System.out.println("5 - Desbloquear utilizador");
	    System.out.println("6 - Simular autenticação/login");
	    System.out.println("7 - Testar utilizador remoto");
	    System.out.println("8 - ?Atualizar utilizador");
	    System.out.println("0 - Terminar!");
	    String str = null;
	    do {
		str = sc.nextLine();
	    } while (str == null || str.length() == 0);
	    op = str.charAt(0);
	    switch (op) {
	    case '1':
		try {
		    exemplo1();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		break;
	    case '2':
		try {
		    exemplo2();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		break;
	    case '3':
		try {
		    exemplo3();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		break;
	    case '4':
		try {
		    exemplo4();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		break;
	    case '5':
		try {
		    exemplo5();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		break;
	    case '6':
		try {
		    exemplo6();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		break;
	    case '7':
		try {
		    exemplo7();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		break;
	    default:
		System.out.println("Opção inválida, esolha uma opção do menu.");
	    }
	} while (op != '0');
	System.out.println("Terminou a execução.");
	System.exit(0);
    }


    /**
     * Autentica um utilizador pelo username e password.
     * Usa XPath para verificar username + hash SHA-256 da password + não bloqueado.
     *
     * @param username nome de utilizador
     * @param password senha em texto simples (convertida para SHA-256 internamente)
     * @return instância User se autenticado com sucesso, null caso contrário
     */
    public static User _authenticate(String username, String password) throws Exception {


	NodeList us = XMLDoc.getXPath("/users/user[username/text()='" + username + "' and password/text()='"
		+ XMLDoc.SHA256(password) + "' and blocked/text()='false']/userid/text()", doc);


	if (us.getLength() != 1) {
	    return null;
	}

	return getByUserName(username);
    }


    /**
     * Altera a senha de um utilizador, verificando primeiro a senha antiga.
     *
     * @param username    nome de utilizador
     * @param senhaAntiga senha actual (verificada contra o hash guardado)
     * @param senhaNova   nova senha (será guardada como SHA-256)
     * @return true se a alteração foi bem-sucedida
     */
    public static boolean _chgPass(String username, String senhaAntiga, String senhaNova)
	    throws XPathExpressionException, NoSuchAlgorithmException {

	NodeList us = XMLDoc.getXPath("/users/user[username/text()='" + username + "' and password/text()='"
		+ XMLDoc.SHA256(senhaAntiga) + "']/password", doc);

	if (us.getLength() != 1) {
	    return false;
	}

	us.item(0).setTextContent(XMLDoc.SHA256(senhaNova));

	return true;
    }


    public static boolean _chgFoto(String username, String file) throws XPathExpressionException {

	NodeList us = XMLDoc.getXPath("/users/user[username/text()='" + username + "']/photography", doc);

	if (us.getLength() != 1) {
	    us = XMLDoc.getXPath("/users/user[username/text()='" + username + "']", doc);
	    if (us.getLength() != 1)
		return false;
	    NodeList nat = XMLDoc.getXPath("/users/user[username/text()='" + username + "']/nationality", doc);
	    if (nat.getLength() == 1)
		us.item(0).insertBefore(doc.createElement("photography"), nat.item(0));
	    else
		us.item(0).appendChild(doc.createElement("photography"));
	    us = XMLDoc.getXPath("/users/user[username/text()='" + username + "']/photography", doc);
	}

	MyImage f = new MyImage(XMLDoc.getContexto() + file);
	if (!f.isOk())
	    return false;
	us.item(0).setTextContent(f.getBase64());

	return true;
    }


    public static void lock(String username, String valor) throws XPathExpressionException {
	NodeList us = XMLDoc.getXPath("/users/user[username/text()='" + username + "']/blocked", doc);

	if (us.getLength() == 1) {
	    us.item(0).setTextContent(valor);
	} else {
	    throw new XPathExpressionException("Utilizador não encontrado: " + username);
	}
    }


    public static void _block(String username) throws XPathExpressionException {
	lock(username, "true");
    }


    public static void _unblock(String username) throws XPathExpressionException {
	lock(username, "false");
    }


    public static User _obtain(String username) throws Exception {

	NodeList us = doc.getElementsByTagName("username");

	if (us.getLength() == 1) {

	    Element pai = (Element) us.item(0).getParentNode();

	    User ret = new User();

	    ret.fromElement(pai);

	    return ret;

	} else {

	    throw new Exception("Utilizador não encontrado: " + username);
	}
    }

    public static void _replace(User user) throws ParserConfigurationException, SAXException, IOException,
	    TransformerFactoryConfigurationError, TransformerException, XPathExpressionException {

	NodeList us = XMLDoc.getXPath("/users/user[userid/text()='" + user.getUserId() + "']", doc);


	NodeList nl = doc.getElementsByTagName("users");
	if (nl.getLength() != 1) {
	    System.out.println("Não encontrou o elemento raiz!");
	    return;
	}
	Node principal = nl.item(0);


	if (us.getLength() == 1) {
	    principal.removeChild(us.item(0));
	} else
	    user.toDocument();
	_save();
    }

    /**
     * Valida o documento XML contra o XSD e grava-o no disco com backup automático.
     * Deve ser chamado após qualquer alteração ao documento em memória.
     */
    public static void _save()
	    throws SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {


	XMLDoc.validDocXSD(doc, XMLDoc.getContexto() + file + ".xsd");

	String docXML = XMLDoc.getContexto() + file + ".xml";

	String backup = XMLDoc.gerarNomeFBackupVersao(docXML);


	XMLDoc.gravarLock(doc, docXML, backup);
    }
 

    public static void main(final String[] args) {
    	try {
			System.out.println("-->"+XMLDoc.SHA256("p9"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	menu();
    }
}