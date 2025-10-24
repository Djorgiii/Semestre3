public class ComandosAleatorios extends Tarefa {
	private BufferCircular buffercircular;
	
	public ComandosAleatorios(BufferCircular buffercircular, Tarefa t) {
		super(t);
		this.buffercircular = buffercircular;
	}
	
	public void execucao() {
	    String[] tipos = {"PARAR", "RETA", "CURVARDIREITA", "CURVARESQUERDA"};
	    for (int i = 0; i < 5; i++) {
	        String tipo = tipos[(int)(Math.random() * tipos.length)];
	        Comando comando;
	        if (tipo.equals("PARAR")) {
	            comando = new Comando(tipo, false); // Exemplo: PARAR pode usar o construtor com boolean
	        } else if (tipo.equals("RETA")) {
	            int distancia = 10 + (int)(Math.random() * 41); // 10 a 50
	            comando = new Comando(tipo, distancia, 0);
	        } else {
	            int raio = 10 + (int)(Math.random() * 21); // 10 a 30
	            int angulo = 20 + (int)(Math.random() * 71); // 20 a 90
	            comando = new Comando(tipo, angulo, raio);
	        }
	        buffercircular.inserirElemento(comando);
	        dormir(); // Simula tempo de produção
	    }
	}

}