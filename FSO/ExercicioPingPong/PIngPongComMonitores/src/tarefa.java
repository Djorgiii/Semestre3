

public abstract class tarefa extends Thread{
	private final byte BLOQUEADO = 0;
	private final byte EXECUCAO = 1;
	private final byte IDLE = 2;
	private byte state;
	private Monitor monitor;
	protected tarefa proxima;
	
	
	public tarefa(tarefa t) {
		state=BLOQUEADO;
		monitor = new Monitor(true);
		proxima = t;
	}
	public void setProxima(tarefa t) {
		proxima = t;
	}

	public void desbloquear() {
		state = EXECUCAO;
		monitor.release();
	}
	
	public void bloquear() {
		state = BLOQUEADO;
		monitor.acquire();
	}
		
		
	private void esperaTrabalho() {
		monitor.acquire();
		}
	
	public abstract void execucao();
	
	
	public void dormir() {
		try {
			Thread.sleep((long) (Math.random()*1000));
		}catch(InterruptedException e) {e.printStackTrace();}	
			
	}
	
	public void run() {
		//fazer por etapas/ estados
		//1 que pode ficar bloqueada
		//2 execucao da zona critica onde da print
		//3 dormir / idle
		while(true) {
			switch(state) {
			case BLOQUEADO:
				esperaTrabalho();
				state = EXECUCAO;
			break;
			case EXECUCAO:
				execucao();
				if(state == EXECUCAO) {
					state = IDLE;}
			break;
			case IDLE:
				dormir();
				if (state == IDLE) {
					state = EXECUCAO;}
			break;
			}
		}
	}

}
