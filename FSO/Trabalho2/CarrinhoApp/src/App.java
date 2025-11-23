public class App {
    private GUI gui;
    private GuiGravador guigravador;

    public App() {
        gui = new GUI();
        guigravador = new GuiGravador();
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

        Servidor servidor = new Servidor(app.gui.getBufferCircular(), app.gui.getBd().getRobot(), app.gui.getBd(),s -> app.gui.myPrint(s));
        app.gui.setServidor(servidor);
        servidor.start();

        // Tarefa dos aleatórios (lote de 5). Já não há "próxima" (manuais).
        MovimentosAleatorios tAleatorios = new MovimentosAleatorios(app.gui, null);
        tAleatorios.start();

        // Dizer à GUI quem é a única tarefa (aleatórios)
        app.gui.setTarefas(tAleatorios);

        EvitarObstaculo tObstaculo = new EvitarObstaculo(tAleatorios, app.gui);
        tObstaculo.start();
        app.gui.setTarefaObstaculo(tObstaculo);
        // Entrar no ciclo normal
        app.run();
    }
}
