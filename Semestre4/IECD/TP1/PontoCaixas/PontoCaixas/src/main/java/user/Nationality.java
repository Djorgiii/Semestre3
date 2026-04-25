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

public class Nationality {
    private static String file = "nationalities";
    private static Document doc=load();
    
    private String abbreviation=null;
    private String name=null;
    private String official=null;
    
    private String ptName=null;
    private String ptNationality=null;
    private String ptMale=null;
    private String ptFemale=null;
    
    private MyImage flag=null;
    
    final static Scanner sc = new Scanner(System.in);

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
    

    public String getAbbreviation() {
	return abbreviation;
    }

    public boolean setAbbreviation(String abbreviation) {
	if(abbreviation==null)
	    return false;
	if(abbreviation.length()!=2)
	    return false;
	this.abbreviation = abbreviation;
	return true;
    }
 
    public String getName() {
	return name;
    }

    public boolean setName(String name) {
	if (name == null)
	    return false;
	this.name = name;
	return true;
    }


    public String getOfficial() {
	return official;
    }


    public boolean setOfficial(String official) {
	if (official == null)
	    return false;
	this.official = official;
	return true;
    }


    public String getPtName() {
	return ptName;
    }


    public boolean setPtName(String ptName) {
	if (ptName == null)
	    return false;
	this.ptName = ptName;
	return true;
    }

    public String getPtNationality() {
	return ptNationality;
    }


    public boolean setPtNationality(String ptNationality) {
	if (ptNationality == null)
	    return false;
	this.ptNationality = ptNationality;
	return true;
    }


    public String getPtMale() {
	return ptMale;
    }


    public boolean setPtMale(String ptMale) {
	if (ptMale == null)
	    return false;
	this.ptMale = ptMale;
	return true;
    }


    public String getPtFemale() {
	return ptFemale;
    }
    

    public boolean setPtFemale(String ptFemale) {
	if(ptFemale==null)
	    return false;
	this.ptFemale = ptFemale;
	return true;
    }
    

    public MyImage getFlag() {
	return flag;
    }


    public boolean setFlag(MyImage flag) {
	if(flag==null)
	    return false;
	this.flag = flag;
	return true;
    }
 

    public Nationality() {
    }
    

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


     public String pt(String gender) {
	if(gender==null)
	    return "";
	if(gender.equals("F"))
	    return ptFemale;
	if(gender.equals("M"))
	    return ptMale;
	return ptNationality;
     }
     

    private void toDocument()  {
	Element nationalityElement = doc.createElement("nationality");

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

	if (flag != null) {
	    Element flagElement = doc.createElement("flag");
	    flagElement.setTextContent(flag.getBase64());
	    nationalityElement.appendChild(flagElement);
	} else
	    System.out.println("Não existe bandeira definida!");
	
	NodeList nats = doc.getElementsByTagName("nationalities");
	if(nats.getLength()!=1) {
	    System.out.println("Não encontrou o elemento raiz!");
	    return;
	}
	Node principal = nats.item(0);
	
	NodeList nl = doc.getElementsByTagName("abbreviation");
	int i=0;
	for(; i<= nl.getLength(); i++)
	    if(nl.item(i).getTextContent().equals(abbreviation))
		break;
	
	principal.removeChild(nl.item(i).getParentNode());
	
	principal.appendChild(nationalityElement);
	
	try {
	    XMLDoc.validDocXSD(doc, XMLDoc.getContexto()+file+".xsd");
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void fromElement(Element nationalityElement) {
	abbreviation = nationalityElement.getElementsByTagName("abbreviation").item(0).getTextContent();
	name = nationalityElement.getElementsByTagName("name").item(0).getTextContent();
	official = nationalityElement.getElementsByTagName("official").item(0).getTextContent();
	
	ptName = nationalityElement.getElementsByTagName("pt-name").item(0).getTextContent();
	ptNationality = nationalityElement.getElementsByTagName("pt-nationality").item(0).getTextContent();
	ptMale = nationalityElement.getElementsByTagName("pt-male").item(0).getTextContent();
	ptFemale = nationalityElement.getElementsByTagName("pt-female").item(0).getTextContent();

	Node flagNode = nationalityElement.getElementsByTagName("flag").item(0);
	if (flagNode != null) {
	    flag = new MyImage();
	    flag.setBase64(flagNode.getTextContent().replaceAll("[\\s]", ""));
	}
    }
    

    public static Nationality getNationality(String xpath) throws XPathExpressionException {
	NodeList l = XMLDoc.getXPath(xpath, doc);
	if(l.getLength()==1) {
	    Nationality nat = new Nationality();
	    nat.fromElement((Element)l.item(0));
	    return nat;
	}
	return null;
    }
    

    public static Nationality getByAbbreviation(String abbreviation) throws XPathExpressionException {
	return getNationality("/nationalities/nationality[abbreviation/text()='"+abbreviation+"']");
    }
    

    public static Nationality getByPtName(String ptName) throws XPathExpressionException {
	return getNationality("/nationalities/nationality[pt-name/text()='"+ptName+"']");
    }
    

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
	nat.toDocument();
	String backup = XMLDoc.gerarNomeFBackupVersao(XMLDoc.getContexto()+file+".xml");
	System.out.println("Ficheiro de backup: "+backup);
	XMLDoc.gravarLock(doc, XMLDoc.getContexto()+file+".xml", backup);
	load();
	Nationality nat3 = getByPtName(pais);
	nat3.print();
    }
    

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

    public static void main(final String[] args) throws Exception {
	menu();
	
	
    }
}