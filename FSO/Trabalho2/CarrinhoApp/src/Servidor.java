import java.util.function.Consumer;

public class Servidor extends Tarefa{
	private BufferCircular buffercircular;
	private RobotLegoEV3 asdrubal;
	private final BaseDados bd;
	private Consumer<String> printCallback;
	private int contadorAleatorios = 0;
    private static final int TOTAL_ALEATORIOS = 5;
    private static final double VELOCIDADE_CM_POR_MS = 0.02;
    private static final int TEMPO_COMUNICACAO_MS = 100;
	
	
	public Servidor(BufferCircular buffercircular, RobotLegoEV3 asdrubal, BaseDados bd, Consumer<String> printCallback) {
	    super(null);
		this.buffercircular = buffercircular;
	    this.asdrubal = asdrubal;
	    this.bd = bd;
	    this.printCallback = printCallback;
	}
	
	public void Reta(int distancia) {
		buffercircular.inserirElemento(new Movimento("RETA", distancia, 0));
	}
	
	public void CurvarDireita(int distancia, int raio) {
		buffercircular.inserirElemento(new Movimento("CURVARDIREITA", distancia, raio));
	}
	
	public void CurvarEsquerda(int distancia, int raio) {
		buffercircular.inserirElemento(new Movimento("CURVARESQUERDA", distancia, raio));
	}
	
	public void Parar(boolean b) {
		buffercircular.inserirElemento(new Movimento("PARAR", b));
	}
	
	public void resetContadorAleatorios() {
        contadorAleatorios = 0;
    }
	
	public BufferCircular getBufferCircular() {
	    return buffercircular;
	}

	public void execucao() {
	    while (true) {

	        if (bd != null && bd.isPausaServidor()) {
	            if (printCallback != null) printCallback.accept("[Servidor] em pausa.");
	            bd.getPausaSem().acquireUninterruptibly();
	            if (printCallback != null) printCallback.accept("[Servidor] retomado.");
	            continue;
	        }

	        Movimento movimento = buffercircular.removerElemento();
	        boolean isManual = movimento.isManual();
	        int pos = buffercircular.getLastRemovedIndex();

	        if (movimento != null) {
	            if (isManual) {
	                if (printCallback != null) printCallback.accept("Comando manual recebido: " + movimento.toString());
	            } else {
	                printCallback.accept(pos + 1 + " - " + movimento.toString());
	            }

	            String tipo = movimento.getTipo().toUpperCase();
	            int tempoExecucao = 0;

	            // 🔒 obter semáforo do EV3
	            java.util.concurrent.Semaphore ev3Sem = bd.getEv3Sem();

	            switch (tipo) {
	                case "RETA":
	                    tempoExecucao = (int) (Math.abs(movimento.getArg1()) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
	                    ev3Sem.acquireUninterruptibly();
	                    try {
	                        asdrubal.Reta(movimento.getArg1());
	                    } finally {
	                        ev3Sem.release();
	                    }
	                    if (!isManual) contadorAleatorios++;
	                    break;

	                case "CURVARDIREITA":
	                    double anguloRadDir = movimento.getArg2() * Math.PI / 180.0;
	                    tempoExecucao = (int) ((movimento.getArg1() * anguloRadDir) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
	                    ev3Sem.acquireUninterruptibly();
	                    try {
	                        asdrubal.CurvarDireita(movimento.getArg1(), movimento.getArg2());
	                    } finally {
	                        ev3Sem.release();
	                    }
	                    if (!isManual) contadorAleatorios++;
	                    break;

	                case "CURVARESQUERDA":
	                    double anguloRadEsq = movimento.getArg2() * Math.PI / 180.0;
	                    tempoExecucao = (int) ((movimento.getArg1() * anguloRadEsq) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
	                    ev3Sem.acquireUninterruptibly();
	                    try {
	                        asdrubal.CurvarEsquerda(movimento.getArg1(), movimento.getArg2());
	                    } finally {
	                        ev3Sem.release();
	                    }
	                    if (!isManual) contadorAleatorios++;
	                    break;

	                case "PARAR":
	                    tempoExecucao = TEMPO_COMUNICACAO_MS;
	                    ev3Sem.acquireUninterruptibly();
	                    try {
	                        asdrubal.Parar(false);
	                    } finally {
	                        ev3Sem.release();
	                    }
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                default:
	                    break;
	            }

	            try {
	                Thread.sleep(tempoExecucao);
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }

	            if (!tipo.equals("PARAR")) {
	                // também protegido
	                java.util.concurrent.Semaphore ev3Sem2 = bd.getEv3Sem();
	                ev3Sem2.acquireUninterruptibly();
	                try {
	                    asdrubal.Parar(false);
	                } finally {
	                    ev3Sem2.release();
	                }
	                try { Thread.sleep(TEMPO_COMUNICACAO_MS); }
	                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
	            }

	            if (contadorAleatorios == TOTAL_ALEATORIOS) {
	                if (printCallback != null) printCallback.accept("Sequência de 5 movimentos aleatórios concluída.");
	                contadorAleatorios = 0;
	            }
	        }
	    }
	}


	
	@Override
	public void run() {
	    execucao();
	}
}