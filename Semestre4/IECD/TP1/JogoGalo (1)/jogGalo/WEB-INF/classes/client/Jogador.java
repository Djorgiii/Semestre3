package client;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;

import org.w3c.dom.Element;

import util.XMLDoc;

/**
 * Classe Jogador representa um jogador que se liga ao servidor
 * e joga o jogo do galo.
 *
 * @author Engº Porfírio Filipe
 */
public class Jogador {
    /**
     * Host por omissão do servidor (endereço IP).
     */
    private final static String DEFAULT_HOST = "localhost";

    /**
     * Porto por omissão do servidor.
     */
    private final static int DEFAULT_PORT = 5025;

    String host = DEFAULT_HOST;
    int port = DEFAULT_PORT;
    
    // Acesso único ao teclado, define um Scanner para ser reutilziado
    private static Scanner leitor = null; 
    
    // Configuração inicial do client
    public Jogador(String Host, int Port, Scanner Sc) {
    	host = Host;
    	port = Port;
    	leitor = Sc;
    }

    public Jogador() {
    }
    
    /**
     * Lê um número curto da entrada do utilizador.
     *
     * @param 	leitor Scanner para ler a entrada do utilizador.
     * @return 	Número curto válido (entre 1 e 9).
     */
    private static short readShort(Scanner leitor) {
        short numero = 0; // Variável para armazenar o número lido.
        // Loop para garantir que um número válido é lido.
        while (true) {
            // Verifica se o próximo token na entrada é um número curto.
            if (leitor.hasNextShort()) {
                // Lê o número curto.
                numero = leitor.nextShort();
                // Verifica se o número está entre 1 e 9.
                if (numero < 0 || numero > 9) {
                    // Mostra uma mensagem de erro.
                    System.out.println("Jogada inválida!");
                } else {
                    // Retorna o número lido.
                    return numero;
                }
            } else {
                // Ignora a linha atual da entrada.
                leitor.nextLine();
            }
        }
    }
    
  // Lê a senha em modo camuflado se estiver disponivel
  private static String leSenha(String prompt, Scanner s) {
      String senha=null;
      if(System.console() != null)
	  senha = new String(System.console().readPassword(prompt, 5000));
      else {
	  System.out.println(prompt);
	  senha = s.nextLine();
      }
      return senha;
  }
                                  
    /**
     * Método principal do programa ClienteTCP.
     *
     * @param args argumentos da linha de comando: host e porta
     */
    public void main(String[] args) {
        // Lê os argumentos da linha de comando (se existirem).
        if (args != null && args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }  
        if(leitor == null) 
        	leitor = new Scanner(System.in);
        
        try (   
            // Tenta criar um socket para se conectar ao servidor.
            Socket socket = new Socket(host, port);
            // Cria um adaptador para comunicar com o servidor.
            Stub Stub = new Stub(socket)) {

            // Mostra informações sobre a ligação.
            System.out.println("Cliente -> Ligação estabelecida: " + socket);
            
            System.out.println("Utilizadores existentes para teste:");
            System.out.println("cartwheel:p1; milkshake:p2; gandalf:p4; opera:p5; smoke:p9; bagel:p10");
            
            // Lê o nome do utilizador.
            System.out.println("<<< ***** Indique o seu nome de utilizador:");
            int t=0;
            String nome = null;
            do {
        	nome = leitor.nextLine();
        	if(t++==3)  // termina o jogador
        	    return;
            }
            while(nome.isBlank());
            //String nome = System.console().readLine(); 
            // Lê a senha do utilizador.
            String senha = leSenha("<<< ***** Indique a sua senha:",leitor);
           
            // Inicia a sessão com o servidor e obtém o símbolo do jogador.
            char simbolo=Stub.iniciar(nome, senha);
            Stub.print();

            //System.out.println("Foi atribuído o simbolo: "+simbolo);
            if(simbolo=='O')
            	System.out.println("À espera que o oponente jogue...");
            
            // Loop do jogo, enquanto não for o fim do jogo estado!="ND"
            for(;;) {
                // Mostra o tabuleiro atual.
            	Element tab=Stub.obter();
                System.out.println(Stub.tabuleiroToTXT(tab));
                String estado=tab.getAttribute("estado");
                if(!estado.equals("ND")) {
                	// Mostra o estado do jogo após a jogada.
                	System.out.println(Stub.estadoToTXT(estado));
                	if(!estado.equals("IV"))
                		break;
                }
                LocalDateTime inicio = LocalDateTime.now();
                // Pede ao jogador para fazer uma jogada.
                System.out.println("Joga " + simbolo + ": ");

                // Lê a jogada do jogador.
                short jogada = readShort(leitor);
                System.out.println(XMLDoc.tempoDif(inicio));
                // Envia jogada para o servidor.
                Stub.jogar(jogada);
            }
        } catch (Exception e) {
            System.err.println("Jogador: " + e.getLocalizedMessage());
            // e.printStackTrace();
        } finally {
            System.out.println("Jogador: terminou o jogo!");
        }
    } // fim do método main
} // fim da classe Jogador