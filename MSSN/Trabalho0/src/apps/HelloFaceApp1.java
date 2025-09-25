package apps;

import processing.core.PApplet;
import processing.core.PVector;
import setup.iProcessing;

import java.util.ArrayList;
import java.util.List;

import apps.HelloFaceApp1;

public class HelloFaceApp1 implements iProcessing{
	
	
	//TODO
	//2-fazer aparecer faces de x em x segundos (temporarias)
	//3-matar faces(???)
	//4-mover as faces de modo que seguem o rato

	
	private float deltat= 0.5f; //2 segundos de tempo
	private int maxfaces= 10;
	private float timer;
	private List<Face> faces;
	
	public void mouseMoved(PApplet parent) {

		this.mousePressed(parent);
		
	}

	@Override
	public void setup(processing.core.PApplet parent) {
		timer = 0;
		faces = new ArrayList<Face>();
	}
	
	@Override
	public void draw(processing.core.PApplet parent, float dt) {
		
		parent.background(255);
		
		timer += dt;
		
		if (timer >=deltat && faces.size()< maxfaces ) { //se o timer for maior que o delta t
			float r = parent.random(20,80);
			// float x= parent.random(0, parent.width); para que a cara seja mesmo random pelo ecra todo e fora dele
			float x = parent.random(r, parent.width-r);
			float y = parent.random(r, parent.height-r);
			
			Face f= new Face(new PVector(x,y),r,parent);
			faces.add(f);
			timer = 0;
		}
		
		for (Face f: faces) { //fazer com que as caras sigam o rato
			//f.setTarget(new PVector(parent.mouseX, parent.mouseY));
			f.moveToTarget();
		}
		
		for (Face f: faces) { //aparecer para cada face na lista
			f.display(parent);
		}
	}
	
	@Override
	public void keyPressed(processing.core.PApplet parent) {
		
	}
	
	@Override
	public void mousePressed(processing.core.PApplet parent) {
		
		// no programa do prof ele meteu o clickPos como cp O.o
		PVector clickPos = new PVector(parent.mouseX, parent.mouseY);
		boolean anyface= false;
		
		if(parent.mouseButton == PApplet.RIGHT) { //quando clicar como botao direito
			//mudar a cor dos olhos
			
			Face f= new Face(clickPos, 30 ,parent);
			faces.add(f);
			
		}else {
			
			for (int i= faces.size()-1; i >= 0; i--) {
				Face f = faces.get(i);
				if(f.isInside(clickPos)) {
					anyface= true;
					faces.remove(f);
					//PApplet.println("ta dentro filho");
				}else {
					f.setTarget(clickPos);
				}
			}
			if(!anyface && faces.size()>0) {
				Face f= faces.get(0);
				f.setTarget(clickPos);
				
			}
		}
		
	}
	
	
}
