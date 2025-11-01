public class App {
    private GUI gui;

    public App() {
        gui = new GUI();
    }

    public void run() {
        System.out.println("A aplicação começou.");
        while(!gui.getBd().isTerminar()) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        System.out.println("A aplicação terminou.");
    }

    public static void main(String[] args) {
        App app = new App();
        RobotLegoEV3Sim robot = new RobotLegoEV3Sim("EV2");
        Servidor servidor = new Servidor(app.gui.getBufferCircular(), robot, s -> app.gui.myPrint(s));
        app.gui.setServidor(servidor);
        servidor.start();
        

        // 1) criar tarefa dos manuais (GUI → buffer)
        TarefaComandosManuais tManuais = new TarefaComandosManuais(app.gui, null);

        // 2) criar tarefa dos aleatórios (lote de 5) e dizer que a “próxima” é a dos manuais
        ComandosAleatorios tAleatorios = new ComandosAleatorios(app.gui, tManuais);

        // 3) ligar a dos manuais à dos aleatórios (anelzinho)
        tManuais.setProxima(tAleatorios);

        // 4) arrancar as duas
        tManuais.start();
        tAleatorios.start();

        // 5) dizer à GUI quem são as tarefas (para os botões poderem chamá-las)
        app.gui.setTarefas(tManuais, tAleatorios);

        // 6) entra no ciclo normal
        app.run();
    }
}