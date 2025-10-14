
public class Comando {
	private String tipo;
	private int arg1;
	private int arg2;
	private boolean b;
	
	public Comando(String tipo, int arg1, int arg2) {
		this.tipo = tipo;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}
	
	public Comando(String tipo, boolean b) {
		this.tipo = tipo;
		this.b = b;
	}
	
	public String getTipo() {
		return tipo;
	}
	
	public int getArg1() {
		return arg1;
	}
	
	public int getArg2() {
		return arg2;
	}
	
}
