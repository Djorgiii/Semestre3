package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import org.w3c.dom.Document;
import util.XMLDoc;

/**
 * Servidor TCP principal — ponto de entrada do sistema de jogo.
 *
 * Responsabilidades:
 *   - Aceitar ligações TCP de clientes (modo Consola/GUI e modo Web).
 *   - Despachar cada pedido para o handler correcto (iniciar, registar, alterar).
 *   - Emparelhar jogadores por par de nomes e lançar um ServidorDedicado por jogo.
 *
 * Argumentos de linha de comandos (opcionais):
 *   args[0] — porto TCP (por omissão: 25565)
 *   args[1] — "S" para modo jogo único, qualquer outro valor para multi-jogo
 *   args[2] — timeout do ServerSocket em ms (0 = sem timeout)
 */
public class Servidor {

    /** Porto TCP por omissão. */
    public final static int DEFAULT_PORT = 25565;
    /** Timeout do ServerSocket (0 = sem timeout). */
    private static int timeout = 0;
    /** Modo jogo único: o servidor termina após o primeiro jogo. */
    private static boolean single = false;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // Processar argumentos opcionais
        if (args.length >= 1) port = Integer.parseInt(args[0]);
        if (args.length >= 2) single = args[1].equalsIgnoreCase("S");
        if (args.length >= 3) timeout = Integer.parseInt(args[2]);

        System.out.println(single ? "⚠️ Modo: Jogo Único" : "🔄 Modo: Multi-Jogo");

        // Gestor de emparelhamento de jogadores
        FIFOJogador fIFOJogador = new Servidor().new FIFOJogador();

        // O emparelhamento e o lançamento do ServidorDedicado são feitos dentro de FIFOJogador.add()
        // pelo que não é necessário um thread separado de gestão de fila.

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🌍 Servidor TCP à escuta no porto: " + port);

