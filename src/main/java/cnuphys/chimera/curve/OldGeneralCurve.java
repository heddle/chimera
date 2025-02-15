package cnuphys.chimera.curve;

import java.awt.geom.Point2D;

import cnuphys.chimera.grid.Cell;
import cnuphys.chimera.util.ChimeraPlane;
import cnuphys.chimera.util.Point3D;
import cnuphys.chimera.util.Point3D.Double;
import cnuphys.chimera.util.SphericalVector;

public class OldGeneralCurve implements Curve {
	
	private double[][] rMatrix;
	private double[][] rMatrixInv;
	
	private SphericalVector sv0Prime;
	private SphericalVector sv1Prime;
	
	private double radius;
	double thetaPrime;
	double phi0Prime;
	double delPhi;
	Point3D.Double wp = new Point3D.Double();
	
	public OldGeneralCurve(Cell cell, int face, Point3D.Double p0, Point3D.Double p1, double R) {
		radius = R;
		//get the rotation matrices that rotate the normal to the z-axis
        ChimeraPlane plane = cell.getPlane(face);
        double[][][] matrices = plane.getRotationMatrices();
        
        rMatrix = matrices[0];
        rMatrixInv = matrices[1];
        
        //rotate the points to the primed system
        Point3D.Double p0Prime = rotate(p0);
        Point3D.Double p1Prime = rotate(p1);
        
        //get the spherical vectors
        sv0Prime = new SphericalVector(p0Prime);
        sv1Prime = new SphericalVector(p1Prime);
        thetaPrime = sv0Prime.theta;
        phi0Prime = sv0Prime.phi;
        delPhi = sv1Prime.phi - sv0Prime.phi;
        
        if (delPhi > Math.PI) {
            delPhi -= 2 * Math.PI;
        } else if (delPhi < -Math.PI) {
            delPhi += 2 * Math.PI;
        }
        
        sv1Prime.phi = sv0Prime.phi + delPhi;
    }

	@Override
	public Double getPoint(double t) {
		SphericalVector sv = new SphericalVector(thetaPrime, phi0Prime + t * delPhi, radius);
		sv.toCartesian(wp);
		return rotateBack(wp);
	}
	
	// rotate a point back to the original system
	private Point3D.Double rotateBack(Point3D.Double p) {
		// rotate the point
		double x = rMatrixInv[0][0] * p.x + rMatrixInv[0][1] * p.y + rMatrixInv[0][2] * p.z;
		double y = rMatrixInv[1][0] * p.x + rMatrixInv[1][1] * p.y + rMatrixInv[1][2] * p.z;
		double z = rMatrixInv[2][0] * p.x + rMatrixInv[2][1] * p.y + rMatrixInv[2][2] * p.z;

		return new Point3D.Double(x, y, z);
	}
	
	/**
	 * Get the arc length of the curve.
	 * 
	 * @return the arc length
	 */
	public double arcLength() {
		return radius * Math.abs(delPhi);
	}
	
	/**
	 * Get the points of the curve as a polyline for jogle 3D drawing.
	 * @param n the number of points
	 * @return the polyline points
	 */
	public float[] getPolyline(int n) {
		double dt = 1.0 / (n-1);
		float[] points = new float[3 * n];
		
		for (int i = 0; i < n; i++) {
			double t = i * dt;
			Point3D.Double p = getPoint(t);
			points[3 * i] = (float) p.x;
			points[3 * i + 1] = (float) p.y;
			points[3 * i + 2] = (float) p.z;
		}
		return points;
	}

	// rotate a point to the primed system where the normal is the z-axis
	private Point3D.Double rotate(Point3D.Double p) {
        //rotate the point
        double x = rMatrix[0][0]*p.x + rMatrix[0][1]*p.y + rMatrix[0][2]*p.z;
        double y = rMatrix[1][0]*p.x + rMatrix[1][1]*p.y + rMatrix[1][2]*p.z;
        double z = rMatrix[2][0]*p.x + rMatrix[2][1]*p.y + rMatrix[2][2]*p.z;
        
        return new Point3D.Double(x, y, z);
    }

}
