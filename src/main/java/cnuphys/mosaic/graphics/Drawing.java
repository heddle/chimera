package cnuphys.mosaic.graphics;

import java.awt.Color;
import java.util.Random;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.mosaic.curve.BaseCurve;
import cnuphys.mosaic.patch.BasePatch;

public class Drawing {
	
	private static Random random;
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

	/**
	 * Draw a patch on a 3D panel
	 *
	 * @param drawable the drawable
	 * @param patch the patch to draw
	 * @param lineColor the color of the lines
	 * @param fillColor the color of the fill
	 * @param lineWidth the width of the lines
	 */
	public static void drawPatch3D(GLAutoDrawable drawable, BasePatch patch, Color lineColor, Color fillColor, float lineWidth) {

		int n = 25;
		double delta = 1.0 / n;
		int numCurve = patch.getCurves().size();

		float[] coords = new float[2 * numCurve * n];

		int i = 0;
		for (BaseCurve curve : patch.getCurves()) {
			for (int j = 0; j < n; j++) {
				double t = j * delta;
				coords[i++] = (float) curve.theta(t);
				coords[i++] = (float) curve.phi(t);
			}
		}

		Support3D.drawSphericalPolygon(drawable, (float)(patch.getRadius()), coords, lineColor, fillColor, lineWidth);

	}

	public static Color randomColor() {
		if (random == null) {
			random = new Random();
		}
		return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.2f);
	}
}
