public class Movimento {
	private String tipo;
	private int arg1;
	private int arg2;
	private boolean b;
	private boolean manual;
	
	public Movimento(String tipo, int arg1, int arg2) {
		this.tipo = tipo;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}
	
	public Movimento(String tipo, boolean b) {
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

	public boolean isB() {
		return b;
	}
	public boolean isManual() { return manual; }
	
	@Override
	public String toString() {
	    switch (tipo) {
	        case "parar":
	            return "PARAR";
	        case "reta":
	            return "RETA(" + arg1 + ")";
	        case "curvardireita":
	            return "CURVADIREITA(" + arg1 + ", " + arg2 + ")";
	        case "curvaresquerda":
	            return "CURVARESQUERDA(" + arg1 + ", " + arg2 + ")";
	        default:
	            return tipo + "(" + arg1 + ", " + arg2 + ")";
	    }
	}

	public void setManual(boolean c) {
		this.manual = c;
		
	}
}