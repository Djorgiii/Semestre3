package apps;

import processing.core.PApplet;
import setup.iProcessing;
import tools.SubPlot;

public class TestApp implements iProcessing{
	
	private double[] window = {0, 5, 0, 7};
	private float[] view1 = {0.7f,0.55f,0.25f,0.25f};
	private float[] view2 = {0.2f,0f,0.45f,0.2f};
	private SubPlot plt1, plt2;
	
	@Override
	public void setup(PApplet parent) {
		plt1 = new SubPlot(window, view1, parent.width, parent.height);
		plt2 = new SubPlot(window, view2, parent.width, parent.height);

		float[] bb = plt1.getBoundingBox();
		parent.rect(bb[0], bb[1], bb[2], bb[3]);
		
		bb = plt2.getBoundingBox();
		parent.fill(255,255,0);
		parent.rect(bb[0], bb[1], bb[2], bb[3]);
		
		
		float[]xy = plt1.getPixelCoord(2.5,3.5);
		parent.circle(xy[0], xy[1], 10);
		
		xy = plt2.getPixelCoord(2.5,3.5);
		parent.circle(xy[0], xy[1], 10);
		
	}
	
	@Override
	public void draw(PApplet parent, float dt) {
		
		
		
	}
	@Override
	public void keyPressed(PApplet parent) {
		
	}
	@Override
	public void mousePressed(PApplet parent) {
		
		double[]xy = plt1.getWorldCoord(parent.mouseX, parent.mouseY);
		PApplet.println("World Coord: x= " + xy[0] + " y= " + xy[1]);
		
	}
	@Override
	public void mouseMoved(PApplet parent) {
		
	}
}
