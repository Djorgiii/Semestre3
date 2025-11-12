import java.util.function.Consumer;

public class Servidor extends Tarefa{
	private BufferCircular buffercircular;
	private RobotLegoEV3 asdrubal;
	private final BaseDados bd;
	private Consumer<String> printCallback;
	private int contadorAleatorios = 0;
    private static final int TOTAL_ALEATORIOS = 5;
	
	
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
	    	
	    	if(bd != null && bd.isPausaServidor()) {
	    		if (printCallback != null) printCallback.accept("[Servidor] em pausa.");
	            bd.getPausaSem().acquireUninterruptibly(); // aguarda retoma
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
	            	printCallback.accept(pos+1 + " - " + movimento.toString());
	            }
	            String tipo = movimento.getTipo().toUpperCase();
	            switch (tipo) {
	                case "RETA":
	                    asdrubal.Reta(movimento.getArg1());
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                case "CURVARDIREITA":
	                    asdrubal.CurvarDireita(movimento.getArg1(), movimento.getArg2());
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                case "CURVARESQUERDA":
	                    asdrubal.CurvarEsquerda(movimento.getArg1(), movimento.getArg2());
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                case "PARAR":
	                    asdrubal.Parar(false);
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                default:
	                    break;
	            }
	            if (!tipo.equals("PARAR")) {
	            	asdrubal.Parar(false);
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