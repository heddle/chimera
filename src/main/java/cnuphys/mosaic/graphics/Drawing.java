package cnuphys.mosaic.graphics;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.mosaic.curve.BaseCurve;
import cnuphys.mosaic.curve.PhiCurve;
import cnuphys.mosaic.curve.ThetaCurve;
import cnuphys.mosaic.patch.BasePatch;
import cnuphys.mosaic.util.ThetaPhi;

public class Drawing {
	
	public static void drawBoundingBox3D(GLAutoDrawable drawable, BasePatch patch, Color color, float lineWidth) {
		ThetaPhi[] boundingBox = patch.getBoundingBox();
		double theta0 = boundingBox[0].getTheta();
		double theta1 = boundingBox[1].getTheta();
		double phi0 = boundingBox[0].getPhi();
		double phi1 = boundingBox[1].getPhi();
		double r = patch.getRadius();
		
		ThetaPhi tp0 = new ThetaPhi(r, theta0, phi0);
		ThetaPhi tp1 = new ThetaPhi(r, theta1, phi0);
		ThetaPhi tp2 = new ThetaPhi(r, theta1, phi1);
		ThetaPhi tp3 = new ThetaPhi(r, theta0, phi1);
		
		BaseCurve curve = new PhiCurve(tp0.toCartesian(), tp1.toCartesian(), r);
		curveDraw3D(drawable, curve, Color.red, lineWidth);
		
		curve = new ThetaCurve(tp1.toCartesian(), tp2.toCartesian(), r);
		curveDraw3D(drawable, curve, Color.blue, lineWidth);
		
		curve = new PhiCurve(tp2.toCartesian(), tp3.toCartesian(), r);
		curveDraw3D(drawable, curve, Color.green, lineWidth);
		
		curve = new ThetaCurve(tp3.toCartesian(), tp0.toCartesian(), r);
		curveDraw3D(drawable, curve, color, lineWidth);

	}

	/**
	 * Draw a curve on a 3D panel
	 * @param curve the curve to draw
	 */
	public static void curveDraw3D(GLAutoDrawable drawable, BaseCurve curve, Color color, float lineWidth) {
		float[] points = curve.getPolyline(50);
		Support3D.drawPolyLine(drawable, points, color, lineWidth);
	}
}
