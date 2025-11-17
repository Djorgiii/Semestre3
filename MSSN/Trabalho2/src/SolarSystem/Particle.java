package SolarSystem;

import processing.core.PApplet;
import processing.core.PVector;

public class Particle extends Mover {

    private int color;
    private PApplet p;
    private float lifespan; // Tempo de vida

    public Particle(PApplet p, PVector pos, PVector vel, float mass, float radius, int color, float lifespan) {
        super(pos, vel, mass, radius); // Chama o construtor da Mover
        this.p = p;
        this.color = color;
        this.lifespan = lifespan; // Ex: 255.0f
    }

    /**
     * Atualiza o movimento E o tempo de vida
     */
    @Override
    public void move(float dt) {
        super.move(dt); // Chama o move() da classe Mover
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
        p.pushStyle();
        // Usar o tempo de vida para fazer um "fade out"
        float alpha = PApplet.map(this.lifespan, 0, 255, 0, 255);
        p.fill(this.color, alpha); // Cor com transparência
        p.noStroke();
        p.ellipse(pos.x, pos.y, 2 * radius, 2 * radius);
        p.popStyle();
    }
}