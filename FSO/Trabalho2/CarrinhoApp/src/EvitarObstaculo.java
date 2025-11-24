import java.util.Random;

public class EvitarObstaculo extends Tarefa {

    //private final RobotLegoEV3 robot;
    private final GUI gui;
    private final Random rnd = new Random();
    private final int sensorToquePort = RobotLegoEV3.S_1; // Porta do sensor de toque
    
    public EvitarObstaculo(Tarefa proxima, GUI gui) {
        super(proxima);  // normalmente 'proxima' = tAleatorios
        //this.robot = robot;
        this.gui = gui;
    }
    
    @Override
    public void execucao() {
        while (gui.getBd().isRobotAberto()) {
            boolean aleatoriosAntes = gui.getBd().isAleatoriosOn();
            RobotLegoEV3 robot = gui.getBd().getRobot();

            // 🔒 proteger acesso ao EV3
            int toque;
            java.util.concurrent.Semaphore ev3Sem = gui.getBd().getEv3Sem();
            ev3Sem.acquireUninterruptibly();
            try {
                toque = robot.SensorToque(sensorToquePort);
            } finally {
                ev3Sem.release();
            }

            if (toque == 1) {
                gui.myPrint("[EVITAR] Obstáculo detectado no sensor! Iniciando evasão...");

                gui.getBd().setPausaServidor(true);
                gui.getBd().setAleatoriosOn(false);
                gui.myPrint("[EVITAR] PARAR(true)");
                //gui.getBufferCircular().inserirElemento(new Movimento("PARAR", true));
                gui.getBd().getRobot().Parar(true); // Parar imediatamente

                gui.myPrint("[EVITAR] RETA(-20, 0)");
                //gui.getBufferCircular().inserirElemento(new Movimento("RETA", -20, 0));
                gui.getBd().getRobot().Reta(-20); // Recuar 20 cm

                boolean direita = rnd.nextBoolean();
                if (direita) {
                    gui.myPrint("[EVITAR] CURVARDIREITA(20, 90)");
                    //gui.getBufferCircular().inserirElemento(new Movimento("CURVARDIREITA", 20, 90));
                    gui.getBd().getRobot().CurvarDireita(0, 90); // Curvar à direita
                } else {
                    gui.myPrint("[EVITAR] CURVARESQUERDA(20, 90)");
                    //gui.getBufferCircular().inserirElemento(new Movimento("CURVARESQUERDA", 20, 90));
                    gui.getBd().getRobot().CurvarEsquerda(0, 90); // Curvar à esquerda
                }

                gui.myPrint("[EVITAR] PARAR(false)");
                //gui.getBufferCircular().inserirElemento(new Movimento("PARAR", false));
                gui.getBd().getRobot().Parar(false); // Parar a manobra

                gui.myPrint("[EVITAR] Evasão concluída");

                if (aleatoriosAntes) {
                    gui.getBd().setAleatoriosOn(true);
                    if (proxima != null) proxima.desbloquear();
                } else {
                    gui.getBd().setAleatoriosOn(false);
                }
                gui.getBd().setPausaServidor(false);
                gui.getBd().getPausaSem().release();
            }

            // opcional: evitar busy-wait violento
            dormir();
        }

        bloquear();
    }



}