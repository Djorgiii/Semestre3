import java.util.function.Consumer;

public class Servidor extends Tarefa{
    private BufferCircular buffercircular;
    private RobotLegoEV3 asdrubal;
    private Gravador gravador;
    private final BaseDados bd;
    private Consumer<String> printCallback;
    private int contadorAleatorios = 0;
    private static final double VELOCIDADE_CM_POR_MS = 0.02;
    private static final int TEMPO_COMUNICACAO_MS = 100;
    
    
    public Servidor(BufferCircular buffercircular, RobotLegoEV3 asdrubal, BaseDados bd, Consumer<String> printCallback) {
    	super();
        this.buffercircular = buffercircular;
        this.asdrubal = asdrubal;
        this.bd = bd;
        this.printCallback = printCallback;
    }
    
    public void resetContadorAleatorios() {
        contadorAleatorios = 0;
    }
    
    public void setGravador(Gravador g) {
        this.gravador = g;
    }
    
    public BufferCircular getBufferCircular() {
        return buffercircular;
    }

    public void execucao() {
        while (true) {

            // PAUSA DO SERVIDOR
            if (bd != null && bd.isPausaServidor()) {
                if (printCallback != null) printCallback.accept("[Servidor] em pausa.");

                try {
                    bd.getPausaSem().acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    continue;
                }

                if (printCallback != null) printCallback.accept("[Servidor] retomado.");
                continue;
            }

            Movimento movimento = buffercircular.removerElemento();
            
         // 🔴 GRAVAÇÃO GLOBAL (mesmo robot)
            if (gravador != null && gravador.isAGravar()) {
                gravador.registar(movimento);
            }


            
            boolean isManual = movimento.isManual();
            int pos = buffercircular.getLastRemovedIndex();

            if (movimento != null) {

                // Impressão
                if (isManual) {
                    if (printCallback != null) 
                        printCallback.accept("Comando manual recebido: " + movimento.toString());
                } else {
                    printCallback.accept(pos + 1 + " - " + movimento.toString());
                }

                String tipo = movimento.getTipo().toUpperCase();
                int tempoExecucao = 0;

                java.util.concurrent.Semaphore ev3Sem = bd.getEv3Sem();

                switch (tipo) {

                    case "RETA":
                        tempoExecucao = (int)(Math.abs(movimento.getArg1()) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
                        try {
                            ev3Sem.acquire();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            continue;
                        }
                        try {
                            asdrubal.Reta(movimento.getArg1());
                        } finally {
                            ev3Sem.release();
                        }
                        if (!isManual) contadorAleatorios++;
                        break;

                    case "CURVARDIREITA":
                        double angDir = movimento.getArg2() * Math.PI / 180.0;
                        tempoExecucao = (int)((movimento.getArg1() * angDir) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;

                        try {
                            ev3Sem.acquire();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            continue;
                        }
                        try {
                            asdrubal.CurvarDireita(movimento.getArg1(), movimento.getArg2());
                        } finally {
                            ev3Sem.release();
                        }

                        if (!isManual) contadorAleatorios++;
                        break;

                    case "CURVARESQUERDA":
                        double angEsq = movimento.getArg2() * Math.PI / 180.0;
                        tempoExecucao = (int)((movimento.getArg1() * angEsq) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;

                        try {
                            ev3Sem.acquire();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            continue;
                        }
                        try {
                            asdrubal.CurvarEsquerda(movimento.getArg1(), movimento.getArg2());
                        } finally {
                            ev3Sem.release();
                        }

                        if (!isManual) contadorAleatorios++;
                        break;

                    case "PARAR":
                        tempoExecucao = TEMPO_COMUNICACAO_MS;

                        try {
                            ev3Sem.acquire();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            continue;
                        }
                        try {
                            asdrubal.Parar(false);
                        } finally {
                            ev3Sem.release();
                        }

                        if (!isManual) contadorAleatorios++;
                        break;
                }

                // Dormir pelo tempo estimado
                try {
                    Thread.sleep(tempoExecucao);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // PARAR NO FIM DE CADA MOVIMENTO (excepto PARAR)
                if (!tipo.equals("PARAR")) {
                    try {
                        ev3Sem.acquire();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        continue;
                    }
                    try {
                        asdrubal.Parar(false);
                    } finally {
                        ev3Sem.release();
                    }

                    try { Thread.sleep(TEMPO_COMUNICACAO_MS); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                }

                // CONTROLO DE LOTE ALEATÓRIO
                if (contadorAleatorios == bd.getSpinnerValue()) {
                    if (printCallback != null)
                        printCallback.accept("Sequência de " + bd.getSpinnerValue() + " movimentos aleatórios concluída.");
                    contadorAleatorios = 0;
                }
            }
        }
    }

    @Override
    public void run() {
        execucao();
    }
}
