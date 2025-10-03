
public class Application {
	private TarefaPing ping;
	private TarefaPong pong;

	public Application() {
		// Solucao com dois semaforos
		
		ping = new TarefaPing(null);
		pong = new TarefaPong(ping);
		ping.setProxima(pong);
		
		ping.desbloquear();
		
		ping.start();
		pong.start();
		
	}
	
	public static void main(String[] args) {
		new Application(); // reserva a memoria e chama o construtor
		
	}
}
