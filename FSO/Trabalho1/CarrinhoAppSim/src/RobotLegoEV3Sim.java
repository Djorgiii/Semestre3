
public class RobotLegoEV3Sim {
	String nome;
	
	public RobotLegoEV3Sim(String nome) {
		nome = null;
	}
	
	public boolean OpenEV3(String nomeRobot) {
		nome = nomeRobot;
		System.out.println("Robot " + nome + " connected");		
		return true;
	}
	
	public void CloseEV3() {
		System.out.println("Robot " + nome + " disconnected");
	}
	
	public void Reta(int distancia) {
		System.out.println("Robot " + nome + " moving forward " + distancia + " cm");
	}
	
	public void CurvarDireita(int raio, int angulo) {
		System.out.println("Robot " + nome + " turning right " + angulo + " degrees");
	}
	
	public void CurvarEsquerda(int raio, int angulo) {
		System.out.println("Robot " + nome + " turning left " + angulo + " degrees");
	}
	
	public void Parar(boolean resposta) {
		System.out.println("Robot " + nome + " stopped");
	}
}
