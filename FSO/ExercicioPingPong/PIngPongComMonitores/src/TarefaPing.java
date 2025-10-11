
public class TarefaPing extends tarefa { 
	
	public TarefaPing(tarefa seg) {
		super(seg);
	}
	
	public void execucao() {
		System.out.print("Ping ");
		proxima.desbloquear();
		this.bloquear();
	}
}
