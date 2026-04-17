package peer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * 🌐 <b>Classe P2Peer (Orquestrador da Arquitetura Distribuída)</b>
 * <p>Componente central responsável pela descoberta de nós, gestão de pertença 
 * (membership) e coordenação entre o plano de controlo e o plano de dados.</p>
 * <p>Esta classe implementa:</p>
 * <ul>
 * <li>Filtros de <b>auto-exclusão</b> para evitar o registo do próprio nó.</li>
 * <li>Seleção manual de nós alvos através da consola.</li>
 * <li>Descoberta híbrida (UDP Broadcast + TCP Scan).</li>
 * </ul>
 * @author Engº P. Filipe
 * @version 1.0
 */
public final class P2Peer {
    /** 🔑 Identificador único (UUID curto) do nó nesta instância da arquitetura */
    private final String idNo;
    /** 🔢 Porto onde o servidor de dados local aceita ligações */
    private static int portoServico = 5025;  // pode mudar se estiver ocupado
    /** 📡 Porto de controlo (calculado em tempo de execução como portoServico - 1) para sinais de liveness */
    private static int portoControlo = portoServico-1;
    /** 🕵️‍♂️ Porto onde o servidor UDP de descoberta aceita mensagens */
    private static int portoDescoberta = 5025;
    /** 🛡️ Pode ser usado para exclui portos efémeros/dinâmicos do sistema (1024-49151) */
	private final static int minPort = 5025;
	private final static int maxPort = 5031; 

    
    /** 🗃️ Serviço de Pertença: Mantém a vista local atualizada da arquitetura */
    private final GestorNos gestorNos = new GestorNos();
    
    /** ⚙️ Referência ao processo externo que executa a lógica de servidor */
    private Process processoServidor;
    
    /** 🏳️ Modo silencioso: Flag que para a exeução das tarefas */ 
    private volatile boolean pausa = false;
    /** 🧵 Executor para tarefas temporizadas */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    /** 🏎️ Pool de threads para realizar atividades em paralelo sem bloquear o nó */
    private final ExecutorService scanPool = Executors.newFixedThreadPool(50);

    /**
     * 🏗️ <b>Construtor da Instância P2Peer</b>
     * @param porto O porto base para o serviço de dados.
     */
    public P2Peer(int portoSugerido) {
    	// UUID.randomUUID().toString().substring(0, 8)
        this.idNo = "Alice-" + new Random().nextInt(1000);
        alocarPortos(portoSugerido);
    }

    /**
     * 🔌 <b>Alocação Dinâmica de Portos</b>
     * <p>Verifica a disponibilidade real do par (PortoServico e PortoControlo).</p>
     */
    private void alocarPortos(int sugerido) {
        int p = sugerido;
        while (true) {
            if (portoDisponivel(p) && portoDisponivel(p - 1)) {
                portoServico = p;
                portoControlo = p - 1;
                break;
            }
            p += 2; // Salta para o próximo par disponível para evitar sobreposição
        }
    }

