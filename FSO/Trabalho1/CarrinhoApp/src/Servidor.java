
public class Servidor extends Thread{
	private BufferCircular buffercircular;
	private RobotLegoEV3 asdrubal;
	
	
	public void Reta(int distancia) {
		buffercircular.removerElemento();
	}
	
	public void CurvarDireita(int distancia, int raio) {
		buffercircular.removerElemento(new Comando("CURVARDIREITA", distancia, raio));
	}
	
	public void CurvarEsquerda(int distancia, int raio) {
		buffercircular.removerElemento(new Comando("CURVARESQUERDA", distancia, raio));
	}
	
	public void Parar(boolean b) {
		buffercircular.removerElemento(new Comando("PARAR", b));
	}
}
