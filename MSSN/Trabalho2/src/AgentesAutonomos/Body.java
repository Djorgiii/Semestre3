// Em physics/Body.java (ou no seu package SolarSystem)

package AgentesAutonomos; // (use o nome do seu package)
import processing.core.PVector;
import processing.core.PApplet;

/**
 * A classe "Body" base.
 * É um Mover que sabe desenhar-se a si próprio com uma cor.
 * Não sabe nada sobre rastos. Usado para o Sol.
 */
public class Body extends Mover {
    
    // "Protected" permite que as classes-filhas (Planet) vejam estas variáveis
    protected int color;
    protected PApplet p;

    public Body(PApplet p, PVector pos, PVector vel, float mass, float radius, int color) {
        super(pos, vel, mass, radius); // Construtor da Mover
        this.p = p;
        this.color = color;
    }

    /**
     * Método display() simples. Sem 'if'.
     */
    public void display() {
        p.pushStyle();
        p.noStroke();
        p.fill(color);
        p.ellipse(pos.x, pos.y, 2 * radius, 2 * radius);
        p.popStyle();
    }
    
    // Nota: Já não precisa de um método move() aqui.
    // Ele herda o move() básico da classe Mover.
}