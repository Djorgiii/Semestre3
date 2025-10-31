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
            gui.inserirComandoNoBuffer(c);
        }

        // Quando termina, deixa os aleatórios correrem
        if (proxima != null) {
            proxima.desbloquear();
        }

        // Volta a dormir até haver outro clique
        bloquear();
    }
}
