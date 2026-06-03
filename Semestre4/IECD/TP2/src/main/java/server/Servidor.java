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
                Socket sk1 = null;
                Socket sk2 = null;
                try {
                    sk1 = fIFOJogador.remove();
                    sk2 = fIFOJogador.remove();
                    
                    System.out.println("🤝 Par encontrado! A iniciar Servidor Dedicado...");
                    Thread jogo = new ServidorDedicado(sk1, sk2);
                    jogo.start(); 

                    if (single) { 
                        try { jogo.join(); } catch (InterruptedException e) {}
                        System.out.println("👋 Modo single-game terminado. A sair...");
                        System.exit(0);
                    }	
                } catch (InterruptedException e) {
                    System.out.println("❌ Erro na tarefa de gestão de fila.");
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
        private final BlockingQueue<Socket> queue = new LinkedBlockingQueue<>();
        private char proximoSimbolo = 'X';

        public synchronized void add(Socket element, Document docPedido) throws InterruptedException {
            new Thread(() -> {
                try {
                    char atribuido = proximoSimbolo;
                    
                    Skeleton.runIniciar(element, atribuido, docPedido);
                    
                    queue.put(element);
                    proximoSimbolo = (atribuido == 'X' ? 'O' : 'X');
                    
                } catch (Exception e) {
                    System.out.println("⚠️ Falha na inicialização do jogador: " + e.getMessage());
                    try { element.close(); } catch (IOException e1) {}
                }
            }).start();
        }

        public Socket remove() throws InterruptedException {
            return queue.take();
        }
    }
}