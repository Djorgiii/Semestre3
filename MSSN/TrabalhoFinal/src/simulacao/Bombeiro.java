package simulacao;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;

public class Bombeiro {
    public PVector pos, vel, acc;
    public float maxSpeed, maxForce;
    public float fitness = 0; // Para Seleção Natural

    public Bombeiro(float x, float y, float speed) {
        pos = new PVector(x, y);
        vel = PVector.random2D();
        acc = new PVector(0, 0);
        this.maxSpeed = speed; // DNA: Velocidade
        this.maxForce = 0.2f;
    }

    public void applyForce(PVector force) {
        acc.add(force);
    }

    public void comportamentos(ArrayList<Bombeiro> outros, Floresta f) {
        // Boids: Separação
        PVector sep = separar(outros);
        // Comportamento Individual: Seek Fogo
        PVector seek = buscarFogo(f);
        
        sep.mult(1.5f);
        seek.mult(1.0f);
        
        applyForce(sep);
        applyForce(seek);
    }

    private PVector separar(ArrayList<Bombeiro> outros) {
        float distDesejada = 25f;
        PVector steer = new PVector(0, 0);
        int count = 0;
        for (Bombeiro outro : outros) {
            float d = PVector.dist(pos, outro.pos);
            if ((d > 0) && (d < distDesejada)) {
                PVector diff = PVector.sub(pos, outro.pos);
                diff.normalize();
                diff.div(d);
                steer.add(diff);
                count++;
            }
        }
        if (count > 0) steer.div((float)count);
        if (steer.mag() > 0) {
            steer.setMag(maxSpeed);
            steer.sub(vel);
            steer.limit(maxForce);
        }
        return steer;
    }

    private PVector buscarFogo(Floresta f) {
        // Busca a célula de fogo mais próxima
        PVector alvo = null;
        float minDist = 100000;
        for (int i = 0; i < f.cols; i++) {
            for (int j = 0; j < f.rows; j++) {
                if (f.grid[i][j] == 2) {
                    PVector fogoPos = new PVector(i * f.cellSize, j * f.cellSize);
                    float d = PVector.dist(pos, fogoPos);
                    if (d < minDist) {
                        minDist = d;
                        alvo = fogoPos;
                    }
                    // Lógica de apagar fogo (Fitness)
                    if (d < f.cellSize * 2.0f) {
                        apagarArea(f,i,j); // Apaga o fogo
                        fitness += 20;     // Ganha pontos
                    }
                }
            }
        }
        if (alvo != null) {
            PVector desired = PVector.sub(alvo, pos);
            desired.setMag(maxSpeed);
            PVector steer = PVector.sub(desired, vel);
            steer.limit(maxForce);
            return steer;
        }
        return new PVector(0, 0);
    }
    
    public void conter(PApplet p) {
        float margem = 10; // Distância da borda onde ele começa a travar
        PVector forcaDesejada = null;

        if (pos.x < margem) {
            forcaDesejada = new PVector(maxSpeed, vel.y);
        } else if (pos.x > p.width - margem) {
            forcaDesejada = new PVector(-maxSpeed, vel.y);
        }

        if (pos.y < margem) {
            forcaDesejada = new PVector(vel.x, maxSpeed);
        } else if (pos.y > p.height - margem) {
            forcaDesejada = new PVector(vel.x, -maxSpeed);
        }

        if (forcaDesejada != null) {
            forcaDesejada.normalize();
            forcaDesejada.mult(maxSpeed);
            PVector steer = PVector.sub(forcaDesejada, vel);
            steer.limit(maxForce * 2); // Força extra para não fugir
            applyForce(steer);
        }
    }

 // Novo método auxiliar para o Bombeiro:
    private void apagarArea(Floresta f, int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nx = x + i;
                int ny = y + j;
                if (nx >= 0 && nx < f.cols && ny >= 0 && ny < f.rows) {
                    if (f.grid[nx][ny] == 2) f.grid[nx][ny] = 1; // Salva a árvore
                }
            }
        }
    }
    
    public void update(float dt) {
        vel.add(PVector.mult(acc, dt * 50)); // Normaliza escala do dt
        vel.limit(maxSpeed);
        pos.add(PVector.mult(vel, dt * 50));
        acc.mult(0);
    }

 // Na classe Bombeiro
    public void display(PApplet p) {
        p.pushMatrix();
        p.translate(pos.x, pos.y);
        p.rotate(vel.heading()); // O avião aponta para onde se move
        
        // Desenha o avião (triângulo estilizado)
        p.fill(255);
        p.stroke(0, 100, 255);
        p.triangle(10, 0, -5, -5, -5, 5);
        
        // Rastro de fumo/água se estiver a apagar fogo
        if (fitness > 0) {
            p.noStroke();
            p.fill(255, 255, 255, 100);
            p.ellipse(-10, 0, 8, 8);
        }
        p.popMatrix();
    }
}