import java.util.Random;

public class EvitarObstaculo extends Tarefa {

    private final RobotLegoEV3 robot;
    private final GUI gui;
    private final Random rnd = new Random();
    private final int sensorToquePort = 1; // Sensor de toque ligado à porta 1

    public EvitarObstaculo(Tarefa proxima, RobotLegoEV3 robot, GUI gui) {
        super(proxima);  // normalmente 'proxima' = tAleatorios
        this.robot = robot;
        this.gui = gui;
    }

    @Override
    public void execucao() {
        // Esta tarefa fica em loop, verificando constantemente o sensor
        while (gui.getBd().isRobotAberto()) {

            // Lê o sensor de toque
            int toque = robot.SensorToque(sensorToquePort);

            // Quando detecta toque, executa a manobra
            if (toque == 1) {
                gui.myPrint("[EVITAR] Obstáculo detectado no sensor! Iniciando evasão...");

                // 1) Parar tudo imediatamente
                gui.getBd().setPausaServidor(true);
                gui.getBd().setAleatoriosOn(false);
                gui.myPrint("[EVITAR] PARAR(true)");
                robot.Parar(true);

                // 2) Evasão com logs
                gui.myPrint("[EVITAR] RETA(-20, 0)");
                robot.Reta(-20);

                boolean direita = rnd.nextBoolean();
                if (direita) {
                    gui.myPrint("[EVITAR] CURVARDIREITA(20, 90)");
                    robot.CurvarDireita(20, 90);
                } else {
                    gui.myPrint("[EVITAR] CURVARESQUERDA(20, 90)");
                    robot.CurvarEsquerda(20, 90);
                }

                gui.myPrint("[EVITAR] PARAR(false)");
                robot.Parar(false);

                gui.myPrint("[EVITAR] Evasão concluída");

                // 3) Retomar o funcionamento normal
                gui.getBd().setAleatoriosOn(true);
                if (proxima != null) proxima.desbloquear(); // reativa aleatórios
                gui.getBd().setPausaServidor(false);
                gui.getBd().getPausaSem().release();        // acorda o Servidor
            }

        	dormir();
        }

        // Se o robot for fechado, a tarefa bloqueia até ser reativada
        bloquear();
    }
}
