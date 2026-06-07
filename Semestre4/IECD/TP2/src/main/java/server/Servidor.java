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

        // O emparelhamento e lançamento do ServidorDedicado é feito dentro de FIFOJogador.add()
        // O modo single não é suportado com emparelhamento por nome (ignorado).

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
                                // Ler adversario directamente do XML (atributo opcional)
                                org.w3c.dom.Element elIniciar = (org.w3c.dom.Element)
                                    docPedido.getElementsByTagName("iniciar").item(0);
                                String adversarioHint = elIniciar.getAttribute("adversario");
                                fIFOJogador.add(newSock, docPedido, adversarioHint);
                                
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

    /**
     * Emparelha jogadores pelo par (nomeA, nomeB).
     * O primeiro a chegar fica à espera numa SynchronousQueue dedicada ao par.
     * O segundo a chegar entrega o seu socket a essa queue e lança o jogo.
     */
    private final class FIFOJogador {

        // Chave: "nomeMin_nomeMax" — simétrico (A vs B == B vs A)
        // Valor: queue onde o primeiro jogador deposita o seu socket após autenticação
        private final java.util.Map<String, java.util.concurrent.SynchronousQueue<Socket>> salas =
            new java.util.concurrent.ConcurrentHashMap<>();

        private String chave(String a, String b) {
            return a.compareTo(b) <= 0 ? a + "_" + b : b + "_" + a;
        }

        public void add(Socket element, Document docPedido, String adversario) {
            new Thread(() -> {
                try {
                    org.w3c.dom.Element el = (org.w3c.dom.Element)
                        docPedido.getElementsByTagName("iniciar").item(0);
                    String meuNome = el.getAttribute("nickname");

                    // Compatibilidade CLI: sem adversario, usa FIFO global
                    final String adv = (adversario == null || adversario.isBlank())
                        ? "__qualquer__" : adversario;

                    String key = chave(meuNome, adv);
                    System.out.println("   ➡️ " + meuNome + " quer jogar com " + adv);

                    java.util.concurrent.SynchronousQueue<Socket> sala;
                    boolean souPrimeiro;

                    synchronized (salas) {
                        if (!salas.containsKey(key)) {
                            // Primeiro a chegar — cria a sala e fica à espera
                            sala = new java.util.concurrent.SynchronousQueue<>();
                            salas.put(key, sala);
                            souPrimeiro = true;
                        } else {
                            // Segundo a chegar — vai usar a sala existente
                            sala = salas.remove(key);
                            souPrimeiro = false;
                        }
                    }

                    // Timeout de espera pelo adversário (60 segundos)
                    final int TIMEOUT_ESPERA_S = 60;

                    if (souPrimeiro) {
                        // Autenticar como X e depositar o socket na sala com timeout
                        Skeleton.runIniciar(element, 'X', docPedido);
                        System.out.println("   ⏳ " + meuNome + " (X) aguarda " + adv + " (max " + TIMEOUT_ESPERA_S + "s)...");
                        boolean emparelhado = sala.offer(element, TIMEOUT_ESPERA_S, java.util.concurrent.TimeUnit.SECONDS);
                        if (!emparelhado) {
                            // O adversário não apareceu — limpar a sala e fechar ligação
                            System.out.println("   ⏰ Timeout: " + adv + " não apareceu. A fechar ligação de " + meuNome + ".");
                            salas.remove(key); // garantir limpeza mesmo que já tenha sido removido
                            element.close();
                        }
                    } else {
                        // Autenticar como O, retirar o socket de X e lançar o jogo
                        Skeleton.runIniciar(element, 'O', docPedido);
                        // poll com timeout — se X entretanto desistiu, skX pode ser null
                        Socket skX = sala.poll(5, java.util.concurrent.TimeUnit.SECONDS);
                        if (skX == null) {
                            System.out.println("   ⏰ Timeout: X já não está disponível. A fechar ligação de " + meuNome + ".");
                            element.close();
                        } else {
                            Socket skO = element;
                            String nX = adv.equals("__qualquer__") ? "X" : adv;
                            String nO = adv.equals("__qualquer__") ? "O" : meuNome;
                            System.out.println("🤝 Par encontrado! A iniciar Servidor Dedicado...");
                            new ServidorDedicado(skX, skO, nX, nO).start();
                        }
                    }

                } catch (Exception e) {
                    System.out.println("⚠️ Falha na inicialização do jogador: " + e.getMessage());
                    try { element.close(); } catch (IOException e1) {}
                }
            }).start();
        }

        public Socket remove() throws InterruptedException { return null; }
    }
}