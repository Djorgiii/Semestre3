import java.util.concurrent.Semaphore;

public abstract class Tarefa extends Thread{
	private final byte BLOQUEADO = 0;
	private final byte EXECUCAO = 1;
	private final byte IDLE = 2;
	private Semaphore sem;
	private byte state;
	protected Tarefa proxima;
	
	public Tarefa(Tarefa t) {
		state=BLOQUEADO;
		sem = new Semaphore(0);
		proxima = t;
	}
	public void setProxima(Tarefa t) {
		proxima = t;
	}

	public void desbloquear() {
		state = EXECUCAO;
		sem.release();
	}
	public void bloquear() {
		state = BLOQUEADO;
		try {
			sem.acquire();
			
		}catch (InterruptedException e) {e.printStackTrace();}
		
	}
	private void esperaTrabalho() {
		try {
			sem.acquire();
		} catch (InterruptedException e) {e.printStackTrace();}
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
