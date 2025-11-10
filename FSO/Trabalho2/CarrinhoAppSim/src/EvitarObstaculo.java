public class EvitarObstaculo extends Tarefa{

		private RobotLegoEV3Sim robot;
	
	public EvitarObstaculo(Tarefa t, RobotLegoEV3Sim robot) {
		super(t);
		this.robot = robot;
	}
	
	public void execucao() {
		// Lógica para evitar obstáculos
		System.out.println("EvitarObstaculo: Verificando sensores...");
		if (robot.getSensorUltrassonico().getDistancia() < 20) {
			System.out.println("EvitarObstaculo: Obstáculo detectado! Executando manobra de evasão.");
			robot.Parar(true);
			robot.Reta(-10); // Recuar
			robot.CurvarDireita(20, 15); // Curvar para a direita
			robot.Reta(30); // Avançar
			robot.Parar(false);
		} else {
			System.out.println("EvitarObstaculo: Caminho livre.");
		}
		
		// Após a execução, passar o controle para a próxima tarefa
		if (proxima != null) {
			proxima.desbloquear();
			this.bloquear();
		}
	}
}
