import java.util.concurrent.Semaphore;

public class BaseDados {
    private boolean terminar;
    private RobotLegoEV3 robot;
    private boolean robotAberto;
    private int distancia;
    private int raio;
    private int angulo;
    private Servidor servidor;
    private volatile boolean aleatoriosOn;
    private final Semaphore produtorMux = new Semaphore(1, true);
    private volatile boolean pausaServidor;
    private final Semaphore pausaSem = new Semaphore(0, true);
    
    private final Semaphore ev3Sem = new Semaphore(1, true);

    
    public Semaphore getEv3Sem() {
        return ev3Sem;
    }

    
    public Semaphore getProdutorMux() {
        return produtorMux;
    }
    
    public boolean isAleatoriosOn() {
		return aleatoriosOn;
	}
    
    public boolean isPausaServidor() {
    	return pausaServidor; 
    	}
    
    public void setPausaServidor(boolean pausaServidor) { 
    	this.pausaServidor = pausaServidor; 
    	}

    public Semaphore getPausaSem() { 
    	return pausaSem; 
    	}

	public void setAleatoriosOn(boolean aleatoriosOn) {
		this.aleatoriosOn = aleatoriosOn;
	}

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