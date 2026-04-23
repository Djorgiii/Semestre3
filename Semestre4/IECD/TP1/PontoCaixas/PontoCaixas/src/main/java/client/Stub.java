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
import util.MyImage;
import util.XMLDoc;

public class Stub implements AutoCloseable {
    private BufferedReader is = null;
    private PrintWriter os = null;
    private Document registo=null;

    public Stub(Socket sk) throws IOException {
        is = new BufferedReader(new InputStreamReader(sk.getInputStream()));
        os = new PrintWriter(sk.getOutputStream(), true);
    }
    
    @Override
    public void close() {
        try { is.close(); } catch (IOException e) {}
        os.close();
    }

    /**
     * Desenha um tabuleiro de Pontos e Caixas com base nas tags <linha> e <caixa>.
     * NOTA: Este método assume que o XML gerado pelo JogoXML é coerente com a grelha.
     */
    public String tabuleiroToTXT(final Element tabuleiro) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- TABULEIRO DE JOGO ---\n\n");

        NodeList linhas = tabuleiro.getElementsByTagName("linha");
        NodeList caixas = tabuleiro.getElementsByTagName("caixa");

        // Tamanho padrão do tabuleiro (4x4 pontos = 3x3 caixas).
        // Ele cresce automaticamente se as coordenadas passarem de 4.
        int maxX = 4; 
        int maxY = 4;
        
        // Matrizes para guardar onde estão as linhas e os donos das caixas
        boolean[][] hLines = new boolean[20][20]; // Linhas horizontais
        boolean[][] vLines = new boolean[20][20]; // Linhas verticais
        char[][] donoCaixa = new char[20][20];

        // Inicializar o meio das caixas vazio
        for(int i = 0; i < 20; i++) for(int j = 0; j < 20; j++) donoCaixa[i][j] = ' ';

        // 1. Ler as Linhas do XML para a memória
        for (int i = 0; i < linhas.getLength(); i++) {
            Element l = (Element) linhas.item(i);
            int x1 = Integer.parseInt(l.getAttribute("x1"));
            int y1 = Integer.parseInt(l.getAttribute("y1"));
            int x2 = Integer.parseInt(l.getAttribute("x2"));
            int y2 = Integer.parseInt(l.getAttribute("y2"));
            
            maxX = Math.max(maxX, Math.max(x1, x2));
            maxY = Math.max(maxY, Math.max(y1, y2));

            if (y1 == y2) { // É uma linha horizontal (-)
                int minX = Math.min(x1, x2);
                hLines[y1][minX] = true;
            } else if (x1 == x2) { // É uma linha vertical (|)
                int minY = Math.min(y1, y2);
                vLines[minY][x1] = true;
            }
        }

        // 2. Ler as Caixas Fechadas para a memória
        for (int i = 0; i < caixas.getLength(); i++) {
            Element c = (Element) caixas.item(i);
            int x = Integer.parseInt(c.getAttribute("x"));
            int y = Integer.parseInt(c.getAttribute("y"));
            String dono = c.getAttribute("dono");
            donoCaixa[y][x] = dono.charAt(0);
        }

        // 3. DESENHAR A GRELHA NO ECRÃ
        // Imprimir a régua do Eixo X no topo (1 2 3 4...)
        sb.append("    ");
        for (int x = 1; x <= maxX; x++) sb.append(x).append("   ");
        sb.append("\n");

        for (int y = 1; y <= maxY; y++) {
            // Desenhar os pontos (*) e as linhas horizontais (---)
            sb.append(String.format("%2d ", y)); // Imprimir número do Eixo Y
            for (int x = 1; x <= maxX; x++) {
                sb.append("*");
                if (x < maxX) {
                    sb.append(hLines[y][x] ? "---" : "   ");
                }
            }
            sb.append("\n");

            // Desenhar as linhas verticais (|) e as Letras dos Donos (X / O) no meio
            if (y < maxY) {
                sb.append("   ");
                for (int x = 1; x <= maxX; x++) {
                    sb.append(vLines[y][x] ? "|" : " ");
                    if (x < maxX) {
                        sb.append(" ").append(donoCaixa[y][x]).append(" ");
                    }
                }
                sb.append("\n");
            }
        }
        
