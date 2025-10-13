import java.util.concurrent.Semaphore;

public class Monitor(){
	
	private boolean ocupado;

	public Monitor(boolean b){
		ocupado = b;
	}

	public synchronized void monWait() { // wait() e notify() e notifyAll() so podem ser usados em metodos sincronizados
		while(ocupado) {
			try {
				wait(); // bloqueia a thread que chamou o metodo e liberta o lock do objeto
			} catch (InterruptedException e) {e.printStackTrace();}
		}
		ocupado = true;
	}
	public synchronized void monSignal() {
		ocupado = false;
		notify(); // envia uma notificação para o monitor, que uma tarefa que estaja desbloquiada pode desbloquear uma tarefa para que ela consiga entrar no recurso
	}
}