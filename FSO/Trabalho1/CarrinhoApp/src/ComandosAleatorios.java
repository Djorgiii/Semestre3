
public class ComandosAleatorios extends Tarefa {
	private BufferCircular buffercircular;
	
	public ComandosAleatorios(Tarefa t) {
		super(t);
	}
	
	

	public void execucao() {
	    String[] tipos = {"PARAR", "RETA", "CURVARDIREITA", "CURVARESQUERDA"};
	    for (int i = 0; i < 5; i++) {
	        String tipo = tipos[(int)(Math.random() * tipos.length)];
	        Comando comando;
	        if (tipo.equals("PARAR")) {
	            comando = new Comando(tipo, false); // Exemplo: PARAR pode usar o construtor com boolean
	        } else {
	            int arg1 = (int)(Math.random() * 100);
	            int arg2 = (int)(Math.random() * 100);
	            comando = new Comando(tipo, arg1, arg2);
	        }
	        buffercircular.inserirElemento(comando);
	        dormir(); // Simula tempo de produção
	    }
	}

}