            while (true) {
                System.out.println("⏳ Aguardando nova ligação...");
                serverSocket.setSoTimeout(timeout);
                Socket newSock = serverSocket.accept();
                System.out.println("✅ Ligação aceite: " + newSock.getInetAddress());

                // Cada ligação é tratada numa Thread própria para não bloquear o accept()
                new Thread(() -> {
                    try {
                        BufferedReader is = new BufferedReader(new InputStreamReader(newSock.getInputStream()));

                        // A primeira linha enviada pelo cliente determina o tipo de pedido
                        String primeiraLinha = is.readLine();

                        if (primeiraLinha != null) {
                            Document docPedido = XMLDoc.parseString(primeiraLinha);

                            if (docPedido.getElementsByTagName("iniciar").getLength() > 0) {
                                // Pedido de início de jogo — extrair o adversário pretendido e enfileirar
                                System.out.println("   ➡️ Pedido de Jogo (Login). A enviar para a fila...");
                                org.w3c.dom.Element elIniciar = (org.w3c.dom.Element)
                                    docPedido.getElementsByTagName("iniciar").item(0);
                                String adversarioHint = elIniciar.getAttribute("adversario");
                                fIFOJogador.add(newSock, docPedido, adversarioHint);

                            } else if (docPedido.getElementsByTagName("alterar").getLength() > 0) {
                                // Pedido de alteração de perfil — tratado pelo Skeleton e ligação fechada
                                System.out.println("   ➡️ Pedido de Alteração de Perfil.");
                                Skeleton.runAlterar(newSock, docPedido);
                                newSock.close();

                            } else if (docPedido.getElementsByTagName("registar").getLength() > 0) {
                                // Pedido de registo de nova conta
                                System.out.println("   ➡️ Pedido de Registo de Conta.");
                                Skeleton.runRegistar(newSock, docPedido);
                                newSock.close();

                            } else {
                                // Pedido desconhecido — fechar ligação
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
     * Gestor de emparelhamento de jogadores por par de nomes.
     *
     * Em vez de uma fila FIFO global (que emparelharia qualquer dois jogadores),
     * utiliza um mapa de SynchronousQueue indexado pela chave do par.
     *
     * A chave é "nomeMin_nomeMax" (ordem alfabética), garantindo simetria:
     * Alice vs Bob e Bob vs Alice produzem a mesma chave.
     *
     * Fluxo:
     *   - 1º a chegar: cria a sala, autentica-se como X e fica à espera (máx. 60s).
     *   - 2º a chegar: encontra a sala, autentica-se como O, retira o socket de X
     *                  e lança o ServidorDedicado.
     *
     * Compatibilidade CLI: clientes sem campo "adversario" usam a chave "__qualquer__",
     * comportando-se como uma FIFO global tradicional.
     */
    private final class FIFOJogador {

        /**
         * Mapa de salas de espera.
         * Chave: "nomeMin_nomeMax" | Valor: fila onde o 1º jogador deposita o seu socket.
         */
        private final java.util.Map<String, java.util.concurrent.SynchronousQueue<Socket>> salas =
            new java.util.concurrent.ConcurrentHashMap<>();

        /**
         * Calcula a chave simétrica para um par de jogadores.
         * A ordenação alfabética garante que a chave é igual independentemente
         * de quem chegou primeiro.
         */
        private String chave(String a, String b) {
            return a.compareTo(b) <= 0 ? a + "_" + b : b + "_" + a;
        }

        /**
         * Regista um jogador na fila de emparelhamento.
         * O processamento corre numa Thread própria para não bloquear o dispatcher.
         *
         * @param element    socket do jogador
         * @param docPedido  documento XML com o pedido <iniciar>
         * @param adversario nome do adversário pretendido (vazio = qualquer)
         */
        public void add(Socket element, Document docPedido, String adversario) {
            new Thread(() -> {
                try {
                    org.w3c.dom.Element el = (org.w3c.dom.Element)
                        docPedido.getElementsByTagName("iniciar").item(0);
                    String meuNome = el.getAttribute("nickname");

                    // Adversário é obrigatório — sem adversário não há jogo
                    if (adversario == null || adversario.isBlank()) {
                        System.out.println("   ⚠️ Adversário não especificado. A fechar ligação.");
                        element.close();
                        return;
                    }
                    final String adv = adversario;

                    String key = chave(meuNome, adv);
                    System.out.println("   ➡️ " + meuNome + " quer jogar com " + adv);

                    java.util.concurrent.SynchronousQueue<Socket> sala;
                    boolean souPrimeiro;

                    // Secção crítica: verificar/criar a sala de forma atómica
                    synchronized (salas) {
                        if (!salas.containsKey(key)) {
                            // Primeiro a chegar — cria a sala e regista-se como dono
                            sala = new java.util.concurrent.SynchronousQueue<>();
                            salas.put(key, sala);
                            souPrimeiro = true;
                        } else {
                            // Segundo a chegar — reutiliza a sala existente e remove-a do mapa
                            sala = salas.remove(key);
                            souPrimeiro = false;
                        }
                    }

                    // Tempo máximo de espera pelo adversário (em segundos)
                    final int TIMEOUT_ESPERA_S = 60;

                    if (souPrimeiro) {
                        // Autenticar como jogador X e depositar o socket na sala
                        Skeleton.runIniciar(element, 'X', docPedido);
                        System.out.println("   ⏳ " + meuNome + " (X) aguarda " + adv + " (max " + TIMEOUT_ESPERA_S + "s)...");

                        // offer() bloqueia até o segundo jogador retirar o socket, com timeout
                        boolean emparelhado = sala.offer(element, TIMEOUT_ESPERA_S, java.util.concurrent.TimeUnit.SECONDS);
                        if (!emparelhado) {
                            // O adversário não apareceu dentro do tempo limite
                            System.out.println("   ⏰ Timeout: " + adv + " não apareceu. A fechar ligação de " + meuNome + ".");
                            salas.remove(key); // limpar o mapa por precaução
                            element.close();
                        }
                    } else {
                        // Autenticar como jogador O e retirar o socket de X da sala
                        Skeleton.runIniciar(element, 'O', docPedido);

                        // poll() com pequeno timeout — se X entretanto desistiu, devolve null
                        Socket skX = sala.poll(5, java.util.concurrent.TimeUnit.SECONDS);
                        if (skX == null) {
                            System.out.println("   ⏰ Timeout: X já não está disponível. A fechar ligação de " + meuNome + ".");
                            element.close();
                        } else {
                            // Par completo — lançar o ServidorDedicado para esta partida
                            Socket skO = element;
                            String nX = adv;      // nome do jogador X (adversário do segundo)
                            String nO = meuNome;  // nome do jogador O (segundo a chegar)
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
    }
}