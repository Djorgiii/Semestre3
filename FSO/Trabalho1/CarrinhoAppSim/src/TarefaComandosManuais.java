public class TarefaComandosManuais extends Tarefa {

    private final GUI gui;

    public TarefaComandosManuais(GUI gui, Tarefa proxima) {
        super(proxima);
        this.gui = gui;
    }

    @Override
    public void execucao() {
        // Ler o comando pedido pela GUI
        Comando c = gui.obterComandoManual();

        if (c != null) {
            java.util.concurrent.Semaphore mux = gui.getBd().getProdutorMux();
            mux.acquireUninterruptibly();
            try {
                gui.inserirComandoNoBuffer(c);
            } finally {
                mux.release();
            }
            if (proxima != null) proxima.desbloquear();
        }

        // Volta a dormir até haver outro clique
        bloquear();
    }
}