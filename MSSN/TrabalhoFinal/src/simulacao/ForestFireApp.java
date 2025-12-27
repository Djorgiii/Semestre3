package simulacao;

import processing.core.PApplet;
import setup.iProcessing;
import java.util.ArrayList;

public class ForestFireApp implements iProcessing {
    Floresta floresta;
    ArrayList<Bombeiro> bombeiros;
 // No topo da ForestFireApp
    ArrayList<Float> historicoDNA = new ArrayList<>();
    float generationTimer = 0;
    int geracao = 1;
    float stockAgua = 1000; // Exemplo de Stock
    int totalArvoresSalvasGlobal = 0; // Acumula ao longo de todas as gerações
    int arvoresSalvasNestaGeracao = 0;
    float melhorFitnessAnterior = 0; // Para comparar se a evolução está a resultar

    @Override
    public void setup(PApplet p) {
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
            b.display(p);
        }
        
        arvoresSalvasNestaGeracao = 0;
        for (Bombeiro b : bombeiros) {
			arvoresSalvasNestaGeracao += b.fitness;
		}

        // --- Lógica de Stocks & Flows ---
        stockAgua -= 0.1f; // Flow de perda constante
        p.fill(255);
        p.text("Geração: " + geracao, 20, 30);
        p.text("Melhor Fitness Anterior: " + (int)melhorFitnessAnterior, 20, 50);
        p.text("Salvas nesta Geração: " + arvoresSalvasNestaGeracao, 20, 70);
        p.fill(0,255,0);
        p.text("Total Árvores Salvas: " + (totalArvoresSalvasGlobal + arvoresSalvasNestaGeracao), 20, 95);
        //p.text("Stock Água: " + (int)stockAgua, 20, 50);
        

        // --- Lógica de Seleção Natural (a cada 15 segundos) ---
        generationTimer += dt;
        if (generationTimer > 15) {
        	totalArvoresSalvasGlobal += arvoresSalvasNestaGeracao;
            evoluir(p);
            generationTimer = 0;
            geracao++;
        }
        
        desenharGraficoDNA(p);
    }

    // Método auxiliar para desenhar o gráfico no canto inferior
    void desenharGraficoDNA(PApplet p) {
        p.pushStyle();
        p.noFill();
        p.stroke(0, 255, 255); // Cor ciano
        p.beginShape();
        for (int i = 0; i < historicoDNA.size(); i++) {
            // Mapeia o gráfico para o canto inferior direito
            float x = p.map(i, 0, 50, p.width - 150, p.width - 20);
            float y = p.map(historicoDNA.get(i), 1, 10, p.height - 20, p.height - 100);
            p.vertex(x, y);
        }
        p.endShape();
        
        p.fill(255);
        p.textSize(10);
        p.text("Evolução da Velocidade (DNA)", p.width - 150, p.height - 105);
        
        // Mostrar a velocidade atual do melhor
        if (bombeiros.size() > 0) {
            float melhorV = 0;
            for(Bombeiro b : bombeiros) if(b.maxSpeed > melhorV) melhorV = b.maxSpeed;
            p.text("Vel. Máx Atual: " + p.nf(melhorV, 1, 2), p.width - 150, p.height - 5);
        }
        p.popStyle();
    }

    private void evoluir(PApplet p) {
        if (bombeiros.isEmpty()) return;

        // 1. Encontrar o Campeão (Maior Fitness)
        Bombeiro melhor = bombeiros.get(0);
        for (Bombeiro b : bombeiros) {
            if (b.fitness > melhor.fitness) melhor = b;
        }
        
        // Guardar para o gráfico
        historicoDNA.add(melhor.maxSpeed);
        if (historicoDNA.size() > 50) historicoDNA.remove(0);
        melhorFitnessAnterior = melhor.fitness;

        ArrayList<Bombeiro> proximaGen = new ArrayList<>();

        // 2. ELITISMO: O campeão passa sempre (Seguro contra retrocesso)
        proximaGen.add(new Bombeiro(p.random(p.width), p.random(p.height), melhor.maxSpeed));

        // 3. SELEÇÃO NATURAL: Se o melhor não apagou nada, mantemos a busca aleatória
        // Se o melhor apagou fogo, os filhos herdam o gene dele
        for (int i = 1; i < 20; i++) {
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