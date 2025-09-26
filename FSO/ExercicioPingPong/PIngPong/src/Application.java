import java.util.concurrent.Semaphore;

public class Application {
	private TarefaPing ping;
	private TarefaPong pong;
	
	private Semaphore semaforo;
	
	
	public Application() {
		semaforo = new Semaphore(1);
		
		ping = new TarefaPing(semaforo);
		pong = new TarefaPong(semaforo);
		
		ping.start();
		new Thread(pong).start();
	}
	
	public static void main(String[] args) {
		new Application(); // reserva a memoria e chama o construtor
		
	}
}
