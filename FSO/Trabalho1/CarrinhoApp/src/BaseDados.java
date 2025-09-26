
public class BaseDados {
    private boolean terminar;
    private RobotLegoEV3 robot;
    private boolean robotAberto;
    private int distancia;
    private int angulo;
    private int raio;
    private int numAleatorio;

    public int getDistancia() {
		return distancia;
	}

	public int getAngulo() {
		return angulo;
	}
	
	public int getNumAleatorio() {
		return numAleatorio;
	}

	public void setNumAleatorio(int numAleatorio) {
		this.numAleatorio = numAleatorio;
	}

	public void setAngulo(int angulo) {
		this.angulo = angulo;
	}

	public int getRaio() {
		return raio;
	}

	public void setRaio(int raio) {
		this.raio = raio;
	}

	public void setDistancia(int distancia) {
		this.distancia = distancia;
	}

	public boolean isRobotAberto() {
        return robotAberto;
    }

    public void setRobotAberto(boolean robotAberto) {
        this.robotAberto = robotAberto;
    }

    public BaseDados() {
        robot = new RobotLegoEV3();
        terminar = false;
        robotAberto = false;
    }

    public RobotLegoEV3 getRobot() {
        return robot;
    }

    public boolean isTerminar() {
        return terminar;
    }

    public void setTerminar(boolean terminar) {
        this.terminar = terminar;
    }
}
