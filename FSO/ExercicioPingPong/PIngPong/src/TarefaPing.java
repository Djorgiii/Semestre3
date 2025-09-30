import java.util.concurrent.Semaphore;

public class TarefaPing extends Thread { 
	private Semaphore semPing;
	private Semaphore semPong;
	
	
	public TarefaPing(Semaphore sPing, Semaphore sPong) {
		semPing = sPing;
		semPong = sPong;
	}
	
	public void run() {
		while(true) {
			try {
				semPing.acquire();
			
			} catch (InterruptedException e) {e.printStackTrace();}
	
				// inicio  da zona critica
				System.out.print("Ping ");
				
				semPong.release();
			try {
				Thread.sleep((long) (Math.random()*1000));
			}catch (InterruptedException e) {e.printStackTrace();}
		}	
	}
}
