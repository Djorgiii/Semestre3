package agentesautonomos; // O seu package

import processing.core.PVector;
import processing.core.PApplet;

/**
 * A classe "Body" base.
 * Refatorizada com nomes de variáveis explícitos.
 */
public class Body extends Mover {
    
    protected int color;
    protected PApplet app; // Antes: 'p' (agora 'app' de Application)

    public Body(PApplet app, PVector position, PVector velocity, float mass, float radius, int color) {
        super(position, velocity, mass, radius); // Passa os novos nomes para o Mover
        this.app = app;
        this.color = color;
    }

    public void display() {
        app.pushStyle();
        app.noStroke();
        app.fill(color);
        
        // Usa 'position' (que deve estar definida na classe Mover)
        app.ellipse(position.x, position.y, 2 * radius, 2 * radius);
        
        app.popStyle();
    }
}