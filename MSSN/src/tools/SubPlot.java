package tools;

public class SubPlot {
	
	private double[] window;
	private float[] viewport;
	private double mx;
	private double bx;
	private double my;
	private double by;
	
	
	public SubPlot(double[] window, float[] viewport, float fullwidth, float fullheight) {
		this.window = window;
		this.viewport = viewport;
		mx = viewport[2] * fullwidth / (window[1] - window[0]);
		bx = viewport[0] * fullwidth;
		my = -viewport[3] * fullheight / (window[3] - window[2]);
		by = (1- viewport[1]) * fullheight;
	}
	
	public float[] getPixelCoord(double x, double y) {
		
		float[] coord = new float[2];
		
		coord[0] = (float) (bx + mx * (x - window[0]));
		coord[1] = (float) (by + my * (y - window[2]));
		return coord;
	}
	
	public float[] getPixelCoord(double[] xy) {
		
		return getPixelCoord(xy[0], xy[1]);
	}

	public double[] getWorldCoord(float xx, float yy) {
		
		double[] coord = new double[2];
		
		coord[0] = window[0] + (xx - bx) / mx;
		coord[1] = window[2] + (yy - by) / my;
		
		return coord;
	}
	
	public double[] getWorldCoord(float[] xy) {
		
		return getWorldCoord(xy[0], xy[1]);
	}
	
	public boolean isInSide(float xx, float yy) {
		
		double[] c = getWorldCoord(xx, yy);
		return (c[0] >= window[0] && c[0] <= window[1] &&
				c[1] >= window[2] && c[1] <= window[3]);
	}
	
	public boolean isInSide(float[] xy) {
		
		return isInSide(xy[0], xy[1]);
	}
	
	public float[] getBoundingBox() {
		
		float[]c1 = getPixelCoord(window[0], window[2]);
		float[]c2 = getPixelCoord(window[1], window[3]);
		float[] box = {c1[0], c2[1], c2[0]-c1[0], c1[1]-c2[1]};
		return box;
	}
	
	public float[] getBox(double cx, double cy, double dimx, double dimy) {
		
		float[] c1 = getPixelCoord(cx, cy);
		float[] c2 = getPixelCoord(cx + dimx, cy + dimy);
		float[] box = {c1[0], c2[1], c2[0]-c1[0], c1[1]-c2[1]};
		return box;
		
		
	}
	
	public float[] getBox (double[] b) {
		return getBox(b[0], b[1], b[2], b[3]);
	}
	
	
	public float[] getVectorCoord(double dx, double dy) {
		
		float[] v = new float[2];
		v[0] = (float) (mx * dx);
		v[1] = (float) (my * dy);
		return v;
	}
	
	public float[] getVectorCoord(double[] dxdy) {
		
		return getVectorCoord(dxdy[0], dxdy[1]);
	}
	
	public float[] getViewport() {
		return viewport;
	}
	public double[] getWindow() {
		return window;
	}
}
