import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Gravador extends Tarefa {

    private static final double VELOCIDADE_CM_POR_MS = 0.02; // 20 cm/s
    private static final int TEMPO_COMUNICACAO_MS = 100;

    private final List<Movimento> movimentos = new ArrayList<>();
    private volatile boolean emReproducao = false;
    private RobotLegoEV3 robotLigado;

    // Construtor: inicia a tarefa (thread)
    public Gravador(Tarefa proxima) {
        super(proxima);
        start();
    }

    // ------------------------------------------------------
    //               GETTERS / SETTERS
    // ------------------------------------------------------

    public synchronized boolean isEmReproducao() {
        return emReproducao;
    }

    private synchronized void setEmReproducao(boolean valor) {
        emReproducao = valor;
    }

    public synchronized void setRobot(RobotLegoEV3 robot) {
        this.robotLigado = robot;
    }

    // ------------------------------------------------------
    //                   REGISTAR COMANDO
    // ------------------------------------------------------

    public synchronized void registar(Movimento m) {
        if (m == null) return;
        if (emReproducao) {
            System.out.println("[Gravador] Ignorado comando durante reprodução: " + m.getTipo());
            return;
        }
        movimentos.add(m);
        System.out.println("[Gravador] Comando registado: " + m.getTipo());
    }

    // ------------------------------------------------------
    //                GUARDAR EM FICHEIRO
    // ------------------------------------------------------

    public synchronized void guardarEmFicheiro(String nomeFicheiro) {
        if (emReproducao) {
            System.out.println("[Gravador] Não pode gravar enquanto reproduz!");
            return;
        }

        try (FileOutputStream out = new FileOutputStream(nomeFicheiro)) {

            for (Movimento m : movimentos) {
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
                        break;
                }

                linha += "\n";
                out.write(linha.getBytes());
            }

            System.out.println("[Gravador] Ficheiro \"" + nomeFicheiro + "\" gravado com sucesso.");
        } catch (IOException e) {
            System.out.println("[Gravador] Erro ao gravar o ficheiro: " + e.getMessage());
        }
    }

    // ------------------------------------------------------
    //                LER FICHEIRO
    // ------------------------------------------------------

    public synchronized void lerFicheiro(String nomeFicheiro) {
        if (emReproducao) {
            System.out.println("[Gravador] Não pode ler enquanto reproduz!");
            return;
        }

        movimentos.clear();

        try (Scanner sc = new Scanner(new File(nomeFicheiro))) {
            while (sc.hasNextLine()) {
                String linha = sc.nextLine().trim();
                if (linha.isEmpty() || linha.startsWith("#")) continue;

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

                if (m != null) movimentos.add(m);
            }

            System.out.println("[Gravador] Ficheiro carregado. Nº movimentos = " + movimentos.size());
        } catch (FileNotFoundException e) {
            System.out.println("[Gravador] Ficheiro não encontrado: " + nomeFicheiro);
        }
    }

    // ------------------------------------------------------
    //                 EXECUTAR MOVIMENTO
    // ------------------------------------------------------

    private void executarMovimentoNoRobot(Movimento m) {
        if (robotLigado == null || m == null) return;

        String tipo = m.getTipo().toUpperCase();
        int a1 = m.getArg1();
        int a2 = m.getArg2();
        int tempoExecucao = 0;

        System.out.println("[Gravador] Exec: " + tipo + " (" + a1 + ", " + a2 + ")");

        switch (tipo) {
            case "RETA":
                tempoExecucao = (int)(Math.abs(a1) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
                robotLigado.Reta(a1);
                break;
            case "CURVARDIREITA":
                double angDirRad = a2 * Math.PI / 180.0;
                tempoExecucao = (int)((a1 * angDirRad) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
                robotLigado.CurvarDireita(a1, a2);
                break;
            case "CURVARESQUERDA":
                double angEsqRad = a2 * Math.PI / 180.0;
                tempoExecucao = (int)((a1 * angEsqRad) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
                robotLigado.CurvarEsquerda(a1, a2);
                break;
            case "PARAR":
                robotLigado.Parar(false);
                return;
            default:
                System.out.println("[Gravador] Tipo desconhecido: " + tipo);
                return;
        }

        try {
            Thread.sleep(tempoExecucao);
        } catch (InterruptedException e) {}
        robotLigado.Parar(false);
        try {
            Thread.sleep(TEMPO_COMUNICACAO_MS);
        } catch (InterruptedException e) {}
    }

    // ------------------------------------------------------
    //                 CICLO PRINCIPAL (TAREFA)
    // ------------------------------------------------------

    @Override
    public void execucao() {
        while (true) {

            if (!emReproducao || robotLigado == null) {
                bloquear(); // dorme até receber desbloquear()
                continue;
            }

            System.out.println("[Gravador] A executar " + movimentos.size() + " movimentos...");
            for (Movimento m : new ArrayList<>(movimentos)) {
                executarMovimentoNoRobot(m);
            }

            System.out.println("[Gravador] Reprodução terminada.");
            setEmReproducao(false);
        }
    }

    // ------------------------------------------------------
    //                 MÉTODO CHAMADO PELA GUI
    // ------------------------------------------------------

    public void iniciarReproducao(RobotLegoEV3 robot) {
        if (emReproducao) {
            System.out.println("[Gravador] Já está em reprodução!");
            return;
        }

        setRobot(robot);
        setEmReproducao(true);
        desbloquear(); // acorda o loop principal
    }
}