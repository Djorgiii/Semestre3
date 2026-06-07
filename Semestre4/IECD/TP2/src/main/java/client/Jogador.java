package client;

/**
 * Jogador — cliente de consola do jogo Pontos e Caixas.
 *
 * Ponto de entrada do cliente em modo texto (CLI). Apresenta um menu
 * com as opções de jogar, registar nova conta e alterar perfil.
 * Toda a comunicação com o servidor é feita através do Stub via TCP.
 *
 * Argumentos de linha de comandos (opcionais):
 *   args[0] — host do servidor (por omissão: localhost)
 *   args[1] — porto TCP       (por omissão: 25565)
 */

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
    

    /** Lê e valida uma jogada do jogador (4 números separados por espaços). */
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
    
    /** Lê a senha de forma segura: usa System.console() se disponível, Scanner caso contrário. */
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
                break;
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

    /** Inicia uma partida: autentica o jogador, entra no ciclo de jogo e trata os estados até o jogo terminar. */
    private static void jogarPartida(String host, int port, Scanner leitor) {
        try (Socket socket = new Socket(host, port);
             Stub stub = new Stub(socket)) {

            System.out.println("\n<<< ***** Indique o seu nome de utilizador:");
            String nome = "";
            while (nome.isBlank()) { nome = leitor.nextLine(); }
            
            String senha = leSenha("<<< ***** Indique a sua senha:", leitor);
           
            char simbolo = stub.iniciar(nome, senha);
            stub.print();

            if (simbolo == 'O') {
                System.out.println("À espera que o oponente jogue...");
            }
            
            for(;;) {
                Element tab = stub.obter();
                System.out.println(stub.tabuleiroToTXT(tab));
                String estado = tab.getAttribute("estado");
                
                if(!estado.equals("ND")) {
                    System.out.println(stub.estadoToTXT(estado));
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

    /** Recolhe os dados necessários e envia o pedido de registo de nova conta ao servidor. */
    private static void registarConta(String host, int port, Scanner leitor) {
        try (Socket socket = new Socket(host, port);
             Stub stub = new Stub(socket)) {
             
            System.out.println("\n--- REGISTAR NOVA CONTA ---");
            String user = "";
            do {
                System.out.print("Username (3 a 10 letras/numeros, sem acentos): ");
                user = leitor.nextLine().trim();
            } while (!user.matches("[a-zA-Z0-9_-]{3,10}"));
            String pass = "";
            do {
                System.out.print("Password (mínimo 3 caracteres): ");
                pass = leitor.nextLine().trim();
            } while (pass.length() < 3);

            String first = "";
            do {
                System.out.print("Nomes Próprios (só letras): ");
                first = leitor.nextLine().trim();
            } while (!first.matches("[a-zA-ZÀ-ÿ\\s]+"));

            String last = "";
            do {
                System.out.print("Apelidos (só letras): ");
                last = leitor.nextLine().trim();
            } while (!last.matches("[a-zA-ZÀ-ÿ\\s]+"));

            String gender = "";
            do {
                System.out.print("Género (M/F): ");
                gender = leitor.nextLine().trim().toUpperCase();
            } while (!gender.matches("[MF]"));

            String birth = "";
            do {
                System.out.print("Data Nascimento (AAAA-MM-DD): ");
                birth = leitor.nextLine().trim();
            } while (!birth.matches("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])"));

            String nac = "";
            do {
                System.out.print("Nacionalidade (ex: PT - 2 letras): ");
                nac = leitor.nextLine().trim().toUpperCase();
            } while (!nac.matches("[A-Z]{2}"));
            
            System.out.print("Caminho da foto (ENTER para default): ");
            String caminhoFoto = leitor.nextLine().trim();
            if (caminhoFoto.isBlank()) caminhoFoto = "src/main/webapp/default.jpg";
            
            MyImage img = new MyImage(caminhoFoto);
            if (img.isOk()) {
                String fotoBase64 = img.getBase64();
                stub.registar(user, pass, first, last, gender, birth, nac, fotoBase64);
                System.out.println("⏳ Pedido enviado!");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }

    /** Permite ao jogador alterar a senha e/ou fotografia de perfil via TCP. */
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