package server;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import org.w3c.dom.*;
import user.User;
import util.XMLDoc;

public class Skeleton {

    private static Document getNext(BufferedReader is) throws Exception {
        String line = is.readLine();
        // XMLDoc.validDocXSD(XMLDoc.parseString(line), XMLDoc.getContexto() + "metodos-srv.xsd");
        return XMLDoc.parseString(line); 
    }

    public static void runIniciar(Socket sk, char simbolo) throws Exception {
        BufferedReader is = new BufferedReader(new InputStreamReader(sk.getInputStream()));
        PrintWriter os = new PrintWriter(sk.getOutputStream(), true);
        Document x = getNext(is);
        String Nome = ((Element)x.getElementsByTagName("iniciar").item(0)).getAttribute("nickname");
        String Senha = ((Element)x.getElementsByTagName("iniciar").item(0)).getAttribute("senha");
        
        User jg = User._authenticate(Nome, Senha);
        if(jg == null) throw new Exception("Falhou autenticação!");
        
        Document d = XMLDoc.parseString(jg.toXMLString(simbolo));
        Node jogador = d.getElementsByTagName("jogador").item(0);
        x.getElementsByTagName("iniciar").item(0).appendChild(x.importNode(jogador, true));
        os.println(XMLDoc.documentToString(x));
    }
    
    public static void runObter(BufferedReader is, PrintWriter os, char simbolo, Socket sk, JogoXML jogo) throws Exception {
        getNext(is); // consome tag
        os.println("<metodo><obter>" + jogo.tabuleiroToXML() + "</obter></metodo>");
    }

    public static JogoXML runJogar(BufferedReader is, PrintWriter os, char simbolo, Socket sk, JogoXML jogo) throws Exception {
        Document jogar = getNext(is);
        Element jogadaEl = (Element) jogar.getElementsByTagName("jogar").item(0);
        String jogadaStr = jogadaEl.getAttribute("jogada");
        String[] partes = jogadaStr.trim().split("\\s+");

        try {
            if (partes.length == 4) {
                int[] coords = new int[]{Integer.parseInt(partes[0]), Integer.parseInt(partes[1]), Integer.parseInt(partes[2]), Integer.parseInt(partes[3])};
                jogo.joga(coords, simbolo);
            } else { jogo.setEstado("IV"); }
        } catch (Exception e) { jogo.setEstado("IV"); }

        os.println(XMLDoc.documentToString(jogar));       
        return jogo;
    }
}