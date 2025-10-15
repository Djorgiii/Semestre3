public class Servidor extends Thread{
	private BufferCircular buffercircular;
	private RobotLegoEV3 asdrubal;
	
	
	public Servidor(BufferCircular buffercircular, RobotLegoEV3 asdrubal) {
	    this.buffercircular = buffercircular;
	    this.asdrubal = asdrubal;
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
	            switch (comando.getTipo()) {
	                case "reta":
	                    Reta(comando.getArg1());
	                    break;
	                case "curvarDireita":
	                    CurvarDireita(comando.getArg1(), comando.getArg2());
	                    break;
	                case "curvarEsquerda":
	                    CurvarEsquerda(comando.getArg1(), comando.getArg2());
	                    break;
	                case "parar":
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