public class RobotLegoEV3Sim {
	String nome;
	
	public RobotLegoEV3Sim(String nome) {
		this.nome = nome;
	}
	
	public boolean OpenEV3(String nomeRobot) {
		nome = nomeRobot;
		System.out.println("Robot " + nome + " connected");		
		return true;
	}
	
	public void CloseEV3() {
		System.out.println("Robot " + nome + " disconnected");
	}
	
	// Constantes para simulação
	private static final double VELOCIDADE_CM_POR_MS = 0.02; // 20 cm/s = 0.02 cm/ms
	private static final int TEMPO_COMUNICACAO_MS = 100;

	public void Reta(int distancia) {
		System.out.println("Robot " + nome + " moving forward " + distancia + " cm");
		int tempo = (int)(Math.abs(distancia) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
		try { Thread.sleep(tempo); } catch (InterruptedException e) {}
	}

	public void CurvarDireita(int raio, int angulo) {
		System.out.println("Robot " + nome + " turning right " + angulo + " degrees");
		double anguloRad = angulo * Math.PI / 180.0;
		int tempo = (int)((raio * anguloRad) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
		try { Thread.sleep(tempo); } catch (InterruptedException e) {}
	}

	public void CurvarEsquerda(int raio, int angulo) {
		System.out.println("Robot " + nome + " turning left " + angulo + " degrees");
		double anguloRad = angulo * Math.PI / 180.0;
		int tempo = (int)((raio * anguloRad) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
		try { Thread.sleep(tempo); } catch (InterruptedException e) {}
	}

	public void Parar(boolean resposta) {
		System.out.println("Robot " + nome + " stopped");
		try { Thread.sleep(TEMPO_COMUNICACAO_MS); } catch (InterruptedException e) {}
	}
}