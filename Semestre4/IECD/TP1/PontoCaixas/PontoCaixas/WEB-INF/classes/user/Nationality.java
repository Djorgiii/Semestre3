package user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.MyImage;
import util.XMLDoc;

/**
 * Classe que manipula atributos de nacionalidade e países
 * Norma: ISO Country Codes Alpha-2 ver https://www.iban.com/country-codes
 */
public class Nationality {
    // Nome de ficheiro associado às nacionalidades
    private static String file = "nationalities";
    // Carrega o ficheiro com as nacionalidades
    private static Document doc=load();
    
    private String abbreviation=null;	// abreviatura/código 2 letras
    private String name=null;		// país
    private String official=null;	// designação official
    
    // Nacionalidade em português
    private String ptName=null;		// país
    private String ptNationality=null;	// nacionalidade
    private String ptMale=null;		// se for homem
    private String ptFemale=null;		// se for mulher
    
    private MyImage flag=null;		// bandeira
    
    final static Scanner sc = new Scanner(System.in);

    // Carrega e valida as nacionalidades
    private static Document load() {
	Document d=XMLDoc.parseFile(XMLDoc.getContexto()+file+".xml");
	try {
	    XMLDoc.validDocXSD(d, XMLDoc.getContexto()+file+".xsd");
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return d;
    }
    
    // Getters and setters for each field

    /**
     * @return abbreviation
     */
    public String getAbbreviation() {
	return abbreviation;
    }

    /**
     * @param abbreviation abreviatura
     * @return sucesso
     */
    public boolean setAbbreviation(String abbreviation) {
	if(abbreviation==null)
	    return false;
	if(abbreviation.length()!=2)
	    return false;
	this.abbreviation = abbreviation;
	return true;
    }
 
    /**
     * @return nome
     */
    public String getName() {
	return name;
    }

    /**
     * @param name nome
     * @return sucesso
     */
    public boolean setName(String name) {
	if (name == null)
	    return false;
	this.name = name;
	return true;
    }

    /**
     * @return designação oficial do país
     */
    public String getOfficial() {
	return official;
    }

    /**
     * @param official designação oficial do país
     * @return sucesso
     */
    public boolean setOfficial(String official) {
	if (official == null)
	    return false;
	this.official = official;
	return true;
    }

    /**
     * @return ptName nome do país em português
     */
    public String getPtName() {
	return ptName;
    }

    /**
     * @param ptName nome do país em português
     * @return sucesso
     */
    public boolean setPtName(String ptName) {
	if (ptName == null)
	    return false;
	this.ptName = ptName;
	return true;
    }

    /**
     * @return nacionalidade em português
     */
    public String getPtNationality() {
	return ptNationality;
    }

    /**
     * @param ptNationality nacionalidade em português
     * @return sucesso
     */
    public boolean setPtNationality(String ptNationality) {
	if (ptNationality == null)
	    return false;
	this.ptNationality = ptNationality;
	return true;
    }

    /**
     * @return nacionalidade em português se for homem
     */
    public String getPtMale() {
	return ptMale;
    }

    /**
     * @param ptMale nacionalidade em português se for homem
     * @return sucesso
     */
    public boolean setPtMale(String ptMale) {
	if (ptMale == null)
	    return false;
	this.ptMale = ptMale;
	return true;
    }

    /**
     * @return nacionalidade em português se for mulher
     */
    public String getPtFemale() {
	return ptFemale;
    }
    
    /**
     * @param ptFemale nacionalidade em português se for mulher
     * @return sucesso
     */
    public boolean setPtFemale(String ptFemale) {
	if(ptFemale==null)
	    return false;
	this.ptFemale = ptFemale;
	return true;
    }
    
    /**
     * @return a imagem que representa a bandeira
     */
    public MyImage getFlag() {
	return flag;
    }

    /**
     * @param flag imagem que representa a bandeira
     * @return sucesso
     */
    public boolean setFlag(MyImage flag) {
	if(flag==null)
	    return false;
	this.flag = flag;
	return true;
    }
 
    /**
     * Construtor por omissão
     */
    public Nationality() {
    }
    
    /**
     * Optional constructor (assuming you might not always have a flag)
     * @param abbreviation	código ISO
     * @param name		designação do país
     * @param official		designação oficial do país
     * @param ptName 		designação do país em portugês
     * @param ptNationality 	designação da nacioalidade em portugês
     * @param ptMale 		designação, masculina, da nacioalidade em portugês
     * @param ptFemale 		designação, feminina, da nacioalidade em portugês
     * @throws Exception 	em caso de erro
     */
    public Nationality(String abbreviation, String name, String official, String ptName, String ptNationality, String ptMale, String ptFemale) throws Exception {
	if (!setAbbreviation(abbreviation)||
		!setName(name)||
		!setOfficial(official)||
		!setPtName(ptName)||
		!setPtNationality(ptNationality)||
		!setPtMale(ptMale)||
		!setPtFemale(ptFemale))
			throw new Exception("Foram indicados valores inválidos");
    }
    
    /**
     * Escreve no ecrã
     * 
     * @throws Exception em caso de erro
     */
    public void print() throws Exception {
	System.out.println("---------- Nacionalidade -----------");
	System.out.println("Abreviatura: " + abbreviation);
	System.out.println("País: " + name);
	System.out.println("Oficial: " + official);
	System.out.println("Em português: ");
	System.out.println("    País: " + ptName);
	System.out.println("    Nacionalidade: " + ptNationality);
	System.out.println("            Homem: " + ptMale);
	System.out.println("           Mulher: " + ptFemale);
	System.out.println("------------------------------------");
	if(flag!=null)
	    flag.view();
	
    }

    /**
     * Devolve o string que representa a nacionalidade em português em conformidade com o género
     * @param gender género do utilizador 'M', 'F' ou 'X'
     * @return designação da nacionalidade em português 
     */
     public String pt(String gender) {
	if(gender==null)
	    return "";
	if(gender.equals("F"))
	    return ptFemale;
	if(gender.equals("M"))
	    return ptMale;
	return ptNationality;
     }
     
    /**
     * Atualiza o modelo DOM com a nacionalidade atual
     */
    private void toDocument()  {
	Element nationalityElement = doc.createElement("nationality");

	// Cria elementos filhos com os dados da nacionalidade
	Element abbreviationElement = doc.createElement("abbreviation");
	abbreviationElement.setTextContent(abbreviation);
	nationalityElement.appendChild(abbreviationElement);

	Element nameElement = doc.createElement("name");
	nameElement.setTextContent(name);
	nationalityElement.appendChild(nameElement);

	Element officialElement = doc.createElement("official");
	officialElement.setTextContent(official);
	nationalityElement.appendChild(officialElement);
	
	Element ptNameElement = doc.createElement("pt-name");
	ptNameElement.setTextContent(ptName);
	nationalityElement.appendChild(ptNameElement);
	
	Element ptNationalityElement = doc.createElement("pt-nationality");
	ptNationalityElement.setTextContent(ptNationality);
	nationalityElement.appendChild(ptNationalityElement);
	
	Element ptMaleElement = doc.createElement("pt-male");
	ptMaleElement.setTextContent(ptMale);
	nationalityElement.appendChild(ptMaleElement);
	
	Element ptFemaleElement = doc.createElement("pt-female");
	ptFemaleElement.setTextContent(ptFemale);
	nationalityElement.appendChild(ptFemaleElement);

	// Se a bandeira estiver definida, cria um elemento "flag"
	if (flag != null) {
	    Element flagElement = doc.createElement("flag");
	    flagElement.setTextContent(flag.getBase64());
	    nationalityElement.appendChild(flagElement);
	} else
	    System.out.println("Não existe bandeira definida!");
	
	// Procura o Nó principal
	NodeList nats = doc.getElementsByTagName("nationalities");
	if(nats.getLength()!=1) {
	    System.out.println("Não encontrou o elemento raiz!");
	    return;		// erro, inconsistencia
	}
	Node principal = nats.item(0);
	
	// Procura o Nó correpondente à nacionalidade atual
	NodeList nl = doc.getElementsByTagName("abbreviation");
	int i=0;
	for(; i<= nl.getLength(); i++)
	    if(nl.item(i).getTextContent().equals(abbreviation))
		break;
	
	// Remove a nacionalidade antiga
	principal.removeChild(nl.item(i).getParentNode());
	
	// Acrescenta a nacionalidade atual
	principal.appendChild(nationalityElement);
	
	try {// para prevenir algum bug
	    XMLDoc.validDocXSD(doc, XMLDoc.getContexto()+file+".xsd");
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Atualiza a nacionalidade atual a partir do elemento indicado em parametro
     * @param nationalityElement Atualiza nacionalidade atual
     */
    public void fromElement(Element nationalityElement) {
	abbreviation = nationalityElement.getElementsByTagName("abbreviation").item(0).getTextContent();
	name = nationalityElement.getElementsByTagName("name").item(0).getTextContent();
	official = nationalityElement.getElementsByTagName("official").item(0).getTextContent();
	
	ptName = nationalityElement.getElementsByTagName("pt-name").item(0).getTextContent();
	ptNationality = nationalityElement.getElementsByTagName("pt-nationality").item(0).getTextContent();
	ptMale = nationalityElement.getElementsByTagName("pt-male").item(0).getTextContent();
	ptFemale = nationalityElement.getElementsByTagName("pt-female").item(0).getTextContent();

	// Handle optional flag
	Node flagNode = nationalityElement.getElementsByTagName("flag").item(0);
	if (flagNode != null) {
	    flag = new MyImage();
	    flag.setBase64(flagNode.getTextContent().replaceAll("[\\s]", ""));
	}
    }
    

    /**
     * Retorna uma única nacionalidade (ou null) que respeite a expressão em argumento
     * @param xpath expressao que vai ser avaliada
     * @return a nacionalidade
     * @throws XPathExpressionException em caso de erro
     */
    public static Nationality getNationality(String xpath) throws XPathExpressionException {
	NodeList l = XMLDoc.getXPath(xpath, doc);
	if(l.getLength()==1) {
	    Nationality nat = new Nationality();
	    nat.fromElement((Element)l.item(0));
	    return nat;
	}
	return null;
    }
    

    /**
     * Retorna a nacionalidade com a abreviatura indicada
     * @param abbreviation abreviatura da nacionalidade
     * @return nacionalidade
     * @throws XPathExpressionException em caso de erro
     */
    public static Nationality getByAbbreviation(String abbreviation) throws XPathExpressionException {
	return getNationality("/nationalities/nationality[abbreviation/text()='"+abbreviation+"']");
    }
    
    /**
     * Retorna a nacionalidade que correponde ao país em português indicado
     * @param ptName	designação do país em português
     * @return		a nacionalidade
     * @throws XPathExpressionException em caso de erro
     */
    public static Nationality getByPtName(String ptName) throws XPathExpressionException {
	return getNationality("/nationalities/nationality[pt-name/text()='"+ptName+"']");
    }
    
    /**
     * Apresenta lista de país com letras iniciais da designação do país em português
     * e retorna a hipotese indicada pelo utilizador
     * @return O nome do país selecionado pelo utilizador
     * @throws XPathExpressionException em caso de erro
     */
    public static String askPais() throws XPathExpressionException {
	System.out.println("Indique as primeiras letras (português) do nome do país: ");
	String inicioPais = sc.nextLine();
	if(inicioPais.length()==0)
	    return "";
	inicioPais = inicioPais.toUpperCase().charAt(0) + inicioPais.toLowerCase().substring(1);
	NodeList pl = XMLDoc.getXPath("/nationalities/nationality/pt-name[starts-with(text(),'"+inicioPais+"')]/text()", doc);
	if(pl.getLength()==0)
	    return "";
	if(pl.getLength()==1)
	    return pl.item(0).getTextContent();
	List<String> lista = new ArrayList<>();
	for(int i=0;i<pl.getLength(); i++)
	    lista.add(pl.item(i).getTextContent());
	lista.sort(Comparator.naturalOrder());
	for(int i=0;i<lista.size(); i++)
	    System.out.println((i+1)+" - "+lista.get(i));
        System.out.println("Digite o número associado ao país:");
        int pais = sc.nextInt();
	return lista.get(pais-1);
    }
    
    private static String exemplo1() throws Exception {
	String pais = askPais();
	System.out.println("Selecionou o pais: "+pais);
	Nationality nat = getByPtName(pais);
	nat.print();
	return pais;
    }
    
    private static void exemplo2() throws Exception {
	String pais = askPais();
	System.out.println("Selecionou o país: "+pais);
	if(pais==null || pais.length()==0)
	    return;
	System.out.println("Indique o nome do ficheiro (png) que tem a bandeira:");
	String bandeira=sc.nextLine();
	MyImage flag = new MyImage(XMLDoc.getContexto()+bandeira);
	Nationality nat = getByPtName(pais);
	nat.setFlag(flag);
	//nat.print();
	// Atualiza o DOM
	nat.toDocument();
	//Nationality nat2 = getByPtName(pais);
	//nat2.print();
	String backup = XMLDoc.gerarNomeFBackupVersao(XMLDoc.getContexto()+file+".xml");
	// Atualiza o ficheiro
	System.out.println("Ficheiro de backup: "+backup);
	XMLDoc.gravarLock(doc, XMLDoc.getContexto()+file+".xml", backup);
	// Para confirmar: Carregar o ficheiro que foi gravado
	load();
	// Procura novamente o país e mostra a bandeira atualizada
	Nationality nat3 = getByPtName(pais);
	nat3.print();
    }
    
    /**
     * Menu para demonstração desta classe
     */
    public static void menu() {
	char op;
	
	do {
	    System.out.println();
	    System.out.println();
	    System.out.println("*** Nacionalidade ***");
	    System.out.println("1 - Consultar a nacionalidade");
	    System.out.println("2 - Alterar a bandeira");
	    System.out.println("0 - Terminar!");
	    String str = sc.nextLine();
	    if (str != null && str.length() > 0)
		op = str.charAt(0);
	    else
		op = ' ';
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
	    default:
		System.out.println("Opção inválida, esolha uma opção do menu.");
	    }
	} while (op != '0');
	sc.close();
	System.out.println("Terminou a execução.");
	System.exit(0);
    }
    // 
    /**
     * Demonstra como se pode usar esta classe
     * @param args não usado
     * @throws Exception em caso de erro
     */
    public static void main(final String[] args) throws Exception {
	menu();
	
	/*
	String abbreviation="PT";
	String name="Portugal";
	String official="The Portuguese Republic";
	String ptName="Portugal";
	String ptNationality="português(esa)";
	String ptMale="português";
	String ptFemale="portuguesa";
	Nationality nat = new Nationality(abbreviation,name,official,ptName,ptNationality,ptMale,ptFemale);
	*/
	
    }
}