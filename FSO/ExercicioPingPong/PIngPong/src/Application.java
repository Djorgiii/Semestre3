
public class Application {
	private TarefaPing ping;
	private TarefaPong pong;
	
	public Application() {
		ping = new TarefaPing();
		pong = new TarefaPong();
		
		ping.start();
		pong.start();
	}
	
	public static void main(String[] args) {
		new Application();
		
	}
}
