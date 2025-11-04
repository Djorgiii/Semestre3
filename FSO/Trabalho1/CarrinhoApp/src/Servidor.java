import java.util.function.Consumer;

public class Servidor extends Tarefa{
	private BufferCircular buffercircular;
	private RobotLegoEV3 asdrubal;
	private Consumer<String> printCallback;
	private int contadorAleatorios = 0;
    private static final int TOTAL_ALEATORIOS = 5;
	
    private static final double VELOCIDADE_CM_POR_MS = 0.02; // 20 cm/s = 0.02 cm/ms
    private static final int TEMPO_COMUNICACAO_MS = 100;

	
	public Servidor(BufferCircular buffercircular, RobotLegoEV3 asdrubal, Consumer<String> printCallback) {
	    super(null);
		this.buffercircular = buffercircular;
	    this.asdrubal = asdrubal;
	    this.printCallback = printCallback;
	}
	
	public void Reta(int distancia) {
		buffercircular.inserirElemento(new Comando("RETA", distancia, 0));
	}
	
	public void CurvarDireita(int raio, int angulo) {
		buffercircular.inserirElemento(new Comando("CURVARDIREITA", raio, angulo));
	}
	
	public void CurvarEsquerda(int raio, int angulo) {
		buffercircular.inserirElemento(new Comando("CURVARESQUERDA", raio, angulo));
	}
	
	public void Parar(boolean b) {
		buffercircular.inserirElemento(new Comando("PARAR", b));
	}
	
	public void resetContadorAleatorios() {
        contadorAleatorios = 0;
    }
	
	public BufferCircular getBufferCircular() {
	    return buffercircular;
	}

	
	public void execucao() {
	    while (true) {
	        Comando comando = buffercircular.removerElemento();
	        boolean isManual = comando.isManual();
	        int pos = buffercircular.getLastRemovedIndex();
	        if (comando != null) {
	            if (isManual) {
	                if (printCallback != null) printCallback.accept("Comando manual recebido: " + comando.toString());
	            } else {
	            	printCallback.accept(pos+1 + " - " + comando.toString());
	            }
	            String tipo = comando.getTipo().toUpperCase();
	            
                int tempoExecucao = 0;

	            switch (tipo) {
	                case "RETA":
                        tempoExecucao = (int) (Math.abs(comando.getArg1()) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
	                    asdrubal.Reta(comando.getArg1());
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                case "CURVARDIREITA":
                        double anguloRadDir = comando.getArg2() * Math.PI / 180.0;
                        tempoExecucao = (int) ((comando.getArg1() * anguloRadDir) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
	                    asdrubal.CurvarDireita(comando.getArg1(), comando.getArg2());
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                case "CURVARESQUERDA":
	                	double anguloRadEsq = comando.getArg2() * Math.PI / 180.0;
                        tempoExecucao = (int) ((comando.getArg1() * anguloRadEsq) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
	                    asdrubal.CurvarEsquerda(comando.getArg1(), comando.getArg2());
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                case "PARAR":
                        tempoExecucao = TEMPO_COMUNICACAO_MS;
	                    asdrubal.Parar(false);
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                default:
	                    break;
	            }
	            
                // ⏱️ aplicar o tempo de execução calculado
                try {
                    Thread.sleep(tempoExecucao);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
	            
	            if (!tipo.equals("PARAR")) {
	            	asdrubal.Parar(false);
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