        sb.append("\nCaixas fechadas: ").append(caixas.getLength()).append("\n");
        return sb.toString();
    }

    public String estadoToTXT(final String valor) {
        switch (valor) {
        case "VA": return "🏆 Vitória do Jogador A!";
        case "VB": return "🏆 Vitória do Jogador B!";
        case "EM": return "🤝 Empate.";
        case "IV": return "❌ Jogada inválida! (Linha já existe ou pontos inválidos)";
        case "BO": return "🔥 BÓNUS! Fechaste uma caixa, joga outra vez!";
        default: return ""; 
        }
    }
    
    private void validXSD(final Document doc) throws Exception {
        // try { XMLDoc.validDocXSD(doc, XMLDoc.getContexto() + "metodos-cli.xsd"); } 
        // catch (SAXException | IOException e) { throw new Exception("XML Inválido: " + e.getMessage()); }
    }
    
    private static void registaLog(String evento) {
        try (PrintWriter escritor = new PrintWriter(new BufferedWriter(new FileWriter(XMLDoc.getContexto()+"protocolo.log", true)))) {
            escritor.println(LocalDateTime.now() + " - " + evento.replaceAll("\n",""));
        } catch (IOException e) {}
    }
    
    public void print() {
        if(registo==null) return;
        System.out.println("--- Autenticado com sucesso ---");
        
        try {
            // 1. Extrai o nome e a String gigante em Base64
            String username = registo.getElementsByTagName("username").item(0).getTextContent();
            String fotoBase64 = registo.getElementsByTagName("photography").item(0).getTextContent();
            
            // 2. A FORMA CORRETA de usar a MyImage do teu professor:
            MyImage img = new MyImage();       // Cria o objeto vazio
            img.setBase64(fotoBase64);         // Converte o texto gigante para Bytes (magia acontece aqui!)
            
            String nomeFicheiro = username + "_perfil.jpg";
            img.save(nomeFicheiro);            // Guarda os bytes no disco rígido
            
            System.out.println("📸 Fotografia descarregada: " + nomeFicheiro);
            System.out.println("Caminho: " + new java.io.File(nomeFicheiro).getAbsolutePath());
            
        } catch (Exception e) {
            System.out.println("⚠️ Utilizador sem fotografia de perfil.");
        }
    }

    public char iniciar(final String user, final String pass) throws Exception {
        os.println("<metodo><iniciar nickname='" + user + "' senha='" + pass + "'/></metodo>");
        String resposta = is.readLine();
        registaLog("Cliente{"+resposta+"}");
        if(resposta==null) throw new Exception("Ligação cancelada!");
        registo = XMLDoc.parseString(resposta);
        validXSD(registo);
        NodeList jogadores = registo.getElementsByTagName("jogador");
        return ((Element)jogadores.item(0)).getAttribute("simbolo").charAt(0);
    }

    public Element obter() throws Exception {
        os.println("<metodo><obter/></metodo>");
        String resposta=is.readLine();
        registaLog("Cliente{"+resposta+"}");
        if(resposta==null) throw new Exception("Ligação cancelada!");
        Document d = XMLDoc.parseString(resposta);
        validXSD(d);
        return (Element) d.getElementsByTagName("tabuleiro").item(0);
    }

    public void jogar(final String coordenadas) throws Exception {
        os.println("<metodo><jogar jogada='" + coordenadas + "'/></metodo>");
        String resposta=is.readLine();
        registaLog("Cliente{"+resposta+"}");
        if(resposta==null) throw new Exception("Ligação cancelada!");
        Document d = XMLDoc.parseString(resposta); 
        validXSD(d);
    }

    /**
     * Envia o pedido de Registo para o Servidor.
     */
    public void registar(String user, String pass, String first, String last, String gender, String birth, String nac, String foto) throws Exception {
        String xml = "<metodo><registar nickname='" + user + "' senha='" + pass + "' "
                   + "firstnames='" + first + "' lastnames='" + last + "' "
                   + "gender='" + gender + "' birthdate='" + birth + "' "
                   + "nacionalidade='" + nac + "'>"
                   + "<photography>" + foto + "</photography>"
                   + "</registar></metodo>";
        os.println(xml);
    }

    /**
     * Envia o pedido de Alteração de Perfil para o Servidor.
     */
    public void alterar(String username, String novaPassword, String novaFotoBase64) throws Exception {
        String xml = "<metodo><alterar nickname='" + username + "' senha='" + novaPassword + "'>"
                   + "<photography>" + novaFotoBase64 + "</photography>"
                   + "</alterar></metodo>";
        os.println(xml);
        registaLog("Cliente{" + xml + "}");
        
        String resposta = is.readLine();
        if (resposta != null) {
            registaLog("Cliente{" + resposta + "}");
            System.out.println("Servidor respondeu: " + resposta);
        }
    }
}