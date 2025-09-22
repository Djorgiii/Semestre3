
public class TarefaPong extends Thread {
	
	public void run() {
		while(true) {
			System.out.println("PONG ");
			try {
				Thread.sleep((long) (Math.random()*1000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
