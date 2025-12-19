import java.util.Random;
import java.util.concurrent.Semaphore;

public class EvitarObstaculo extends Tarefa {

    private final GUI gui;
    private GuiGravador guiGravador;
    private final Random rnd = new Random();
private final int sensorToquePort = RobotLegoEV3.S_1; // Porta do sensor de toque 

    public EvitarObstaculo(GUI gui, GuiGravador guiGravador) {
		super();
		this.gui = gui;
		this.guiGravador = guiGravador;
    }

    @Override
    public void execucao() {
        while (gui.getBd().isRobotAberto()) {
            boolean aleatoriosAntes = gui.getBd().isAleatoriosOn();
            RobotLegoEV3 robot = gui.getBd().getRobot();

            int toque = 0;
            Semaphore ev3Sem = gui.getBd().getEv3Sem();

            try {
                ev3Sem.acquire(); // esperar acesso ao EV3
                try {
                    toque = robot.SensorToque(sensorToquePort);
                } finally {
                    ev3Sem.release();
                }
            } catch (InterruptedException e) {
                // simplesmente ignora e continua o loop
            }
            
            boolean gravar = guiGravador.getGravador().isAGravar();

            if (toque == 1) {
                gui.myPrint("[EVITAR] Obstáculo detectado no sensor! Iniciando evasão...");
                
                gui.getBd().setPausaServidor(true);
                gui.getBd().setAleatoriosOn(false);

                gui.myPrint("[EVITAR] PARAR(true)");
                robot.Parar(true); // Parar imediatamente
                if (gravar) {
                    guiGravador.getGravador().registar(new Movimento("PARAR", true));
				}
                

                gui.myPrint("[EVITAR] RETA(-20)");
                robot.Reta(-20); // Recuar 20 cm
                if (gravar) {
                    guiGravador.getGravador().registar(new Movimento("RETA", -20, 0));
				}

                boolean direita = rnd.nextBoolean();
                if (direita) {
                    gui.myPrint("[EVITAR] CURVARDIREITA(0, 90)");
                    robot.CurvarDireita(0, 90); // Curvar à direita
                    if (gravar) {
                        guiGravador.getGravador().registar(new Movimento("CURVARDIREITA", 0, 90));
    				}
                } else {
                    gui.myPrint("[EVITAR] CURVARESQUERDA(0, 90)");
                    robot.CurvarEsquerda(0, 90); // Curvar à esquerda
                    if (gravar) {
                        guiGravador.getGravador().registar(new Movimento("CURVARESQUERDA", 0, 90));
    				}

                }

                gui.myPrint("[EVITAR] PARAR(false)");
                robot.Parar(false);
                if (gravar) {
                    guiGravador.getGravador().registar(new Movimento("PARAR", false));
				}


                gui.myPrint("[EVITAR] Evasão concluída");

                if (aleatoriosAntes) {
                    gui.getBd().setAleatoriosOn(true);
                } else {
                    gui.getBd().setAleatoriosOn(false);
                }

                gui.getBd().setPausaServidor(false);
                gui.getBd().getPausaSem().release();
            }

            dormir();
        }

        bloquear();
    }
}
