public class App {
	
    private BaseDados bd;
    
    
    // mudamos e colocamos a base de dados na aplicação
    //antes tinhas a base de dados na GUI
    // assim a gui fica espeficiamente apenas para tratar da interface da aplicação
    
    public App() {
    	// inicializa a base de dados e a gui
    	// a gui recebe um this que tem a ver com a bse de dados
    	bd = new BaseDados();
    	new GUI(this);
        
    }
    public BaseDados getBd() {
		return bd;
	}

    public void run() {
        System.out.println("A aplicação começou.");
        while(!bd.isTerminar()) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        System.out.println("A aplicação terminou.");
    }

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }
}