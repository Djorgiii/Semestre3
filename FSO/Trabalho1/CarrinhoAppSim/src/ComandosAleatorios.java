public class ComandosAleatorios extends Tarefa {
	private BufferCircular buffercircular;
	private Servidor servidor;
	
	public ComandosAleatorios(BufferCircular buffercircular, Servidor servidor) {
		super(null);
		this.buffercircular = buffercircular;
		this.servidor = servidor;
	}
	
	public void execucao() {
		if (servidor != null) {
			servidor.resetContadorAleatorios();
		}
		String[] tipos = {"PARAR", "RETA", "CURVARDIREITA", "CURVARESQUERDA"};
		for (int i = 0; i < 5; i++) {
			String tipo = tipos[(int)(Math.random() * tipos.length)];
			Comando comando;
			if (tipo.equals("PARAR")) {
				comando = new Comando(tipo, false);
			} else if (tipo.equals("RETA")) {
				int distancia = 10 + (int)(Math.random() * 41);
				comando = new Comando(tipo, distancia, 0);
			} else {
				int raio = 10 + (int)(Math.random() * 21);
				int angulo = 20 + (int)(Math.random() * 71);
				comando = new Comando(tipo, angulo, raio);
			}
			buffercircular.inserirElemento(comando);
			dormir();
		}
		bloquear();
	}

}