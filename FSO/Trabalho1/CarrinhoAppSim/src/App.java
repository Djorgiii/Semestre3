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
        // Start the Servidor thread to consume commands
        RobotLegoEV3Sim robot = new RobotLegoEV3Sim("EV2");
        Servidor servidor = new Servidor(app.gui.getBufferCircular(), robot, s -> app.gui.myPrint(s));
        app.gui.setServidor(servidor);
        servidor.start();
        app.run();
    }
}