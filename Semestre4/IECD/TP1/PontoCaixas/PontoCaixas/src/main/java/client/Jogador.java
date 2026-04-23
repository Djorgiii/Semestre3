package client;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;
import org.w3c.dom.Element;
import util.XMLDoc;

public class Jogador {
    private final static String DEFAULT_HOST = "localhost";
    private final static int DEFAULT_PORT = 5025;
    
    // Construtor vazio
    public Jogador() {}
    
    /**
     * Lê a jogada do utilizador (coordenadas x1 y1 x2 y2).
     */
    private static String readJogada(Scanner leitor) {
        String entrada = "";
        while (true) {
            entrada = leitor.nextLine().trim();
            if (!entrada.isEmpty()) {
                return entrada;
            }
        }
    }
    
    private static String leSenha(String prompt, Scanner s) {
        String senha = null;
        if(System.console() != null)
            senha = new String(System.console().readPassword(prompt, 5000));
        else {
            System.out.println(prompt);
            senha = s.nextLine();
        }
        return senha;
    }
                                  
    public static void main(String[] args) {
        // Variáveis locais (não precisam de static)
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        
        if (args != null && args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }  
        
        Scanner leitor = new Scanner(System.in);
        
        try (Socket socket = new Socket(host, port);
             Stub Stub = new Stub(socket)) {

            System.out.println("Cliente -> Ligação estabelecida: " + socket);
            
            System.out.println("<<< ***** Indique o seu nome de utilizador:");
            int t = 0;
            String nome = null;
            do {
                nome = leitor.nextLine();
                if (t++ == 3) return;
            } while (nome.isBlank());
            
            String senha = leSenha("<<< ***** Indique a sua senha:", leitor);
           
            char simbolo = Stub.iniciar(nome, senha);
            Stub.print();

            // Mensagem correta para o 2º Jogador
            if (simbolo == 'O') {
                System.out.println("À espera que o oponente jogue...");
            }
            
            for(;;) {
                Element tab = Stub.obter();
                System.out.println(Stub.tabuleiroToTXT(tab));
                String estado = tab.getAttribute("estado");
                
                if(!estado.equals("ND")) {
                    System.out.println(Stub.estadoToTXT(estado));
                    // Se o estado não for "Inválido" nem "Bónus", o jogo acabou
                    if(!estado.equals("IV") && !estado.equals("BO"))
                        break;
                }
                
                LocalDateTime inicio = LocalDateTime.now();
                
                // --- INÍCIO DO BLOCO DE INSTRUÇÕES INTUITIVO ---
                System.out.println("\n💡 COMO JOGAR (Coordenadas):");
                System.out.println("Escreve 4 números separados por espaço: [Coluna A] [Linha A] [Coluna B] [Linha B]");
                System.out.println("  > Traço Horizontal: 1 1 2 1");
                System.out.println("  > Traço Vertical:   1 1 1 2");
                System.out.print("👉 É a tua vez! Joga " + simbolo + ": ");
                // --- FIM DO BLOCO DE INSTRUÇÕES ---

                String jogada = readJogada(leitor);
                System.out.println("Tempo de resposta: " + XMLDoc.tempoDif(inicio));
                
                Stub.jogar(jogada);
            }
        } catch (Exception e) {
            System.err.println("Jogador: " + e.getLocalizedMessage());
        } finally {
            System.out.println("Jogador: terminou o jogo!");
        }
    }
}