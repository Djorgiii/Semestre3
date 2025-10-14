package JogoDaVida;

import processing.core.PApplet;
import setup.iProcessing;

public class JogoDaVidaApp implements iProcessing {
    private CellularAutomata automata;

    private int cellSize = 12;     // pixels por célula
    private boolean running = false;

    // 👉 NÃO precisa de main() aqui. O launcher da disciplina arranca isto.
    // 👉 Também não precisa de settings() sem parent; o tamanho define-se no launcher.

    @Override
    public void setup(PApplet parent) {
        // a janela já deve estar criada pelo teu "processingMain"
        parent.frameRate(30);

        int rows = parent.height / cellSize;
        int cols = parent.width  / cellSize;

        automata = new CellularAutomata(rows, cols);
        randomFill(parent, 0.25f); // começa com 25% vivas
    }

    @Override
    public void draw(PApplet parent, float dt) {
        parent.background(20);

        if (running) automata.update();

        parent.noStroke();
        for (int r = 0; r < automata.getRows(); r++) {
            for (int c = 0; c < automata.getCols(); c++) {
                if (automata.isAlive(r, c)) parent.fill(60, 200, 120); // viva
                else                         parent.fill(60,  40,  70); // morta
                parent.rect(c * cellSize, r * cellSize, cellSize - 1, cellSize - 1);
            }
        }

        parent.fill(230);
        parent.textSize(14);
        parent.text((running ? "RUN" : "PAUSE")
                        + "  |  clique/arraste para alternar  |  Espaço: Run/Pause, N: Step, R: Random, L: Limpar",
                10, 18);
    }

    @Override
    public void keyPressed(PApplet parent) {
        switch (parent.key) {
            case ' ':
                running = !running; break;            // run/pause
            case 'n': case 'N':
                automata.update(); break;             // step
            case 'r': case 'R':
                randomFill(parent, 0.25f); break;     // random
            case 'l': case 'L':
                clearAll(); break;                    // limpar
        }
    }

    @Override
    public void mousePressed(PApplet parent) { paintUnderMouse(parent); }

    @Override
    public void mouseMoved(PApplet parent) { /* opcional */ }

    private void paintUnderMouse(PApplet parent) {
        int c = parent.mouseX / cellSize;
        int r = parent.mouseY / cellSize;
        if (r >= 0 && r < automata.getRows() && c >= 0 && c < automata.getCols()) {
            automata.setAlive(r, c, !automata.isAlive(r, c));
        }
    }

    // ---------- utilitários ----------
    private void randomFill(PApplet parent, float p) {
        for (int r = 0; r < automata.getRows(); r++)
            for (int c = 0; c < automata.getCols(); c++)
                automata.setAlive(r, c, parent.random(1) < p);
    }

    private void clearAll() {
        for (int r = 0; r < automata.getRows(); r++)
            for (int c = 0; c < automata.getCols(); c++)
                automata.setAlive(r, c, false);
    }
}
