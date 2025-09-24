package apps;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Face {
	
	private PVector position;
	private float size;
	private int eyeColor;
	private PVector target;
	
	public Face(PVector position, float size,PApplet parent) {
		this.position = position;
		this.size = size;
		eyeColor= parent.color(0);
	}
	public void setEyeColor( int c) {
		eyeColor= c;
	}
	public boolean isInside(PVector clickPos) {
		
		float d = PVector.dist(clickPos, this.position);
		// se a distancia for menor que o raio da cara
		return (d< this.size);
	}
	public void setTarget(PVector target) {
		this.target= target;
	}
	public void moveToTarget() {
		if(target ==null) {return;}
		float fct= 0.01f;
		PVector v= PVector.sub(this.target, this.position);
		position.add(v.mult(fct));
		
	}
	
	public void display(PApplet parent) {
		//tens q fazer push e pop para n afetar o resto do desenho
		// como usamos um eixo especifico para as coordenadas da cara
		//depois temos de voltar ao eixo original ent fazes push e pop
		parent.pushMatrix();
		
		// fronteira da cara
		parent.translate(position.x, position.y);
		parent.fill(180,180,360);
		parent.circle(0, 0, 2*size);
		
		parent.fill(0);
		//boca
		// representar uma elipse, dizer onde ta o centro, a largura e a altura, e o angulo
		// o centro vai ser no nariz 
		parent.arc(0, 0, 2*size, size, PApplet.radians(60), PApplet.radians(120), PConstants.CHORD);
		
		// nariz
		parent.fill(0);
		parent.circle(0, 0, size/4);
		parent.fill(eyeColor);
		// olhos, relembrar que o y para baixo é positivo e pa cima e negativo
		// olho direito
		parent.translate(0.3f*size, -0.4f*size);
		parent.circle(0, 0, size/5f);
		
		// olho esquerdo
		parent.translate(-2*0.3f*size, 0);
		parent.circle(0, 0, size/5f);
		
		parent.popMatrix();
		
	}
	
}
