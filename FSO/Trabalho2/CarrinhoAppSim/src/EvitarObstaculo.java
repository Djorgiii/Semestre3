import java.util.Random;

public class EvitarObstaculo extends Tarefa {

    private final RobotLegoEV3Sim robot;
    private GUI gui;

    public EvitarObstaculo(Tarefa proxima, RobotLegoEV3Sim robot, GUI gui) {
        super(proxima);           // proxima deve ser tAleatorios (ver App)
        this.robot = robot;
        this.gui = gui;
    }

    @Override
    public void execucao() {
        // 1) parar imediatamente o movimento em curso
        robot.Parar(true);

        // 2) pausar servidor (não consumir mais do buffer)
        gui.getBd().setPausaServidor(true);

        // 3) travar produção aleatória
        gui.getBd().setAleatoriosOn(false);

        // 4) manobra de evasão
        System.out.println("EvitarObstaculo: Evasão iniciada.");
        robot.Reta(-20);
        gui.myPrint("RETA(-20,0)");
        Random rnd = new Random();
        boolean direita = rnd.nextBoolean();
        if (direita) {
			robot.CurvarDireita(0, 90);
			gui.myPrint("CURVARDIREITA(0,90)");
		} else {
			robot.CurvarEsquerda(0, 90);
			gui.myPrint("CURVARESQUERDA(0,90)");
		}
        robot.Parar(false);
        gui.myPrint("PARAR(false)");
        System.out.println("EvitarObstaculo: Evasão concluída.");

        // 5) RETOMAR automaticamente
        gui.getBd().setPausaServidor(false);
        gui.getBd().getPausaSem().release();         // acorda Servidor
        gui.getBd().setAleatoriosOn(true);           // reativa produção
        if (proxima != null) proxima.desbloquear();  // acorda tAleatorios

        // 6) volta a dormir até novo clique
        bloquear();
    }
}