    /** 🔍 <b>Validar Porto</b>: Tenta abrir um ServerSocket temporário. */
    private boolean portoDisponivel(int porto) {
        try (ServerSocket ss = new ServerSocket(porto)) {
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 🚀 <b>Iniciar Nó</b>
     * <p>Ativa os serviços de escuta, agenda as rotinas de manutenção e lança o servidor local.</p>
     */
    public void iniciar() {
        configurarShutdown();

        // 📡 Ativação dos planos de escuta (Threads de fundo)
        new Thread(this::servidorControloUDP).start();
        new Thread(this::servidorControloTCP).start();
        
        // 🗓️ Agendamento de ciclos de vida da arquitetura
        scheduler.scheduleAtFixedRate(this::anunciarPresenca, 0, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::varrimentoArquitetura, 2, 10, TimeUnit.SECONDS);
        // scheduler.scheduleAtFixedRate(gestorNos::limparNosInativos, 10, 15, TimeUnit.SECONDS);

        lancarServidorDados();
    }

    /** 📢 <b>Anunciar Presença</b>: Envia batimento cardíaco (heartbeat) via UDP Broadcast. */
    private void anunciarPresenca() {
    	if(pausa)
    		return;
        try (DatagramSocket udp = new DatagramSocket()) {
            udp.setBroadcast(true);
            String msg = "PRESENCA:" + idNo + ":" + portoServico;
            byte[] buf = msg.getBytes();
            // Porto do ponto de encontro padrão para descoberta inicial na sub-rede
            udp.send(new DatagramPacket(buf, buf.length, InetAddress.getByName("255.255.255.255"), portoDescoberta));
        } catch (Exception e) {}
    }

    /** 👂 Responde a pedidos PRESENCA de outros nós, validando a identidade para auto-exclusão. */
    private void servidorControloUDP() {
        try (DatagramSocket recetor = new DatagramSocket(null)) {
            recetor.setReuseAddress(true);
            recetor.bind(new InetSocketAddress(portoDescoberta));
            byte[] buf = new byte[1024];
            while (true) {
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                recetor.receive(p);
                String msg = new String(p.getData(), 0, p.getLength());
                
                if (msg.startsWith("PRESENCA")) {
                    String[] partes = msg.split(":");
                    String idDetectado = partes[1];
                    
                    // 🛡️ Filtro: Ignora se o ID for igual ao ID local (auto-anúncio)
                    // if (!idDetectado.equals(this.idNo)) 
                        gestorNos.atualizarOuAdicionarNo(idDetectado, p.getAddress().getHostAddress(), Integer.parseInt(partes[2]));
                    
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Monitor UDP ocupado por outra instância local.");
        }
    }

    /** 🔌 Responde a pedidos PING de outros nós, validando a identidade para auto-exclusão. */
    private void servidorControloTCP() {
        try (ServerSocket ss = new ServerSocket(portoControlo)) {
            ss.setReuseAddress(true);
            while (true) {
                Socket s = ss.accept();
                new Thread(() -> {
                    try (s; 
                    	 PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))) {
                        
                        String sinal = in.readLine();
                        if (sinal != null && sinal.startsWith("PING")) {
                            String[] partes = sinal.split(":");
                            String idRemoto = partes[1];

                            // 🛡️ Filtro: Ignora se o ID for igual ao ID local (auto-anúncio)
                            /* if (!idRemoto.equals(this.idNo))*/ {
                                gestorNos.atualizarOuAdicionarNo(idRemoto, s.getInetAddress().getHostAddress(), Integer.parseInt(partes[2]));
                                out.println("PONG:" + idNo + ":" + portoServico);
                            }
                        }
                    } catch (IOException e) {}
                }).start();
            }
        } catch (IOException e) {}
    }

    /**
     * 🔍 <b>Varrimento da Arquitetura (Discovery)</b>
     * <p>Explora a rede local em busca de outros nós, garantindo o fecho de todos os recursos
     * e a utilização de UTF-8 para evitar corrupção de caracteres (como no ID do nó).</p>
     */
    private void varrimentoArquitetura() {
    	if(pausa)
    		return;
        try {
            String ipLocal = InetAddress.getLocalHost().getHostAddress();
            String prefixo = ipLocal.substring(0, ipLocal.lastIndexOf(".") + 1);
            
            for (int i = 1; i <= 254; i++) {
                final String alvoIP = prefixo + i;
                for (int p = minPort-1; p <= maxPort+1; p++) {
                    if (p%2==1) // só varre portos pares (portos de controlo)
                    	continue;
                    final int alvoPorto = p;
                    scanPool.execute(() -> {
                        // 🛡️ Try-with-resources aninhado: Garante o fecho do Reader, Writer e Socket
                        try (Socket s = new Socket()) {
                            s.connect(new InetSocketAddress(alvoIP, alvoPorto), 150);
                            
                            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))) {
                                
                                // 🆔 Envio do PING com ID local
                                out.println("PING:" + idNo + ":" + portoServico);
                                
                                String res = in.readLine();
                                if (res != null && res.startsWith("PONG")) {
                                    String[] partes = res.split(":");
                                    // 🛡️ Filtro de Identidade: Ignora o próprio nó
                                    if (!partes[1].equals(this.idNo)) {
                                        gestorNos.atualizarOuAdicionarNo(partes[1], alvoIP, Integer.parseInt(partes[2]));
                                    }
                                }
                            }
                        } catch (IOException e) {
                            // Silencioso: O IP/Porto não tem um nó ativo
                        }
                    });
                }
            }
        } catch (Exception e) {}
    }

    /**
     * ⚙️ <b>Lançar Servidor de Dados</b>
     * <p>Instancia o processo do servidor de dados ({@code peer.Servidor}) forçando o uso de 
     * <b>UTF-8</b> para garantir consistência nos logs em qualquer Sistema Operativo.</p>
     * * <p><b>Notas Técnicas:</b></p>
     * <ul>
     * <li>Usa o parâmetro {@code -Dfile.encoding=UTF-8} para uniformizar a escrita de ficheiros.</li>
     * <li>O {@code URLDecoder} descodifica o caminho do <i>ClassPath</i> para evitar erros com espaços ou acentos.</li>
     * </ul>
     */
    private void lancarServidorDados() {
        try {
            // 🛠️ Localiza o executável java
            String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            
            // 📂 Resolve o ClassPath tratando caracteres especiais no diretório
            String cp = URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
            
            // 🏗️ Comando: java -Dfile.encoding=UTF-8 -cp [caminho] peer.Servidor [porto]
            ProcessBuilder pb = new ProcessBuilder(
                    java, 
                    "-Dfile.encoding=UTF-8", 
                    "-Dsun.jnu.encoding=UTF-8", // 💡 Força UTF-8 também nos nomes de ficheiros internos
                    "-cp", cp, 
                    "peer.Server", 
                    String.valueOf(portoServico)
                );
            
            // 📝 Redireciona Standard Output e Error para o log (agora garantidamente em UTF-8)
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File("log_" + idNo + ".txt")));
            pb.redirectErrorStream(true); 
            
            this.processoServidor = pb.start();
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao lançar o servidor de dados: " + e.getMessage());
        }
    }

    /** 🛑 <b>Encerramento Limpo</b>: Garante que todos os recursos e processos são finalizados. */
    private void configurarShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Desativando recursos do nó...");
            scheduler.shutdownNow();
            scanPool.shutdownNow();
            if (processoServidor != null) processoServidor.destroyForcibly();
        }));
    }

    /** 🏁 <b>Main</b>: Ciclo de interação humano-arquitetura com seleção manual de nós. */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
		// System.out.print("⌨️ Introduza o porto TCP ("+minPort+"-"+maxPort+") [Omissão "+portoServico+"]: ");
		// String input = sc.nextLine().trim();
        String input = "5025";
		// 🔢 Validação da entrada
		if (!input.isEmpty()) {
			try {
				int p = Integer.parseInt(input);
				if (p >= minPort && p <= maxPort) 
					portoServico = p;
				 else 
					System.out.println("⚠️ Fora da gama. A usar " + portoServico);
			} catch (NumberFormatException e) {
				System.out.println("⚠️ Inválido. A usar " + portoServico);
			}
		}

        P2Peer no = new P2Peer(portoServico);
        no.iniciar();

        for (;;) {
            // Verificação de Pertença: O nó tem vizinhos detetados?
            if (no.gestorNos.estaIsolado()) {
                System.out.println("\n✅ Nó atual na arquitetura [" + no.idNo + "]");
                System.out.println("	🔌 Porto de Dados: " + portoServico);
                System.out.println("	⚙️ Porto de Controlo: " + portoControlo);
                System.out.println("\n🔍 Aguardando descoberta de nós na arquitetura... (<enter> ou escreva 'sair')");
                String opt = sc.nextLine();
                if (opt.equalsIgnoreCase("sair")) 
                	break;
            } else {
	            // Exibição da Vista Local da Arquitetura
	            System.out.println(no.gestorNos.listar());
	            // Interação do Utilizador
	            System.out.print("👉 Escreva o 🆔 ID do nó alvo, <enter> ou 'sair': ");
	            String selecao = sc.nextLine().trim();
	
	            if (selecao.isEmpty()) 
	            	continue;
	            if (selecao.equalsIgnoreCase("sair")) 
	            	break;
	
	            // Tentativa de ligação ao recurso selecionado
	            NoRemoto alvo = no.gestorNos.getNoPorId(selecao);
	            if (alvo!=null) {
	            	// no.pausa=true;  // ativa modo silencioso
	        		System.out.println("🚀 Vai lançar o cliente...");
	        		try {
						new Cliente(alvo.getIp(), alvo.getPorto(),sc).main(null);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        		// no.pausa=false;
	            } else 
	                System.out.println("⚠️ Nó não encontrado. Certifique-se de que escreveu o ID corretamente.");
            }
        }
        System.exit(0);
    }
}