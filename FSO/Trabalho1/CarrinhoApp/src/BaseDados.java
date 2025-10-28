public class BaseDados {
    private boolean terminar;
    private RobotLegoEV3 robot;
    private GUI gui;
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
    public void setGui(GUI gui) {
        this.gui = gui;
    }
    public void myPrint(String s) {
        if (gui != null)
            gui.myPrint(s); // passa o texto para a GUI
    }
    public BaseDados() {
        robot = new RobotLegoEV3();
        terminar = false;
        robotAberto = false;
    }

    public RobotLegoEV3 getRobot() {
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