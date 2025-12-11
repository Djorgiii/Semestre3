public class MovimentosAleatorios extends Tarefa {

    private GUI gui;
    private BaseDados bd;
    public MovimentosAleatorios(GUI gui) {
    	super();
        this.gui = gui;
        this.bd = gui.getBd();
    }

    public void execucao() {

        if (gui == null || bd == null || bd.getServidor() == null) {
            bloquear();
            return;
        }

        String[] tipos = {"PARAR", "RETA", "CURVARDIREITA", "CURVARESQUERDA"};

        while (true) {

            // 1️⃣ SE ROBOT ESTIVER FECHADO → ESPERA
            if (!bd.isRobotAberto()) {
                dormir();
                continue;
            }

            // 2️⃣ SE ALEATÓRIOS ESTIVER OFF → ESPERA
            if (!bd.isAleatoriosOn()) {
                dormir();
                continue;
            }

            // 3️⃣ COMEÇA UM NOVO LOTE (apenas se aleatórios estava ON no início)
            java.util.concurrent.Semaphore mux = bd.getProdutorMux();

            try {
                mux.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            int spinnerValue = bd.getSpinnerValue();

            try {
                // ⚠️ NÃO verificar o botão DURANTE o lote
                for (int i = 0; i < spinnerValue; i++) {

                    // robot pode fechar a meio → aí sim parar o lote
                    if (!bd.isRobotAberto())
                        break;

                    // Criar comando aleatório
                    String tipo = tipos[(int)(Math.random() * tipos.length)];
                    Movimento comando;

                    if (tipo.equals("PARAR")) {
                        comando = new Movimento("PARAR", false);
                    } else if (tipo.equals("RETA")) {
                        int distancia = 10 + (int)(Math.random() * 41);
                        comando = new Movimento("RETA", distancia, 0);
                    } else {
                        int angulo = 20 + (int)(Math.random() * 71);
                        int raio = 10 + (int)(Math.random() * 21);
                        comando = new Movimento(tipo, raio, angulo);
                    }

                    gui.myPrint("[Aleatório] " + comando.getTipo()
                    + " (" + comando.getArg1() + ", " + comando.getArg2() + ")");
                    
		            gui.getBufferCircular().inserirElemento(comando);
		            dormir();
                }

                // 4️⃣ INSERIR PENDENTES
                Movimento pend;
                while ((pend = gui.obterMovimentoManual()) != null) {
                    gui.getBufferCircular().inserirElemento(pend);
                    gui.myPrint("[GUI] Comando manual inserido entre lotes: " + pend.getTipo());
                }

            } finally {
                mux.release();
            }

            dormir();
        }
    }

}
