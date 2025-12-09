import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.io.File;


public class Gravador extends Tarefa {

    private static final double VELOCIDADE_CM_POR_MS = 0.02;
    private static final int TEMPO_COMUNICACAO_MS = 100;

    private final BufferGravacao buffer = new BufferGravacao();
    private volatile boolean emReproducao = false;
    private RobotLegoEV3 robotLigado;

    public Gravador() {
        start();
    }

    // ------------------------------------------------------
    // GETTERS / SETTERS
    // ------------------------------------------------------

    public synchronized boolean isEmReproducao() {
        return emReproducao;
    }

    public synchronized void limpar() {
        buffer.clear();
    }

    private synchronized void setEmReproducao(boolean valor) {
        emReproducao = valor;
    }

    public synchronized void setRobot(RobotLegoEV3 robot) {
        this.robotLigado = robot;
    }

    // ------------------------------------------------------
    // REGISTAR
    // ------------------------------------------------------

    public void registar(Movimento m) {
        if (m == null) return;
        if (emReproducao) return;
        buffer.inserirElemento(m);
    }
    
    public synchronized void lerFicheiro(String nomeFicheiro) {
        if (emReproducao) {
            System.out.println("[Gravador] Não pode ler enquanto reproduz!");
            return;
        }

        buffer.clear(); // limpar buffer antes de carregar

        try (Scanner sc = new Scanner(new File(nomeFicheiro))) {
            while (sc.hasNextLine()) {

                String linha = sc.nextLine().trim();
                if (linha.isEmpty() || linha.startsWith("#"))
                    continue;

                String[] p = linha.split("\\s+|;");
                String cmd = p[0].toUpperCase();
                Movimento m = null;

                try {
                    switch (cmd) {
                        case "RETA":
                            m = new Movimento("RETA", Integer.parseInt(p[1]), 0);
                            break;

                        case "CURVADIREITA":
                            m = new Movimento("CURVARDIREITA",
                                    Integer.parseInt(p[1]),
                                    Integer.parseInt(p[2]));
                            break;

                        case "CURVARESQUERDA":
                            m = new Movimento("CURVARESQUERDA",
                                    Integer.parseInt(p[1]),
                                    Integer.parseInt(p[2]));
                            break;

                        case "PARAR":
                            m = new Movimento("PARAR", false);
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("[Gravador] Linha inválida: " + linha);
                }

                if (m != null) {
                    buffer.inserirElemento(m);
                }
            }

            System.out.println("[Gravador] Ficheiro carregado!");
        }
        catch (FileNotFoundException e) {
            System.out.println("[Gravador] Ficheiro não encontrado: " + nomeFicheiro);
        }
    }

    

    // ------------------------------------------------------
    // GUARDAR EM FICHEIRO
    // ------------------------------------------------------
    
    public synchronized void guardarEmFicheiro(String nomeFicheiro) {

        if (emReproducao) {
            System.out.println("[Gravador] Não pode gravar enquanto reproduz!");
            return;
        }

        try (FileOutputStream out = new FileOutputStream(nomeFicheiro)) {

            // Copiar movimentos atuais para um array temporário
            int n = buffer.getCount();
            Movimento[] lista = new Movimento[n];

            for (int i = 0; i < n; i++) {
                lista[i] = buffer.removerElemento();
                buffer.inserirElemento(lista[i]); // voltar a inserir para não perder a gravação
            }

            // Escrever no ficheiro
            for (Movimento m : lista) {

                if (m == null) continue;
                String linha = "";

                switch (m.getTipo().toUpperCase()) {
                    case "RETA":
                        linha = "RETA " + m.getArg1();
                        break;

                    case "CURVARDIREITA":
                        linha = "CURVADIREITA " + m.getArg1() + " " + m.getArg2();
                        break;

                    case "CURVARESQUERDA":
                        linha = "CURVARESQUERDA " + m.getArg1() + " " + m.getArg2();
                        break;

                    case "PARAR":
                        linha = "PARAR";
                        break;

                    default:
                        linha = "# desconhecido";
                }

                linha += "\n";
                out.write(linha.getBytes());
            }

            System.out.println("[Gravador] Ficheiro \"" + nomeFicheiro + "\" gravado com sucesso.");

        } catch (IOException e) {
            System.out.println("[Gravador] Erro ao gravar ficheiro: " + e.getMessage());
        }
    }

    // ------------------------------------------------------
    // EXECUTAR MOVIMENTO
    // ------------------------------------------------------

    private void executarMovimentoNoRobot(Movimento m) {

        if (robotLigado == null || m == null) return;

        String tipo = m.getTipo().toUpperCase();
        int a1 = m.getArg1();
        int a2 = m.getArg2();
        int tempoExecucao = 0;

        switch (tipo) {
            case "RETA":
                tempoExecucao = (int)(Math.abs(a1) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
                robotLigado.Reta(a1);
                break;

            case "CURVARDIREITA":
                tempoExecucao = (int)((a1 * (a2 * Math.PI / 180)) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
                robotLigado.CurvarDireita(a1, a2);
                break;

            case "CURVARESQUERDA":
                tempoExecucao = (int)((a1 * (a2 * Math.PI / 180)) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
                robotLigado.CurvarEsquerda(a1, a2);
                break;

            case "PARAR":
                robotLigado.Parar(false);
                return;

            default:
                return;
        }

        try { Thread.sleep(tempoExecucao); } catch (InterruptedException ignored) {}
        robotLigado.Parar(false);
        try { Thread.sleep(TEMPO_COMUNICACAO_MS); } catch (InterruptedException ignored) {}
    }

    // ------------------------------------------------------
    // CICLO DA TAREFA
    // ------------------------------------------------------

    @Override
    public void execucao() {

        while (true) {

            if (!emReproducao || robotLigado == null) {
                bloquear();
                continue;
            }

            // Executar tudo o que estiver no buffer
            while (!buffer.isVazio()) {
                Movimento m = buffer.removerElemento();
                executarMovimentoNoRobot(m);
            }

            setEmReproducao(false);
        }
    }

    // ------------------------------------------------------
    // INICIAR REPRODUÇÃO
    // ------------------------------------------------------

    public void iniciarReproducao(RobotLegoEV3 robot) {
        if (emReproducao) return;

        setRobot(robot);
        setEmReproducao(true);
        desbloquear();
    }
}
