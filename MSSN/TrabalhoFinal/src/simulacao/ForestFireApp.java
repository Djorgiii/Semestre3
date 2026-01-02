package simulacao;


import processing.core.PApplet;
import processing.core.PImage;
import setup.iProcessing;
import java.util.ArrayList;


public class ForestFireApp implements iProcessing {
	PImage imgAviao;
    Floresta floresta;
    ArrayList<Bombeiro> bombeiros;
    float generationTimer = 0;
    int geracao = 1;
    int numBoidsDesejados = 20; // Valor inicial

    @Override
    public void setup(PApplet p) {
    	
    	// 1. Reset das variáveis de evolução e tempo
        geracao = 1;
        generationTimer = 0;
        imgAviao = p.loadImage("data/aviao.png");
    	
    	int colunasDesejadas = 128;
    	float calculoCellSize = (float)p.width / colunasDesejadas;
    	int linhasNecessarias = PApplet.floor(p.height / calculoCellSize);
        floresta = new Floresta(p, colunasDesejadas, linhasNecessarias);
        floresta.cellSize = calculoCellSize; // Ajusta o tamanho da célula conforme o cálculo
        
        bombeiros = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            bombeiros.add(new Bombeiro(p.random(p.width), p.random(p.height), p.random(1, 4)));
        }
    }

    @Override
    public void draw(PApplet p, float dt) {
        floresta.display(p);
        
        // Atualiza autómato a cada X segundos
        if (p.frameCount % 10 == 0) floresta.atualizar(p);
        
     // Lógica extra: Se não houver fogo nenhum no mapa, força um novo foco
        // Isto garante que a Seleção Natural tenha sempre dados para avaliar
        if (p.frameCount % 300 == 0) { // Aproximadamente a cada 5 segundos
            floresta.forçarFogoAleatorio(p);
        }

        for (Bombeiro b : bombeiros) {
            b.comportamentos(bombeiros, floresta);
            b.conter(p);
            b.update(dt);
            b.display(p, imgAviao);
        }

        // --- Lógica de Stocks & Flows ---
        p.fill(255);
        p.text("Clica +/- para alterar o numero de Bombeiros", 20, 30);
        p.text("Clica no espaço para dar reset à simulação ", 20, 50);
        

        // --- Lógica de Seleção Natural (a cada 15 segundos) ---
        generationTimer += dt;
        if (generationTimer > 15) {
            evoluir(p);
            generationTimer = 0;
            geracao++;
        }
        
    }

    private void evoluir(PApplet p) {
        if (bombeiros.isEmpty()) return;

        // 1. Encontrar o Campeão (Maior Fitness)
        Bombeiro melhor = bombeiros.get(0);
        for (Bombeiro b : bombeiros) {
            if (b.fitness > melhor.fitness) melhor = b;
        }

        ArrayList<Bombeiro> proximaGen = new ArrayList<>();

        // 2. ELITISMO: O campeão passa sempre (Seguro contra retrocesso)
        proximaGen.add(new Bombeiro(p.random(p.width), p.random(p.height), melhor.maxSpeed));

        // 3. SELEÇÃO NATURAL: Se o melhor não apagou nada, mantemos a busca aleatória
        // Se o melhor apagou fogo, os filhos herdam o gene dele
        for (int i = 1; i < numBoidsDesejados; i++) {
            float novoDNA;
            if (melhor.fitness > 0) {
                // Filho do campeão com mutação equilibrada
                novoDNA = melhor.maxSpeed + p.random(-0.1f, 0.3f); 
            } else {
                // Se ninguém trabalhou, explora novas velocidades aleatórias
                novoDNA = p.random(2, 8);
            }
            
            // Limites para não quebrar a física do jogo
            novoDNA = p.constrain(novoDNA, 1.5f, 12.0f);
            proximaGen.add(new Bombeiro(p.random(p.width), p.random(p.height), novoDNA));
        }
        
        bombeiros = proximaGen;
    }

    @Override public void keyPressed(PApplet p) { 
        if(p.key == ' ') setup(p); // Reset total
        
     // Aumentar Boids
        if(p.key == '+') {
            if (numBoidsDesejados < 100) { // Limite para não travar o PC
            	numBoidsDesejados++;
            	
            	float velDoMelhor = 2.0f;
            	if (!bombeiros.isEmpty()) {
            		Bombeiro campeaoAtual = bombeiros.get(0);
					for (Bombeiro b : bombeiros) {
						if (b.fitness > campeaoAtual.fitness) {
							campeaoAtual = b;
						}
					}
					velDoMelhor = campeaoAtual.maxSpeed;
            	}
                bombeiros.add(new Bombeiro(p.random(p.width), p.random(p.height), velDoMelhor) );
            }
        }
        
        // Diminuir Boids
        if(p.key == '-') {
            if (numBoidsDesejados > 1) {
            	numBoidsDesejados--;
                if(bombeiros.size() > 1) bombeiros.remove(bombeiros.size() - 1);
            }
        }
    }
    @Override 
    public void mousePressed(PApplet p) {
        int i = (int)(p.mouseX / floresta.cellSize);
        int j = (int)(p.mouseY / floresta.cellSize);
        
        // 1. Verifica se o clique está dentro dos limites da matriz
        if(i >= 0 && i < floresta.cols && j >= 0 && j < floresta.rows) {
            // 2. SÓ deixa queimar se for Árvore (1) ou Solo/Terra (4)
            // Se for Água (0), o clique é ignorado
            if (floresta.grid[i][j] == 1 || floresta.grid[i][j] == 4) {
                floresta.grid[i][j] = 2;
            }
        }
    }
    @Override public void keyReleased(PApplet p) {}
    @Override public void mouseMoved(PApplet p) {}
}