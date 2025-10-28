
public class Servidor extends Tarefa {
    private BufferCircular buffercircular;
    private BaseDados bd;
    private RobotLegoEV3 asdrubal;
    private int contadorAleatorios = 0;
    private int numeroComando = 1; // <<< numeração global dos comandos
    private static final int TOTAL_ALEATORIOS = 5;

    public Servidor(BufferCircular buffercircular, RobotLegoEV3 asdrubal, BaseDados bd) {
        super(null);
        this.buffercircular = buffercircular;
        this.asdrubal = asdrubal;
        this.bd = bd;
    }

    public void Reta(int distancia) { 
    	buffercircular.inserirElemento(new Comando("RETA", distancia, 0)); 
    	}
    public void CurvarDireita(int angulo, int raio) { 
    	buffercircular.inserirElemento(new Comando("CURVARDIREITA", angulo, raio)); 
    	}
    public void CurvarEsquerda(int angulo, int raio) { 
    	buffercircular.inserirElemento(new Comando("CURVARESQUERDA", angulo, raio)); 
    	}
    public void Parar(boolean b) { 
    	buffercircular.inserirElemento(new Comando("PARAR", b)); }
    
    public void resetContadorAleatorios() {
    	contadorAleatorios = 0; 
    	}
    
	public BufferCircular getBuffercircular() {
		return buffercircular;
	}
	
	public void iniciarLoteAleatorio() {
	    contadorAleatorios = 0;
	    numeroComando = 1;       // reinicia a numeração no início do lote
	    bd.myPrint("Iniciando sequência de 5 movimentos aleatórios + PARAR(true)...");
	}
	
    @Override
    public void execucao() {
        final double velocidade = 0.02; // cm/ms (20 cm/s)
        final int tempoComunicacao = 100; // ms

        Comando comando = buffercircular.removerElemento();
        if (comando == null) return;

        String tipo = comando.getTipo().toUpperCase();

        try {
            double tempoExecucao = 0;

            switch (tipo) {
                case "RETA": {
                    int distanciaCm = comando.getArg1();
                    asdrubal.Reta(distanciaCm);
                    tempoExecucao = (distanciaCm / velocidade) + tempoComunicacao;
                    dormir((long) Math.round(tempoExecucao));
                    asdrubal.Parar(false);
                    contadorAleatorios++; 
                    break;
                }
                case "CURVARDIREITA": {
                    int anguloGraus = comando.getArg1();
                    int raioCm      = comando.getArg2();
                    double anguloRad = Math.toRadians(anguloGraus);
                    asdrubal.CurvarDireita(raioCm, anguloGraus);
                    tempoExecucao = (raioCm * anguloRad / velocidade) + tempoComunicacao;
                    dormir((long) Math.round(tempoExecucao));
                    asdrubal.Parar(false);
                    contadorAleatorios++; 
                    break;
                }
                case "CURVARESQUERDA": {
                    int anguloGraus = comando.getArg1();
                    int raioCm      = comando.getArg2();
                    double anguloRad = Math.toRadians(anguloGraus);
                    asdrubal.CurvarEsquerda(raioCm, anguloGraus);
                    tempoExecucao = (raioCm * anguloRad / velocidade) + tempoComunicacao;
                    dormir((long) Math.round(tempoExecucao));
                    asdrubal.Parar(false);
                    contadorAleatorios++; 
                    break;
                }
                case "PARAR": {
                    asdrubal.Parar(comando.isB());
                    tempoExecucao = tempoComunicacao;
                    dormir((long) Math.round(tempoExecucao));
                    contadorAleatorios++;
                    break;
                }
                default:
                	bd.myPrint("Comando desconhecido recebido no servidor: " + tipo);
            }

            // Espera o tempo físico estimado
            //Thread.sleep((long)Math.max(0, Math.round(tempoExecucao)));

            // Mostra o número do comando
            bd.myPrint("Comando " + numeroComando + ": " + tipo +
						 " executado em ~" + Math.round(tempoExecucao) + " ms");
            numeroComando++; // incrementa após cada execução

        } catch (Exception e) {
        	bd.myPrint("Erro ao enviar comando ao robô: " + e.getMessage());
        }

        // --- Depois de 5 comandos aleatórios ---
        if (contadorAleatorios == TOTAL_ALEATORIOS) {
        	bd.myPrint("Sequência de 5 movimentos aleatórios concluída. Enviando PARAR(true)...");
            try { 
            	asdrubal.Parar(true); 
            	} catch (Exception ignore) {}
            buffercircular.inserirElemento(new Comando("PARAR", true));

            contadorAleatorios = 0;
        }
        
        
    }

}
