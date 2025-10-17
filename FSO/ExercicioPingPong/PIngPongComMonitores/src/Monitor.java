

public class Monitor{
	
	private boolean resursoOcupado;
	
	public Monitor(boolean b) {
		resursoOcupado = b;
	}
	
	public  synchronized void acquire() {
		while (resursoOcupado) {
				try {
				wait();
				} catch (InterruptedException e ) {}
				
		}
		resursoOcupado = true;
	
	}
		
	public synchronized void release() {
		
		notify();
		resursoOcupado = false;
	
	}
	
	
	
}