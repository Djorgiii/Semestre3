package flock;

import processing.core.PApplet;
import processing.core.PVector;

public class Particle extends Mover {

    private int color;
    private PApplet app; // Antes: p
    private float lifespan; // Tempo de vida

    public Particle(PApplet app, PVector position, PVector velocity, float mass, float radius, int color, float lifespan) {
        // Chama o construtor do Mover (que já usa os nomes novos)
        super(position, velocity, mass, radius); 
        this.app = app;
        this.color = color;
        this.lifespan = lifespan; // Ex: 255.0f
    }

    /**
     * Atualiza o movimento E o tempo de vida
     */
    @Override
    public void move(float secondsElapsed) { // Antes: dt
        super.move(secondsElapsed); // Chama o move() da classe Mover
        this.lifespan -= 0.5f; // "Envelhece" a partícula
    }

    /**
     * Verifica se a partícula já "morreu"
     */
    public boolean isDead() {
        return this.lifespan < 0.0f;
    }

    /**
     * Desenha a partícula (com fade out)
     */
    public void display() {
        app.pushStyle();
        
        // Usar o tempo de vida para fazer um "fade out"
        float alpha = PApplet.map(this.lifespan, 0, 255, 0, 255);
        app.fill(this.color, alpha); // Cor com transparência
        app.noStroke();
        
        // Usa 'position' (herdado do Mover refatorizado)
        app.ellipse(position.x, position.y, 2 * radius, 2 * radius);
        
        app.popStyle();
    }
}