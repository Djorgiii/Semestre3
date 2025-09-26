import java.util.concurrent.Semaphore;

public class TarefaPing extends Thread {
	
	private Semaphore semaforo;
	
	public TarefaPing(Semaphore sem) {
		this.semaforo = sem;
	}

	public void dormirRandom(int time) {
		try {
			Thread.sleep((long) (Math.random()*time));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void run() {
		while(true) {
			try {
				semaforo.acquire();
			} catch (InterruptedException e) {e.printStackTrace();}
				
				// inicio  da zona critica
				System.out.print("Ping ");
				dormirRandom(1000);
				//final da zona critica
				
				semaforo.release();
				dormirRandom(1000);
			}	
		}
	}
