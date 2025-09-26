import processing.core.PApplet;
//classe PApplet tem um monte de cenas pa usar
public class HelloJava extends PApplet {
	
	public static void main(String[] args) {
		PApplet.main(HelloJava.class.getName());
		//abre um canvas vazio, so escrevendo isso ai q ta em cima, 
		//poe a correr e ve oq te da so este comando a cima
	}
	
	
	public void settings() {
		size(800,600);
		//tamanho do canvas e do renderizador
		//APENAS para definir o tamanho do canvas mais nada
	}
	
	public void setup() {
		//background(255,255,255);
		//cor de fundo do canvas rgb podemos usar de 1 a 4 numeros para definir a cor
		
		fill(0,0,0);// cor do circulo
	}
	
	public void draw() {//draw da para fazer animaçoes pois atualiza de 60 segundos em 60 (1 min)
		
		// A ORDEM DOS COMANDOS IMPORTA!!
		// experimenta comentar o background e des-comentar o fill para ver o que acontece :D
		
		//background(255,255,255);
		circle(mouseX, mouseY, 20);// desenha um circulo q segue o rato, o numero é o diametro do circulo
		fill(0,0,0);// cor do circulo 
		// no fill(r, g, b, transparecia)
	}
	//prox aula ter ja tudo configurado, e fazer o projeto zero um boneco ou uma cena simples

	
}
