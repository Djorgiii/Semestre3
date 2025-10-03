
public class TarefaPong extends tarefa {
	
	public TarefaPong(tarefa seg) {
		super(seg);
	}

	public void execucao() {
		System.out.println("PONG");
		proxima.desbloquear();
		this.bloquear();
	}
}
