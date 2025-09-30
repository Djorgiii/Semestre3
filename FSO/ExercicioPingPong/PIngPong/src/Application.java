import java.util.concurrent.Semaphore;

public class Application {
	private TarefaPing ping;
	private TarefaPong pong;
	private Semaphore semPing;
	private Semaphore semPong;

	public Application() {
		// Solucao com dois semaforos
		
		semPing = new Semaphore(1); // ping comeca
		semPong = new Semaphore(0);
		
		ping = new TarefaPing(semPing, semPong);
		pong = new TarefaPong(semPing, semPong);
		
		ping.start();
		new Thread(pong).start();
		
	}
	
	public static void main(String[] args) {
		new Application(); // reserva a memoria e chama o construtor
		
	}
}
