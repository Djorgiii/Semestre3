import java.util.function.Consumer;

public class Servidor extends Thread{
	private BufferCircular buffercircular;
	private RobotLegoEV3Sim asdrubal;
	private Consumer<String> printCallback;
	private int contadorAleatorios = 0;
    private static final int TOTAL_ALEATORIOS = 5;
	
	
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
	
	public void resetContadorAleatorios() {
        contadorAleatorios = 0;
    }

	
	public void execucao() {
	    while (true) {
	        Comando comando = buffercircular.removerElemento();
	        int pos = buffercircular.getLastRemovedIndex();
	        if (comando != null) {
	            if (printCallback != null) printCallback.accept(pos+1 + " - " + comando.toString());
	            String tipo = comando.getTipo().toUpperCase();
	            switch (tipo) {
	                case "RETA":
	                    Reta(comando.getArg1());
	                    contadorAleatorios++;
	                    break;
	                case "CURVARDIREITA":
	                    CurvarDireita(comando.getArg1(), comando.getArg2());
	                    contadorAleatorios++;
	                    break;
	                case "CURVARESQUERDA":
	                    CurvarEsquerda(comando.getArg1(), comando.getArg2());
	                    contadorAleatorios++;
	                    break;
	                case "PARAR":
	                    Parar(true);
	                    contadorAleatorios++;
	                    break;
	                default:
	                    // Comando desconhecido
	                    break;
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