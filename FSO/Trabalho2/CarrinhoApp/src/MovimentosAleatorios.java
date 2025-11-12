public class MovimentosAleatorios extends Tarefa {
    private GUI gui;

    public MovimentosAleatorios(GUI gui, Tarefa proxima) {
        super(proxima);
        this.gui = gui;
    }

    public void execucao() {
        // Garantias mínimas
        if (gui == null || gui.getBd() == null || gui.getBd().getServidor() == null) {
            bloquear();
            return;
        }

        String[] tipos = {"PARAR", "RETA", "CURVARDIREITA", "CURVARESQUERDA"};

        // Enquanto o robot estiver aberto e a flag "aleatórios" ligada,
        // produz lotes de 5 comandos de cada vez.
        while (gui.getBd().isRobotAberto() && gui.getBd().isAleatoriosOn()) {
            // (opcional) Reinicia contador de lote no servidor
            //gui.getBd().getServidor().resetContadorAleatorios();

            java.util.concurrent.Semaphore mux = gui.getBd().getProdutorMux();
            mux.acquireUninterruptibly();
            try {
                for (int i = 0; i < 5; i++) {
                    if (!gui.getBd().isRobotAberto() || !gui.getBd().isAleatoriosOn()) break;

                    String tipo = tipos[(int)(Math.random() * tipos.length)];
                    Movimento comando;
                    if (tipo.equals("PARAR")) {
                        comando = new Movimento(tipo, false);
                    } else if (tipo.equals("RETA")) {
                        int distancia = 10 + (int)(Math.random() * 41); // 10..50
                        comando = new Movimento(tipo, distancia, 0);
                    } else { // curvas
                        int raio = 10 + (int)(Math.random() * 21);      // 10..30
                        int angulo = 20 + (int)(Math.random() * 71);    // 20..90
                        comando = new Movimento(tipo, angulo, raio);
                    }

                    gui.getBufferCircular().inserirElemento(comando);
                    dormir(); // pequeno intervalo entre comandos
                }
            } finally {
                mux.release();
            }
         // ENTRE LOTES: inserir manuais pendentes, se houver
            Movimento pend;
            if ((pend = gui.obterComandoManual()) != null) {
                java.util.concurrent.Semaphore mux2 = gui.getBd().getProdutorMux();
                mux2.acquireUninterruptibly();
                try {
                    do {
                        gui.getBufferCircular().inserirElemento(pend);
                        if (gui != null) gui.myPrint("[GUI] Comando manual inserido entre lotes: " + pend.getTipo());
                        pend = gui.obterMovimentoManual();   // tenta apanhar mais um pendente
                    } while (pend != null);
                } finally {
                    mux2.release();
                }
            }

            dormir();
        }

        // Quando desligares aleatórios ou fechares o robot, a tarefa bloqueia.
        bloquear();
    }

}