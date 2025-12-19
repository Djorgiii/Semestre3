import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Gravador extends Tarefa {

    private static final double VELOCIDADE_CM_POR_MS = 0.02;
    private static final int TEMPO_COMUNICACAO_MS = 100;

    // Buffer próprio para gravação/reprodução
    private final BufferGravacao buffer = new BufferGravacao();

    // Exclusão mútua REAL entre gravar / reproduzir
    private final Semaphore exclusao = new Semaphore(1, true);

    private volatile boolean emReproducao = false;
    private RobotLegoEV3 robotLigado;
    private volatile boolean aGravar = false;


    

    public Gravador() {
        start();
    }

    // --------------------------------------------------
    // ESTADO
    // --------------------------------------------------

    public boolean isEmReproducao() {
        return emReproducao;
    }
    
    public void iniciarGravacao() {
        aGravar = true;
        limpar();
    }

    public void pararGravacao() {
        aGravar = false;
    }

    public boolean isAGravar() {
        return aGravar;
    }

    
    public void limpar() {
        try {
            exclusao.acquire();
            buffer.clear();
        } catch (InterruptedException ignored) {
        } finally {
            exclusao.release();
        }
    }

    // --------------------------------------------------
    // REGISTAR COMANDOS (GRAVAÇÃO)
    // --------------------------------------------------

    public void registar(Movimento m) {
        if (m == null) return;
        
        if (!aGravar) return;

        // Se não conseguir adquirir, está a reproduzir
        if (!exclusao.tryAcquire()) {
            System.out.println("[Gravador] Gravação bloqueada (em reprodução)");
            return;
        }

        try {
            buffer.inserirElemento(m);
        } finally {
            exclusao.release();
        }
    }
    
    public void reproduzirParaBuffer(BufferCircular bufferCentral) {

        try {
            exclusao.acquire();     // 🔒 bloqueia gravação
            emReproducao = true;

            while (!buffer.isVazio()) {
                bufferCentral.inserirElemento(buffer.removerElemento());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            emReproducao = false;
            exclusao.release();     // 🔓 ACABA AQUI
        }
    }





    // --------------------------------------------------
    // LER FICHEIRO
    // --------------------------------------------------

    public void lerFicheiro(String nomeFicheiro) {

        if (!exclusao.tryAcquire()) {
            System.out.println("[Gravador] Não pode ler durante reprodução");
            return;
        }

        try {
            buffer.clear();

            Scanner sc = new Scanner(new File(nomeFicheiro));

            while (sc.hasNextLine()) {

                String linha = sc.nextLine().trim();
                if (linha.isEmpty() || linha.startsWith("#")) continue;

                String[] p = linha.split("\\s+|;");
                String cmd = p[0].toUpperCase();
                Movimento m = null;

                try {
                    if (cmd.equals("RETA")) {
                        m = new Movimento("RETA", Integer.parseInt(p[1]), 0);
                    }
                    else if (cmd.equals("CURVADIREITA")) {
                        m = new Movimento("CURVARDIREITA",
                                Integer.parseInt(p[1]),
                                Integer.parseInt(p[2]));
                    }
                    else if (cmd.equals("CURVARESQUERDA")) {
                        m = new Movimento("CURVARESQUERDA",
                                Integer.parseInt(p[1]),
                                Integer.parseInt(p[2]));
                    }
                    else if (cmd.equals("PARAR")) {
                        m = new Movimento("PARAR", false);
                    }
                } catch (Exception e) {
                    System.out.println("[Gravador] Linha inválida: " + linha);
                }

                if (m != null) buffer.inserirElemento(m);
            }

            sc.close();
            System.out.println("[Gravador] Ficheiro carregado");

        } catch (FileNotFoundException e) {
            System.out.println("[Gravador] Ficheiro não encontrado");
        } finally {
            exclusao.release();
        }
    }

    // --------------------------------------------------
    // GUARDAR EM FICHEIRO
    // --------------------------------------------------

    public void guardarEmFicheiro(String nomeFicheiro) {

        if (!exclusao.tryAcquire()) {
            System.out.println("[Gravador] Não pode guardar durante reprodução");
            return;
        }

        try (FileOutputStream out = new FileOutputStream(nomeFicheiro)) {

            int n = buffer.getCount();
            Movimento[] lista = new Movimento[n];

            for (int i = 0; i < n; i++) {
                lista[i] = buffer.removerElemento();
                buffer.inserirElemento(lista[i]);
            }

            for (Movimento m : lista) {

                String linha = "";

                if (m.getTipo().equalsIgnoreCase("RETA")) {
                    linha = "RETA " + m.getArg1();
                }
                else if (m.getTipo().equalsIgnoreCase("CURVARDIREITA")) {
                    linha = "CURVADIREITA " + m.getArg1() + " " + m.getArg2();
                }
                else if (m.getTipo().equalsIgnoreCase("CURVARESQUERDA")) {
                    linha = "CURVARESQUERDA " + m.getArg1() + " " + m.getArg2();
                }
                else if (m.getTipo().equalsIgnoreCase("PARAR")) {
                    linha = "PARAR";
                }

                out.write((linha + "\n").getBytes());
            }

            System.out.println("[Gravador] Ficheiro gravado");

        } catch (IOException e) {
            System.out.println("[Gravador] Erro ao gravar ficheiro");
        } finally {
            exclusao.release();
        }
    }

    // --------------------------------------------------
    // EXECUTAR MOVIMENTO NO ROBOT
    // --------------------------------------------------

    private void executarMovimento(Movimento m) {

        if (robotLigado == null || m == null) return;

        int tempo = 0;

        if (m.getTipo().equalsIgnoreCase("RETA")) {
            tempo = (int)(Math.abs(m.getArg1()) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
            robotLigado.Reta(m.getArg1());
        }
        else if (m.getTipo().equalsIgnoreCase("CURVARDIREITA")) {
            tempo = (int)((m.getArg1() * (m.getArg2() * Math.PI / 180)) / VELOCIDADE_CM_POR_MS)
                    + TEMPO_COMUNICACAO_MS;
            robotLigado.CurvarDireita(m.getArg1(), m.getArg2());
        }
        else if (m.getTipo().equalsIgnoreCase("CURVARESQUERDA")) {
            tempo = (int)((m.getArg1() * (m.getArg2() * Math.PI / 180)) / VELOCIDADE_CM_POR_MS)
                    + TEMPO_COMUNICACAO_MS;
            robotLigado.CurvarEsquerda(m.getArg1(), m.getArg2());
        }
        else if (m.getTipo().equalsIgnoreCase("PARAR")) {
            robotLigado.Parar(false);
            return;
        }

        try { Thread.sleep(tempo); } catch (InterruptedException ignored) {}
        robotLigado.Parar(false);
        try { Thread.sleep(TEMPO_COMUNICACAO_MS); } catch (InterruptedException ignored) {}
    }

    // --------------------------------------------------
    // CICLO DA TAREFA
    // --------------------------------------------------

    @Override
    public void execucao() {

        while (true) {

            if (!emReproducao || robotLigado == null) {
                bloquear();
                continue;
            }

            while (!buffer.isVazio()) {
                executarMovimento(buffer.removerElemento());
            }

            emReproducao = false;
            exclusao.release();
        }
    }


    // --------------------------------------------------
    // INICIAR REPRODUÇÃO
    // --------------------------------------------------

    public void iniciarReproducao(RobotLegoEV3 robot) {

        try {
            exclusao.acquire();      // 🔒 bloqueia gravar
            robotLigado = robot;
            emReproducao = true;
            desbloquear();
        } catch (InterruptedException ignored) {}
    }
}
