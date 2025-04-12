package cnuphys.mosaic.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Random;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.mosaic.curve.BaseCurve;
import cnuphys.mosaic.frame.MapOverlayView2D;
import cnuphys.mosaic.frame.Mosaic;
import cnuphys.mosaic.patch.BasePatch;
import cnuphys.mosaic.patch.Tuple;
import cnuphys.mosaic.util.ThetaPhi;

public class Drawing {
	
	private static Random random;
	
	private static Color[] mapColors1 = { Color.red, new Color(70, 130, 180), new Color(34, 200, 34), Color.yellow,
			new Color(240, 230, 140), new Color(128, 128, 128), new Color(48, 48, 48), Color.orange,
			new Color(95, 158, 160), new Color(200, 20, 60), Color.cyan, Color.pink };

	private static Color[] monochromeColors = { new Color(32, 32, 32), new Color(64, 64, 64), new Color(96, 96, 96),
			new Color(128, 128, 128), new Color(160, 160, 160), new Color(192, 192, 193) };

	private static Color mapColors[] = Mosaic.monochrome ? monochromeColors : mapColors1;

	
	public static void curveDraw2D(Graphics g, MapOverlayView2D view, BaseCurve curve, Color color, int lineWidth) {
		ThetaPhi[] points = curve.getThetaPhiPoints(50);
		
		Point pp0 = new Point();
		Point pp1 = new Point();
		
		boolean p0vis;
		boolean p1vis;
		
		p0vis = view.thetaPhiToLocal(pp0, points[0]);
		g.setColor(color);
		
		for (int i = 1; i < points.length; i++) {
			p1vis = view.thetaPhiToLocal(pp1, points[i]);
			
			if (p0vis && p1vis && dist(pp0, pp1) < 400) {
				g.drawLine(pp0.x, pp0.y, pp1.x, pp1.y);
			}
			pp0.setLocation(pp1);
			p0vis = p1vis;
		}
	}
	
	private static double dist(Point p0, Point p1) {
		double dx = p1.x - p0.x;
		double dy = p1.y - p0.y;
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	/**
	 * Draw a curve on a 3D panel
	 * @param drawable the drawable
	 * @param curve the curve to draw
	 * @param color the color of the curve
	 * @param lineWidth the width of the line
	 */
	public static void curveDraw3D(GLAutoDrawable drawable, BaseCurve curve, Color color, float lineWidth) {
		float[] points = curve.getPolyline(50);
		Support3D.drawPolyLine(drawable, points, color, lineWidth);
	}
	
	//used for debugging
	public static void taperedCurveDraw3D(GLAutoDrawable drawable, BaseCurve curve, Color color, float lineWidth) {

		int nsteps = 8;
		double delta = 1.0 / nsteps;
		
		for (int i = 0; i < nsteps; i++) {
			int j = nsteps - i - 1;
			float[] points = curve.getPolyline(i * delta, (i + 1) * delta, 50);
			Support3D.drawPolyLine(drawable, points, color, lineWidth + 2*j);
		}
	
	}
	
	/**
	 * Draw a patch on a 3D panel
	 *
	 * @param drawable the drawable
	 * @param patch the patch to draw
	 * @param lineColor the color of the lines
	 * @param fillColor the color of the fill
	 * @param lineWidth the width of the lines
	 */
	public static void drawPatch2D(Graphics g, MapOverlayView2D view, BasePatch patch,Color lineColor, Color fillColor, int lineWidth) {
		for (BaseCurve curve : patch.getCurves()) {
			curveDraw2D(g, view, curve, lineColor, lineWidth);
		}
	}

	/**
	 * Draw a patch on a 3D panel
	 *
	 * @param drawable the drawable
	 * @param patch the patch to draw
	 * @param lineColor the color of the lines
	 * @param lineWidth the width of the lines
	 */
	public static void drawPatch3D(GLAutoDrawable drawable, BasePatch patch, Color lineColor,  float lineWidth) {

		int n = 25;
		double delta = 1.0 / n;
		int numCurve = patch.getCurves().size();

		float[] coords = new float[2 * numCurve * n];

		int i = 0;
		for (BaseCurve curve : patch.getCurves()) {
			curveDraw3D(drawable, curve, lineColor, lineWidth);
			for (int j = 0; j < n; j++) {
				double t = j * delta;
				coords[i++] = (float) curve.theta(t);
				coords[i++] = (float) curve.phi(t);
			}
		}

		Color fillColor = Drawing.tupleColor(patch.getTuple());
		Support3D.drawSphericalPolygon(drawable, (float)(patch.getRadius()), coords, null, fillColor, lineWidth);

	}

	public static Color randomColor() {
		if (random == null) {
			random = new Random();
		}
		return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.2f);
	}
	
	/**
	 * Get the color of a tuple
	 * 
	 * @param tuple the tuple
	 * @return the color
	 */
	public static Color tupleColor(Tuple tuple) {
		int sum = 7*tuple.getNx() + 5 * tuple.getNy() + 3 * tuple.getNz();
		if (tuple.length > 3) {
			sum += tuple.getNtheta();
		}
		if (tuple.length > 4) {
			sum += 2*tuple.getNphi();
		}
		return mapColors[sum % mapColors.length];
	}
}
