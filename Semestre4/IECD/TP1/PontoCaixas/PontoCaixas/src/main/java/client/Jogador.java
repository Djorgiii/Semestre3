package client;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;
import org.w3c.dom.Element;
import util.MyImage;
import util.XMLDoc;

public class Jogador {
    private final static String DEFAULT_HOST = "localhost";
    private final static int DEFAULT_PORT = 25565;
    
    public Jogador() {}
    
    /**
     * Lê a jogada do utilizador e obriga a que o formato seja estritamente 4 números.
     */
    private static String readJogada(Scanner leitor) {
        while (true) {
            String entrada = leitor.nextLine().trim();
            if (entrada.matches("\\d+\\s+\\d+\\s+\\d+\\s+\\d+")) {
                return entrada;
            } else {
                System.out.println("❌ Formato incorreto! Deves escrever exatamente 4 números separados por espaços.");
                System.out.print("👉 Tenta novamente (ex: 1 1 2 1): ");
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
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        
        if (args != null && args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }  
        
        Scanner leitor = new Scanner(System.in);
        
        // --- CICLO DO MENU PRINCIPAL ---
        while(true) {
            System.out.println("\n" + "=".repeat(30));
            System.out.println("🎮 PONTOS E CAIXAS - MENU 🎮");
            System.out.println("=".repeat(30));
            System.out.println("1 - Jogar (Fazer Login)");
            System.out.println("2 - Registar Nova Conta");
            System.out.println("3 - Alterar Perfil");
            System.out.println("0 - Sair");
            System.out.print("👉 Escolhe uma opção: ");
            
            String op = leitor.nextLine().trim();
            
            if (op.equals("0")) {
                System.out.println("👋 Até à próxima!");
                break; // Sai do while e o programa termina
            } else if (op.equals("1")) {
                jogarPartida(host, port, leitor);
            } else if (op.equals("2")) {
                registarConta(host, port, leitor);
            } else if (op.equals("3")) {
                alterarConta(host, port, leitor);
            } else {
                System.out.println("❌ Opção inválida. Escolhe 1, 2, 3 ou 0.");
            }
        }
        leitor.close();
    }

    // ==========================================================
    // OPÇÃO 1: JOGAR (LOGIN E CICLO DO JOGO)
    // ==========================================================
    private static void jogarPartida(String host, int port, Scanner leitor) {
        try (Socket socket = new Socket(host, port);
             Stub stub = new Stub(socket)) {

            System.out.println("\n<<< ***** Indique o seu nome de utilizador:");
            String nome = "";
            while (nome.isBlank()) { nome = leitor.nextLine(); }
            
            String senha = leSenha("<<< ***** Indique a sua senha:", leitor);
           
            char simbolo = stub.iniciar(nome, senha);
            stub.print(); // Descarrega a foto

            if (simbolo == 'O') {
                System.out.println("À espera que o oponente jogue...");
            }
            
            for(;;) {
                Element tab = stub.obter();
                System.out.println(stub.tabuleiroToTXT(tab));
                String estado = tab.getAttribute("estado");
                
                if(!estado.equals("ND")) {
                    System.out.println(stub.estadoToTXT(estado));
                    // Se não for Inválido nem Bónus, o jogo terminou
                    if(!estado.equals("IV") && !estado.equals("BO")) break;
                }
                
                LocalDateTime inicio = LocalDateTime.now();
                
                System.out.println("\n💡 COMO JOGAR (Coordenadas):");
                System.out.println("Escreve 4 números separados por espaço: [Coluna A] [Linha A] [Coluna B] [Linha B]");
                System.out.println("  > Traço Horizontal: 1 1 2 1");
                System.out.println("  > Traço Vertical:   1 1 1 2");
                System.out.print("👉 É a tua vez! Joga " + simbolo + ": ");

                String jogada = readJogada(leitor);
                System.out.println("Tempo de resposta: " + XMLDoc.tempoDif(inicio));
                
                stub.jogar(jogada);
            }
        } catch (Exception e) {
            System.err.println("❌ Erro no Jogo: " + e.getMessage());
        }
    }

    // ==========================================================
    // OPÇÃO 2: REGISTAR NOVA CONTA
    // ==========================================================
    private static void registarConta(String host, int port, Scanner leitor) {
        try (Socket socket = new Socket(host, port);
             Stub stub = new Stub(socket)) {
             
            System.out.println("\n--- REGISTAR NOVA CONTA ---");
            System.out.print("Username: ");
            String user = leitor.nextLine().trim();
            System.out.print("Password: ");
            String pass = leitor.nextLine().trim();
            System.out.print("Nomes Próprios: ");
            String first = leitor.nextLine().trim();
            System.out.print("Apelidos: ");
            String last = leitor.nextLine().trim();
            System.out.print("Género (M/F): ");
            String gender = leitor.nextLine().trim();
            System.out.print("Data Nascimento (AAAA-MM-DD): ");
            String birth = leitor.nextLine().trim();
            System.out.print("Nacionalidade (ex: PT): ");
            String nac = leitor.nextLine().trim();
            
            System.out.print("Caminho da foto (ENTER para default): ");
            String caminhoFoto = leitor.nextLine().trim();
            if (caminhoFoto.isBlank()) caminhoFoto = "default.jpg";
            
            MyImage img = new MyImage(caminhoFoto);
            if (img.isOk()) {
                String fotoBase64 = img.getBase64();
                // Enviar todos os parâmetros para o stub
                stub.registar(user, pass, first, last, gender, birth, nac, fotoBase64);
                System.out.println("⏳ Pedido enviado!");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }

    // ==========================================================
    // OPÇÃO 3: ALTERAR DADOS DO PERFIL
    // ==========================================================
    private static void alterarConta(String host, int port, Scanner leitor) {
        try (Socket socket = new Socket(host, port);
             Stub stub = new Stub(socket)) {
             
            System.out.println("\n--- ALTERAR DADOS ---");
            System.out.print("Qual é o teu Username atual? ");
            String user = leitor.nextLine().trim();
            System.out.print("Nova Password (deixa em branco para não mudar): ");
            String novaPass = leitor.nextLine().trim();
            System.out.print("Caminho da Nova Foto (ex: foto.jpg) ou deixa em branco: ");
            String caminhoFoto = leitor.nextLine().trim();
            
            String novaFotoBase64 = "";
            if (!caminhoFoto.isBlank()) {
                MyImage img = new MyImage(caminhoFoto);
                if (img.isOk()) {
                    novaFotoBase64 = img.getBase64();
                    System.out.println("📸 Fotografia lida com sucesso.");
                } else {
                    System.out.println("⚠️ Não foi possível ler a imagem. A foto não será alterada.");
                }
            }
            
            stub.alterar(user, novaPass, novaFotoBase64);

        } catch (Exception e) {
            System.err.println("❌ Erro ao alterar: " + e.getMessage());
        }
    }
}