import java.util.concurrent.Semaphore;

public class TarefaPong extends Thread {
	
	private Semaphore semPing;
	private Semaphore semPong;
	
	
	public TarefaPong(Semaphore sPing, Semaphore sPong) {
		semPing = sPing;
		semPong = sPong;
	}
	public void run() {
		while(true) {
			try {
				semPong.acquire();
			
			} catch (InterruptedException e) {e.printStackTrace();}
				
				// inicio  da zona critica
				System.out.println("PONG");
			
				semPing.release();
			try {
				Thread.sleep((long) (Math.random()*1000));
			}catch(InterruptedException e) {e.printStackTrace();	
				
			}
		}
	}
}
