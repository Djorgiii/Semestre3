package client;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import org.w3c.dom.*;
import util.XMLDoc;

public class Stub implements AutoCloseable {
    private BufferedReader is = null;
    private PrintWriter os = null;
    private Document registo = null;

    public Stub(Socket sk) throws IOException {
        is = new BufferedReader(new InputStreamReader(sk.getInputStream()));
        os = new PrintWriter(sk.getOutputStream(), true);
    }
    
    @Override
    public void close() {
        try { is.close(); } catch (IOException e) {}
        os.close();
    }

    public String tabuleiroToTXT(final Element tabuleiro) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Tabuleiro de Pontos e Caixas ---\n");
        NodeList linhas = tabuleiro.getElementsByTagName("linha");
        NodeList caixas = tabuleiro.getElementsByTagName("caixa");
        sb.append("Linhas desenhadas: ").append(linhas.getLength()).append("\n");
        sb.append("Caixas fechadas: ").append(caixas.getLength()).append("\n");
        return sb.toString();
    }

    public String estadoToTXT(final String valor) {
        switch (valor) {
            case "VX": return "🏆 Vitória do Jogador X!";
            case "VO": return "🏆 Vitória do Jogador O!";
            case "EM": return "🤝 Empate.";
            case "IV": return "❌ Jogada inválida! (Tenta de novo)";
            case "BO": return "🔥 BÓNUS! Fechaste uma caixa, joga outra vez!";
            default: return ""; 
        }
    }
    
    private void validXSD(final Document doc) throws Exception {
        // XMLDoc.validDocXSD(doc, XMLDoc.getContexto() + "metodos-cli.xsd");
    }
    
    private static void registaLog(String evento) {
        try (PrintWriter esc = new PrintWriter(new BufferedWriter(new FileWriter(XMLDoc.getContexto()+"protocolo.log", true)))) {
            esc.println(LocalDateTime.now() + " - " + evento.replaceAll("\n",""));
        } catch (IOException e) {}
    }
    
    public void print() {
        if(registo==null) return;
        System.out.println("--- Autenticado com sucesso ---");
    }

    public char iniciar(final String user, final String pass) throws Exception {
        os.println("<metodo><iniciar nickname='" + user + "' senha='" + pass + "'/></metodo>");
        String resposta = is.readLine();
        registaLog("Cliente{"+resposta+"}");
        if(resposta==null) throw new Exception("Ligação cancelada!");
        registo = XMLDoc.parseString(resposta);
        validXSD(registo);
        return ((Element)registo.getElementsByTagName("jogador").item(0)).getAttribute("simbolo").charAt(0);
    }

    public Element obter() throws Exception {
        os.println("<metodo><obter/></metodo>");
        String resposta = is.readLine();
        registaLog("Cliente{"+resposta+"}");
        if(resposta==null) throw new Exception("Ligação cancelada!");
        Document d = XMLDoc.parseString(resposta);
        validXSD(d);
        return (Element) d.getElementsByTagName("tabuleiro").item(0);
    }

    public void jogar(final String coordenadas) throws Exception {
        os.println("<metodo><jogar jogada='" + coordenadas + "'/></metodo>");
        String resposta = is.readLine();
        registaLog("Cliente{"+resposta+"}");
        if(resposta==null) throw new Exception("Ligação cancelada!");
        validXSD(XMLDoc.parseString(resposta));
    }
}