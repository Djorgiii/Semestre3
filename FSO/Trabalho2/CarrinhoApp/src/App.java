public class App {
    private GUI gui;
    private GuiGravador guiGravador;

    public App() {
    	BaseDados bd = new BaseDados();
        gui = new GUI(bd);
        guiGravador = new GuiGravador(bd);
        guiGravador.setBufferCentral(gui.getBufferCircular());
        gui.setGuiGravador(guiGravador);
    }

    public void run() {
        System.out.println("A aplicação começou.");
        while (!gui.getBd().isTerminar()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("A aplicação terminou.");
    }

    public static void main(String[] args) {
        App app = new App();

        Servidor servidor = new Servidor(app.gui.getBufferCircular(), app.gui.getBd().getRobot(), app.gui.getBd(),s -> app.gui.myPrintServidor(s));
        app.gui.setServidor(servidor);
        servidor.setGravador(app.guiGravador.getGravador());
        servidor.start();

        // Tarefa dos aleatórios (lote de 5). Já não há "próxima" (manuais).
        MovimentosAleatorios tAleatorios = new MovimentosAleatorios(app.gui);
        tAleatorios.start();

        // Dizer à GUI quem é a única tarefa (aleatórios)
        app.gui.setTarefas(tAleatorios);

        EvitarObstaculo tObstaculo = new EvitarObstaculo(app.gui, app.guiGravador);
        tObstaculo.start();
        app.gui.setTarefaObstaculo(tObstaculo);
        // Entrar no ciclo normal
        app.run();
    }
}
