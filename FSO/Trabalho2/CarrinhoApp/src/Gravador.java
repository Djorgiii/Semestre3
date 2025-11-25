import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Gravador {

    private static final double VELOCIDADE_CM_POR_MS = 0.02; // 20 cm/s
    private static final int TEMPO_COMUNICACAO_MS = 100;

    private List<Movimento> movimentos;   // lista que guarda os comandos

    public Gravador() {
        movimentos = new ArrayList<>();
    }

    // GUI chama isto sempre que carregas num botão (FRENTE, TRÁS, etc.)
    public void registar(Movimento m) {
        if (m != null) {
            movimentos.add(m);
        }
    }

    // Grava a lista num ficheiro de texto
    public void guardarEmFicheiro(String nomeFicheiro) {
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

    // Lê um ficheiro e recria a lista de movimentos
    public void lerFicheiro(String nomeFicheiro) {

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

    public List<Movimento> getMovimentos() {
        return movimentos;
    }

    // -------------------------------------------------------
    //  EXECUÇÃO NO ROBOT (robot vem de fora, criado na GUI)
    // -------------------------------------------------------

    public void executarMovimentoNoRobot(Movimento m, RobotLegoEV3 robot) {
        if (robot == null || m == null) {
            return;
        }

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
                return; // não faz pausa extra
            default:
                System.out.println("[Gravador] Tipo desconhecido: " + tipo);
                return;
        }

        // Esperar o tempo real de execução
        try {
            Thread.sleep(tempoExecucao);
        } catch (InterruptedException e) {
            // ignorar
        }

        // Parar no final de cada movimento "ativo"
        robot.Parar(false);

        try {
            Thread.sleep(TEMPO_COMUNICACAO_MS);
        } catch (InterruptedException e) {
            // ignorar
        }
    }

    // Executa TODOS os movimentos gravados no robot
    public void executarTodosNoRobot(RobotLegoEV3 robot) {
        if (robot == null) return;

        System.out.println("[Gravador] A executar " + movimentos.size() + " movimentos no robot...");
        for (Movimento m : movimentos) {
            executarMovimentoNoRobot(m, robot);
        }
        System.out.println("[Gravador] Reprodução terminada.");
    }
}
