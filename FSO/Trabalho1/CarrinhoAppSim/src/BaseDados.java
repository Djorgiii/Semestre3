public class BaseDados {
    private boolean terminar;
    private RobotLegoEV3Sim robot;
    private boolean robotAberto;
    private int distancia;
    private int raio;
    private int angulo;
    private Servidor servidor;

    public int getRaio() {
		return raio;
	}

	public void setRaio(int raio) {
		this.raio = raio;
	}

	public int getAngulo() {
		return angulo;
	}

	public void setAngulo(int angulo) {
		this.angulo = angulo;
	}

	public int getDistancia() {
		return distancia;
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
        robot = new RobotLegoEV3Sim("Asdrubal");
        terminar = false;
        robotAberto = false;
    }

    public RobotLegoEV3Sim getRobot() {
        return robot;
    }

    public Servidor getServidor() {
        return servidor;
    }

    public void setServidor(Servidor servidor) {
        this.servidor = servidor;
    }

    public boolean isTerminar() {
        return terminar;
    }

    public void setTerminar(boolean terminar) {
        this.terminar = terminar;
    }
}