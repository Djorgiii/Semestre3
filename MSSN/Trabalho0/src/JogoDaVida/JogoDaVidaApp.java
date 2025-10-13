package JogoDaVida;

import processing.core.PApplet;
import setup.iProcessing;
import ca.CellullarAutomata;

public class JogoDaVidaApp implements iProcessing {
    private CellullarAutomata automata;

    @Override
    public void setup(PApplet parent) {
        int rows = 50;
        int cols = 50;
        int radius = 1;
        boolean moore = true;
        int numberOfStates = 2;
        automata = new CellullarAutomata(parent, rows, cols, radius, moore, numberOfStates);
        automata.setRandomStates();
    }

    @Override
    public void draw(PApplet parent, float dt) {
        parent.background(255);
        automata.display();
        // Aqui podes adicionar lógica para atualizar o autómato, se quiseres
    }

    @Override
    public void keyPressed(PApplet parent) {
        // Exemplo: reset ao pressionar 'r'
        if (parent.key == 'r' || parent.key == 'R') {
            automata.setRandomStates();
        }
    }

    @Override
    public void mousePressed(PApplet parent) {
        // Exemplo: altera o estado da célula clicada
        int x = parent.mouseX;
        int y = parent.mouseY;
        ca.Cell cell = automata.getCell(x, y);
        cell.setState((cell.getState() + 1) % automata.getNumberOfStates());
    }

    @Override
    public void mouseMoved(PApplet parent) {
        // Não faz nada por omissão
    }
}
