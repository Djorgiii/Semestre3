import java.util.function.Consumer;

public class Servidor extends Thread{
	private BufferCircular buffercircular;
	private RobotLegoEV3Sim asdrubal;
	private Consumer<String> printCallback;
	
	
	public Servidor(BufferCircular buffercircular, RobotLegoEV3Sim asdrubal, Consumer<String> printCallback) {
	    this.buffercircular = buffercircular;
	    this.asdrubal = asdrubal;
	    this.printCallback = printCallback;
	}
	
	public void Reta(int distancia) {
		asdrubal.Reta(distancia);
	}
	
	public void CurvarDireita(int distancia, int raio) {
		asdrubal.CurvarDireita(distancia, raio);
	}
	
	public void CurvarEsquerda(int distancia, int raio) {
		asdrubal.CurvarEsquerda(distancia, raio);
	}
	
	public void Parar(boolean b) {
		asdrubal.Parar(b);
	}

	
	public void execucao() {
	    while (true) {
	        Comando comando = buffercircular.removerElemento();
	        if (comando != null) {
	            if (printCallback != null) printCallback.accept("Consumido: " + comando.toString());
	            switch (comando.getTipo()) {
	                case "RETA":
	                    Reta(comando.getArg1());
	                    break;
	                case "CURVARDIREITA":
	                    CurvarDireita(comando.getArg1(), comando.getArg2());
	                    break;
	                case "CURVARESQUERDA":
	                    CurvarEsquerda(comando.getArg1(), comando.getArg2());
	                    break;
	                case "PARAR":
	                    Parar(true);
	                    break;
	                default:
	                    // Comando desconhecido
	                    break;
	            }
	        }
	    }
	}
	
	@Override
	public void run() {
	    execucao();
	}
}