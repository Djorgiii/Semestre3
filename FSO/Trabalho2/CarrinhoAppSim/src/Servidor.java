import java.util.function.Consumer;

public class Servidor extends Tarefa{
	private BufferCircular buffercircular;
	private RobotLegoEV3Sim asdrubal;
	private Consumer<String> printCallback;
	private int contadorAleatorios = 0;
    private static final int TOTAL_ALEATORIOS = 5;
	
	
	public Servidor(BufferCircular buffercircular, RobotLegoEV3Sim asdrubal, Consumer<String> printCallback) {
	    super(null);
		this.buffercircular = buffercircular;
	    this.asdrubal = asdrubal;
	    this.printCallback = printCallback;
	}
	
	public void Reta(int distancia) {
		buffercircular.inserirElemento(new Comando("RETA", distancia, 0));
	}
	
	public void CurvarDireita(int distancia, int raio) {
		buffercircular.inserirElemento(new Comando("CURVARDIREITA", distancia, raio));
	}
	
	public void CurvarEsquerda(int distancia, int raio) {
		buffercircular.inserirElemento(new Comando("CURVARESQUERDA", distancia, raio));
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
	            switch (tipo) {
	                case "RETA":
	                    asdrubal.Reta(comando.getArg1());
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                case "CURVARDIREITA":
	                    asdrubal.CurvarDireita(comando.getArg1(), comando.getArg2());
	                    if (!isManual) contadorAleatorios++;
	                    break;
	                case "CURVARESQUERDA":
	                    asdrubal.CurvarEsquerda(comando.getArg1(), comando.getArg2());
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