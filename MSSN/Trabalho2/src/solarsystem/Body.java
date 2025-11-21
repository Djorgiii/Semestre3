package solarsystem;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * A classe "Body" base.
 * Refatorizada com nomes de variáveis explícitos.
 */
public class Body extends Mover {
    
    protected int color;
    protected PApplet app; // Antes: p

    public Body(PApplet app, PVector position, PVector velocity, float mass, float radius, int color) {
        // Chama o construtor da Mover (que já usa os nomes novos)
        super(position, velocity, mass, radius);
        this.app = app;
        this.color = color;
    }

    public void display() {
        app.pushStyle();
        app.noStroke();
        app.fill(color);
        
        // Usa 'position' (herdado do Mover refatorizado) em vez de 'pos'
        app.ellipse(position.x, position.y, radius * 2, radius * 2);
        
        app.popStyle();
    }
    
    // Getter útil (caso precise de acessar a cor de fora)
    public int getColor() { 
        return color; 
    }
}