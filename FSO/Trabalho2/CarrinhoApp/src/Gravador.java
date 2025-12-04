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

    private final List<Movimento> movimentos;   // lista de comandos
    private boolean emReproducao = false;       // flag de reprodução em curso

    public Gravador(Tarefa proxima) {
        super(proxima);
        movimentos = new ArrayList<>();
    }

    // ------------------ FLAGS DE CONTROLO ------------------

    public synchronized boolean isEmReproducao() {
        return emReproducao;
    }

    private synchronized void setEmReproducao(boolean valor) {
        emReproducao = valor;
    }

    // ------------------ REGISTAR ------------------

    public synchronized void registar(Movimento m) {
        if (m == null) return;

        // se está em reprodução, ignora
        if (emReproducao) {
            System.out.println("[Gravador] Ignorado comando durante reprodução: " + m.getTipo());
            return;
        }

        movimentos.add(m);
    }

    // ------------------ GUARDAR EM FICHEIRO ------------------

    public synchronized void guardarEmFicheiro(String nomeFicheiro) {
        if (emReproducao) {
            System.out.println("[Gravador] Não pode gravar enquanto reproduz.");
            return;
        }

        try (FileOutputStream out = new FileOutputStream(nomeFicheiro)) {
            for (Movimento m : movimentos) {
                if (m == null) continue;
                String linha;

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
            System.out.println("[Gravador] Gravado em " + nomeFicheiro);
        } catch (IOException e) {
            System.out.println("[Gravador] Erro ao gravar o ficheiro " + nomeFicheiro);
        }
    }

    // ------------------ LER FICHEIRO ------------------

    public synchronized void lerFicheiro(String nomeFicheiro) {
        if (emReproducao) {
            System.out.println("[Gravador] Não pode ler enquanto reproduz.");
            return;
        }

        movimentos.clear();
        File f = new File(nomeFicheiro);
        Scanner sc;

        try {
            sc = new Scanner(f);
        } catch (FileNotFoundException e) {
            System.out.println("[Gravador] Ficheiro não encontrado: " + nomeFicheiro);
            return;
        }

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

            if (m != null)
                movimentos.add(m);
        }
        sc.close();
        System.out.println("[Gravador] Ficheiro carregado. Nº movimentos = " + movimentos.size());
    }

    // ------------------ EXECUTAR ------------------

    public synchronized List<Movimento> getMovimentos() {
        // devolve uma cópia para não mexer diretamente na lista
        return new ArrayList<>(movimentos);
    }

    public void executarMovimentoNoRobot(Movimento m, RobotLegoEV3 robot) {
        if (robot == null || m == null) return;

        String tipo = m.getTipo().toUpperCase();
        int a1 = m.getArg1();
        int a2 = m.getArg2();

        System.out.println("[Gravador] Exec: " + tipo + " (" + a1 + ", " + a2 + ")");

        int tempoExecucao = 0;

        switch (tipo) {
            case "RETA":
                tempoExecucao = (int)(Math.abs(a1) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
                robot.Reta(a1);
                break;
            case "CURVARDIREITA":
                double angDirRad = a2 * Math.PI / 180.0;
                tempoExecucao = (int)((a1 * angDirRad) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
                robot.CurvarDireita(a1, a2);
                break;
            case "CURVARESQUERDA":
                double angEsqRad = a2 * Math.PI / 180.0;
                tempoExecucao = (int)((a1 * angEsqRad) / VELOCIDADE_CM_POR_MS) + TEMPO_COMUNICACAO_MS;
                robot.CurvarEsquerda(a1, a2);
                break;
            case "PARAR":
                robot.Parar(false);
                return;
            default:
                System.out.println("[Gravador] Tipo desconhecido: " + tipo);
                return;
        }

        try {
            Thread.sleep(tempoExecucao);
        } catch (InterruptedException e) {
            // ignorar
        }

        robot.Parar(false);
        try {
            Thread.sleep(TEMPO_COMUNICACAO_MS);
        } catch (InterruptedException e) {
            // ignorar
        }
    }

    // ---- Executa todos os movimentos ----
    public void executarTodosNoRobot(RobotLegoEV3 robot) {
        synchronized (this) {
            if (emReproducao) {
                System.out.println("[Gravador] Já está em reprodução!");
                return;
            }
            emReproducao = true;
            System.out.println("[DEBUG] Início de reprodução: emReproducao = true");

        }

        System.out.println("[Gravador] A executar " + movimentos.size() + " movimentos no robot...");

        for (Movimento m : getMovimentos()) {
            executarMovimentoNoRobot(m, robot);
        }

        System.out.println("[Gravador] Reprodução terminada.");

        synchronized (this) {
            emReproducao = false;
        }
    }

    @Override
    public void execucao() {
        // não usada diretamente neste caso
    }
}
