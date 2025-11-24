import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Gravador {

    private final BufferCircular buffer;
    private final BaseDados bd;

    // Construtor: recebe o buffer que já tens e a BaseDados (para o semáforo produtorMux)
    public Gravador(BufferCircular buffer, BaseDados bd) {
        this.buffer = buffer;
        this.bd = bd;
    }

    /**
     * Lê um ficheiro de texto com comandos e coloca-os no BufferCircular.
     * Cada linha do ficheiro deve ter um comando, por exemplo:
     *
     *  RETA 30
     *  CURVADIREITA 20 90
     *  CURVARESQUERDA 20 90
     *  PARAR
     *
     * Também aceito ; como separador:
     *  RETA;30
     *  CURVADIREITA;20;90
     */
    public void lerFicheiro(String nomeFicheiro) {
        File f = new File(nomeFicheiro);
        Scanner sc;

        try {
            sc = new Scanner(f);
        } catch (FileNotFoundException e) {
            System.out.println("[Gravador] Ficheiro não encontrado: " + nomeFicheiro);
            return;
        }

        // Para não chocar com aleatórios / manuais, uso o mesmo produtorMux
        java.util.concurrent.Semaphore mux = bd.getProdutorMux();
        mux.acquireUninterruptibly();
        try {
            while (sc.hasNextLine()) {
                String linha = sc.nextLine().trim();
                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue; // linhas vazias ou comentários
                }

                // separa por espaços OU por ';'
                String[] partes = linha.split("\\s+|;");
                if (partes.length == 0) continue;

                String cmd = partes[0].toUpperCase();
                Movimento m = null;

                try {
                    if ("RETA".equals(cmd) && partes.length >= 2) {
                        int dist = Integer.parseInt(partes[1]);
                        m = new Movimento("RETA", dist, 0);

                    } else if ("CURVADIREITA".equals(cmd) && partes.length >= 3) {
                        int raio = Integer.parseInt(partes[1]);
                        int angulo = Integer.parseInt(partes[2]);
                        m = new Movimento("CURVARDIREITA", raio, angulo);

                    } else if ("CURVARESQUERDA".equals(cmd) && partes.length >= 3) {
                        int raio = Integer.parseInt(partes[1]);
                        int angulo = Integer.parseInt(partes[2]);
                        m = new Movimento("CURVARESQUERDA", raio, angulo);

                    } else if ("PARAR".equals(cmd)) {
                        // false = stop “normal” (como no botão Parar)
                        m = new Movimento("PARAR", false);
                    } else {
                        System.out.println("[Gravador] Linha ignorada: " + linha);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[Gravador] Erro a converter números na linha: " + linha);
                    m = null;
                }

                if (m != null) {
                    // Comandos de ficheiro não são “manuais”
                    m.setManual(false);
                    buffer.inserirElemento(m);
                }
            }
        } finally {
            sc.close();
            mux.release();
        }
    }
}
