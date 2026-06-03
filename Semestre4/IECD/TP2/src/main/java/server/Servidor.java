package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.w3c.dom.Document;
import util.XMLDoc;

public class Servidor {

    public final static int DEFAULT_PORT = 25565;
    private static int timeout = 0; 
    private static boolean single = false;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length >= 1) port = Integer.parseInt(args[0]);
        if (args.length >= 2) single = args[1].equalsIgnoreCase("S");
        if (args.length >= 3) timeout = Integer.parseInt(args[2]);

        System.out.println(single ? "⚠️ Modo: Jogo Único" : "🔄 Modo: Multi-Jogo");
        
        FIFOJogador fIFOJogador = new Servidor().new FIFOJogador();

        new Thread(() -> { 
            for(;;) { 
                String[] entrada1 = null;
                String[] entrada2 = null;
                try {
                    entrada1 = fIFOJogador.remove();
                    entrada2 = fIFOJogador.remove();
                    Socket sk1   = fIFOJogador.getSocket(entrada1);
                    Socket sk2   = fIFOJogador.getSocket(entrada2);
                    String nome1 = fIFOJogador.getNome(entrada1);
                    String nome2 = fIFOJogador.getNome(entrada2);

                    System.out.println("🤝 Par encontrado! A iniciar Servidor Dedicado...");
                    Thread jogo = new ServidorDedicado(sk1, sk2, nome1, nome2);
                    jogo.start(); 

                    if (single) { 
                        try { jogo.join(); } catch (InterruptedException e) {}
                        System.out.println("👋 Modo single-game terminado. A sair...");
                        System.exit(0);
                    }	
                } catch (Exception e) {
                    System.out.println("❌ Erro na tarefa de gestão de fila: " + e.getMessage());
                }
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🌍 Servidor TCP à escuta no porto: " + port);
            
            while (true) {
                System.out.println("⏳ Aguardando nova ligação...");
                serverSocket.setSoTimeout(timeout); 
                Socket newSock = serverSocket.accept();
                System.out.println("✅ Ligação aceite: " + newSock.getInetAddress());

                new Thread(() -> {
                    try {
                        BufferedReader is = new BufferedReader(new InputStreamReader(newSock.getInputStream()));
                        String primeiraLinha = is.readLine();
                        
                        if (primeiraLinha != null) {
                            Document docPedido = XMLDoc.parseString(primeiraLinha);
                            
                            if (docPedido.getElementsByTagName("iniciar").getLength() > 0) {
                                System.out.println("   ➡️ Pedido de Jogo (Login). A enviar para a fila...");
                                fIFOJogador.add(newSock, docPedido);
                                
                            } else if (docPedido.getElementsByTagName("alterar").getLength() > 0) {
                                System.out.println("   ➡️ Pedido de Alteração de Perfil.");
                                Skeleton.runAlterar(newSock, docPedido);
                                newSock.close();
                                
                            } else if (docPedido.getElementsByTagName("registar").getLength() > 0) {
                                System.out.println("   ➡️ Pedido de Registo de Conta.");
                                Skeleton.runRegistar(newSock, docPedido);
                                newSock.close();
                                
                            } else {
                                System.out.println("   ⚠️ Pedido desconhecido. A fechar ligação.");
                                newSock.close();
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Erro no Dispatcher: " + e.getMessage());
                    }
                }).start();
            }
        } catch (IOException e) {
            System.err.println("🚨 Erro crítico no Servidor: " + e.getLocalizedMessage());
        }
    }

    private final class FIFOJogador {
        // Cada entrada é um array: [0]=socket, [1]=nome do jogador
        private final BlockingQueue<String[]> queue = new LinkedBlockingQueue<>();
        private char proximoSimbolo = 'X';

        public synchronized void add(Socket element, Document docPedido) throws InterruptedException {
            new Thread(() -> {
                try {
                    char atribuido = proximoSimbolo;

                    // Autenticar e obter o nome do jogador
                    String nomeJogador = getMethod(docPedido, "iniciar").getAttribute("nickname");
                    Skeleton.runIniciar(element, atribuido, docPedido);

                    // Guardar socket e nome juntos na fila
                    String[] entrada = new String[]{ element.toString(), nomeJogador };
                    // Guardar o socket num mapa indexado pelo toString
                    socketMap.put(element.toString(), element);
                    queue.put(entrada);
                    proximoSimbolo = (atribuido == 'X' ? 'O' : 'X');

                } catch (Exception e) {
                    System.out.println("⚠️ Falha na inicialização do jogador: " + e.getMessage());
                    try { element.close(); } catch (IOException e1) {}
                }
            }).start();
        }

        public String[] remove() throws InterruptedException {
            return queue.take();
        }

        public Socket getSocket(String[] entrada) {
            return socketMap.remove(entrada[0]);
        }

        public String getNome(String[] entrada) {
            return entrada[1];
        }

        // Mapa temporário para recuperar o Socket a partir do toString
        private final java.util.concurrent.ConcurrentHashMap<String, Socket> socketMap =
            new java.util.concurrent.ConcurrentHashMap<>();

        private org.w3c.dom.Element getMethod(Document doc, String method) throws Exception {
            org.w3c.dom.NodeList items = doc.getElementsByTagName(method);
            if (items.getLength() != 1) throw new Exception("Método não encontrado: " + method);
            return (org.w3c.dom.Element) items.item(0);
        }
    }
}