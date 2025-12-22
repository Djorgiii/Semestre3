package simulacao;

import processing.core.PApplet;
import setup.iProcessing;
import java.util.ArrayList;

public class ForestFireApp implements iProcessing {
    Floresta floresta;
    ArrayList<Bombeiro> bombeiros;
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
        
        /*
        p.fill(50,50,50,30);
        for(int i = 0;i < 5;i++) {
        	float fx = p.noise(p.frameCount * 0.01f + i) * p.width;
        	float fy = p.noise(i, p.frameCount * 0.01f) * p.height;
        	p.ellipse(fx, fy, 200, 200);
        }
        */
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
    }
    
    

    private void evoluir(PApplet p) {
        Bombeiro melhor = bombeiros.get(0);
        for (Bombeiro b : bombeiros) {
            if (b.fitness > melhor.fitness) melhor = b;
        }
        
        melhorFitnessAnterior = melhor.fitness; 

        ArrayList<Bombeiro> proximaGen = new ArrayList<>();
        
        // 1. ELITISMO: Mantém o melhor exatamente como está
        proximaGen.add(new Bombeiro(p.random(p.width), p.random(p.height), melhor.maxSpeed));

        // 2. RESTO DA POPULAÇÃO: Filhos com pequenas mutações
        for (int i = 1; i < 20; i++) {
            float novoDNA = melhor.maxSpeed + p.random(-0.2f, 0.2f);
            if (novoDNA < 1.0f) novoDNA = 1.0f; // Não deixa ficar parado
            proximaGen.add(new Bombeiro(p.random(p.width), p.random(p.height), novoDNA));
        }
        bombeiros = proximaGen;
    }

    @Override public void keyPressed(PApplet p) { 
        if(p.key == ' ') setup(p); // Reset total
    }
    @Override public void mousePressed(PApplet p) {
        int i = (int)(p.mouseX / floresta.cellSize);
        int j = (int)(p.mouseY / floresta.cellSize);
        if(i >= 0 && i < floresta.cols && j >= 0 && j < floresta.rows) floresta.grid[i][j] = 2;
    }
    @Override public void keyReleased(PApplet p) {}
    @Override public void mouseMoved(PApplet p) {}
}