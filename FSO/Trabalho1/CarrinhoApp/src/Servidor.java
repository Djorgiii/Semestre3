public class Servidor extends Tarefa{
	private BufferCircular buffercircular;
	private RobotLegoEV3 asdrubal;
	
	public Servidor(BufferCircular buffercircular, RobotLegoEV3 asdrubal, Tarefa proxima) {
		super(proxima);
		this.buffercircular = buffercircular;
		this.asdrubal = asdrubal;
	}
	
	public void Reta(int distancia) {
		asdrubal.Reta(distancia);
	}
	
	public void CurvarDireita(int angulo, int raio) {
		asdrubal.CurvarDireita(angulo, raio);
	}
	
	public void CurvarEsquerda(int angulo, int raio) {
		asdrubal.CurvarEsquerda(angulo, raio);
	}
	
	public void Parar(boolean b) {
		asdrubal.Parar(b);
	}

	
	public void execucao() {
	    while (true) {
	        Comando comando = buffercircular.removerElemento();
	        if (comando != null) {
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
}