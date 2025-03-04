package cnuphys.mosaic.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;

import cnuphys.mosaic.frame.Mosaic;
import cnuphys.mosaic.grid.SphericalGrid;
import cnuphys.mosaic.util.Point3D;
import cnuphys.mosaic.util.Point3D.Double;
import cnuphys.mosaic.util.SphericalVector;
import cnuphys.mosaic.util.ThetaPhi;

public abstract class BaseCurve {

	//used for tolerance checks
	protected static final double TOL = 1.0e-6;

	// The endpoints of the curve in Cartesian and spherical coordinates.
	protected Point3D.Double p0;
	protected Point3D.Double p1;
	protected SphericalVector sv0;
	protected SphericalVector sv1;

	// the radius of the sphere
	protected double R;

	///the theta crossings
	protected List<TValue> _thetaCrossings;
	
	///the phi crossings
	protected List<TValue> _phiCrossings;

	/**
	 * Constructs a curve on the surface of a sphere.
	 *
	 * @param p0        the starting point of the curve
	 * @param p1        the ending point of the curve
	 * @param R         the radius of the sphere
	 */
	public BaseCurve(Point3D.Double p0, Point3D.Double p1, double R) {
		this.p0 = p0;
		this.p1 = p1;
		this.R = R;

		sv0 = new SphericalVector(p0);
		sv1 = new SphericalVector(p1);

	}

	/** Getter for one spherical endpoint. */
	public SphericalVector getSV0() {
		return sv0;
	}

	/** Getter for the other spherical endpoint. */
	public SphericalVector getSV1() {
		return sv1;
	}

	/** Getter for one endpoint in Cartesian coordinates. */
	public Point3D.Double getP0() {
		return p0;
	}

	/** Getter for the other endpoint in Cartesian coordinates. */
	public Point3D.Double getP1() {
		return p1;
	}

	/** Getter for the radius of the sphere. */
	public double getRadius() {
		return R;
	}

    /**
     * Returns a point on the curve for a given parameter t in [0,1).
     * @param t the parameter
     * @return the point on the curve
     */
	public Double getPoint(double t) {
		double theta = theta(t);
		double phi = phi(t);
		double x = R * Math.sin(theta) * Math.cos(phi);
		double y = R * Math.sin(theta) * Math.sin(phi);
		double z = R * Math.cos(theta);
		return new Point3D.Double(x, y, z);
	}

	/**
	 * Returns the parametrized theta function.
	 * @return the theta function
	 */
	public abstract UnivariateFunction getThetaFunction();

	/**
	 * Returns the parametrized phi function.
	 * @return the phi function
	 */
	public abstract UnivariateFunction getPhiFunction();

	/**
	 * Returns the parametrized derivative of the theta function.
	 * @return the derivative of the theta function
     */
    public UnivariateFunction getDThetaFunction() {
		return ParametrizationDerivative(getThetaFunction());
    }

    /**
     * Returns the parametrized derivative of the phi function.
     * @return the derivative of the phi function
     */
    public UnivariateFunction getDPhiFunction() {
    	return ParametrizationDerivative(getPhiFunction());
    }

    /**
     * Returns the path length of the curve.
     * @return the path length of the curve
     */
    public abstract double pathLength();

	/**
	 * Returns the polar angle theta(t) (in radians) for the curve.
	 *
	 * @param t the parameter
	 * @return the polar angle theta(t) in radians
	 */
	public double theta(double t) {
		return getThetaFunction().value(t);
	}

	/**
	 * Returns the azimuthal angle phi(t) (in radians) for the curve.
	 *
	 * @param t the parameter
	 * @return the azimuthal angle phi(t) in radians
	 */
	public double phi(double t) {
		return getPhiFunction().value(t);
	}

	/**
	 * Computes the numerical derivative of a function using finite differences.
	 *
	 * @param f The function to differentiate.
	 * @return The derivative function.
	 */
	public static UnivariateFunction ParametrizationDerivative(UnivariateFunction f) {
		FiniteDifferencesDifferentiator differentiator = new FiniteDifferencesDifferentiator(5, 1e-5);
		return differentiator.differentiate(f);
	}

	/**
	 * Interpolates angles to ensure the shortest path is used.
	 */
	protected double interpolateAngle(double a0, double a1, double t) {
		double d = a1 - a0;
		if (d > Math.PI) {
			d -= 2 * Math.PI;
		} else if (d < -Math.PI) {
			d += 2 * Math.PI;
		}
		return a0 + t * d;
	}

	/**
	 * Get the points of the curve as a polyline for 3D visualization.
	 *
	 * @param n the number of points
	 * @return the polyline points
	 */
	public float[] getPolyline(int n) {
		double dt = 1.0 / (n - 1);
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
	
	/**
	 * Get the points of the curve as a ThetaPhi points for visualization.
	 * @param n the number of points
	 * @return the ThetaPhi points
	 */
	public ThetaPhi[] getThetaPhiPoints(int n) {
		double dt = 1.0 / (n - 1);
		ThetaPhi[] points = new ThetaPhi[n];

		for (int i = 0; i < n; i++) {
			double t = i * dt;
			points[i] = new ThetaPhi(getRadius(), theta(t), phi(t));
		}
		return points;
	}
	
	/**
	 * Get the points of the curve as a polyline for 3D visualization.
	 * @param t0 the starting parameter
	 * @param t1 the ending parameter
	 * @param n the number of points
	 * @return the polyline points
	 */
	public float[] getPolyline(double t0, double t1, int n) {
		double dt = (t1-t0) / (n - 1);
		float[] points = new float[3 * n];

		for (int i = 0; i < n; i++) {
			double t = t0 + i * dt;
			Point3D.Double p = getPoint(t);
			points[3 * i] = (float) p.x;
			points[3 * i + 1] = (float) p.y;
			points[3 * i + 2] = (float) p.z;
		}
		return points;
	}


	/**
	 * Checks if the endpoint of this curve connects to the start of another curve.
	 * @param curve the other curve
	 * @return true if the curves connect
	 */
	public boolean connectsTo(BaseCurve curve) {
		return pointsAreClose(p1, curve.p0);
//		return Point3D.Double.distance(p1, curve.p0) < TOL;
	}
	

	/**
	 * Get a copy of the curve but with the endpoints reversed.
	 * @return a reversed copy of the curve
	 */
    public abstract BaseCurve reverse();
    
	public boolean isItsReverse(BaseCurve curve) {
		return (this.p0.equals(curve.p1) && this.p1.equals(curve.p0));
	}

	/**
	 * Get the t values where the curve crosses a theta grid line
	 * @return a list of t values
	 */
	public List<TValue> getThetaCrossings() {
		if ((_thetaCrossings != null) || (this instanceof ThetaCurve)) {
			return _thetaCrossings;
		}

		SphericalGrid sgrid = Mosaic.getInstance().getSphericalGrid();
		_thetaCrossings = new ArrayList<>();


		int numIntervals = 100;
		double step = 1.0 / numIntervals;

		//bracket any crossings
		for (int i = 0; i < numIntervals; i++) {
			double t1 = i * step;
			double t2 = (i + 1) * step;
			int theta1Index = sgrid.getThetaGrid().locateInterval(theta(t1));
			int theta2Index = sgrid.getThetaGrid().locateInterval(theta(t2));
			int minIndex = Math.min(theta1Index, theta2Index);


			// Check for sign change in this subinterval
			if (theta1Index != theta2Index) {
				double theta = sgrid.getThetaGrid().gridValue(minIndex + 1);
				TValue tValue = new TValue(this, getThetaFunction(), theta, t1, t2, 1.0e-10);
				if (!java.lang.Double.isNaN(tValue.t)) {
					_thetaCrossings.add(tValue);
				}
			}
		}


		return _thetaCrossings;
	}
	
	/**
	 * Get the t values where the curve crosses a phi grid line
	 * @return a list of t values
	 */
	public List<TValue> getPhiCrossings() {
		if ((_phiCrossings != null) || (this instanceof PhiCurve)) {
			return _phiCrossings;
		}

		SphericalGrid sgrid = Mosaic.getInstance().getSphericalGrid();
		_phiCrossings = new ArrayList<>();


		int numIntervals = 100;
		double step = 1.0 / numIntervals;

		//bracket any crossings
		for (int i = 0; i < numIntervals; i++) {
			double t1 = i * step;
			double t2 = (i + 1) * step;
			int phi1Index = sgrid.getPhiGrid().locateInterval(phi(t1));
			int phi2Index = sgrid.getPhiGrid().locateInterval(phi(t2));
			int minIndex = Math.min(phi1Index, phi2Index);


			// Check for sign change in this subinterval
			if (phi1Index != phi2Index) {
				double phi = sgrid.getPhiGrid().gridValue(minIndex + 1);
				TValue tValue = new TValue(this, getPhiFunction(), phi, t1, t2, 1.0e-10);
				if (!java.lang.Double.isNaN(tValue.t)) {
					_phiCrossings.add(tValue);
				}
			}
		}


		return _phiCrossings;
	}


	private static boolean sameNum(double a, double b) {
        return Math.abs(a - b) < TOL;
    }

	/**
	 * Check if two curves are the same. That is if they have the same endpoints and radius
	 * even if the endpoints are reversed
	 * @param c1 one curve
	 * @param c2 the other curve
	 * @return true if the curves are the same (ignoring endpoint order)
	 */
	public static boolean sameCurve(BaseCurve c1, BaseCurve c2) {
		if (!sameNum(c1.getRadius(), c2.getRadius())) {
			return false;
		}

		double d0 = Point3D.Double.distance(c1.getP0(), c2.getP0());
		double d1 = Point3D.Double.distance(c1.getP1(), c2.getP1());

		if (sameNum(d0, 0) && sameNum(d1, 0)) {
			return true;
		}

		double d2 = Point3D.Double.distance(c1.getP0(), c2.getP1());
		double d3 = Point3D.Double.distance(c1.getP1(), c2.getP0());

		if (sameNum(d2, 0) && sameNum(d3, 0)) {
			return true;
		}

		return false;
	}

	/**
	 * Remove duplicate curves from a list of curves
	 * @param curves the list of curves
	 */
	public static void removeDuplicateCurves(List<BaseCurve> curves) {
		for (int i = 0; i < curves.size(); i++) {
			BaseCurve c1 = curves.get(i);
			for (int j = i + 1; j < curves.size(); j++) {
				BaseCurve c2 = curves.get(j);
				if (sameCurve(c1, c2)) {
					curves.remove(j);
					j--;
				}
			}
		}
	}

	/**
	 * Split the curve at a given parameter value.
	 *
	 * @param t the parameter value
	 * @return an array of two curves
	 */
	public abstract BaseCurve[] split(double t);
	
	
    /**
     * Validates that the curves form a continuous closed loop.
     * Throws an exception if any curve does not connect properly.
     */
    public static boolean validateLoop(List<BaseCurve> curves) {
        for (int i = 0; i < curves.size(); i++) {
            BaseCurve current = curves.get(i);
            BaseCurve next = curves.get((i + 1) % curves.size()); // Wrap around for last-to-first

            Point3D.Double currentEnd = current.getP1();
            Point3D.Double nextStart = next.getP0();

            if (!pointsAreClose(currentEnd, nextStart)) {
            	System.err.println("currentEnd: " + currentEnd);
            	System.err.println("nextStart: " + nextStart);
            	System.err.println("distance: " + Point3D.Double.distance(currentEnd, nextStart));
                return false;
             }
        }
        return true;
    }
    
    /**
     * Checks if two points are close enough to be considered the same.
     *
     * @param p1 First point.
     * @param p2 Second point.
     * @return true if the points are close within a tolerance, false otherwise.
     */
    public static  boolean pointsAreClose(Point3D.Double p1, Point3D.Double p2) {
        final double TOLERANCE = 1.0e-6;
        return Math.abs(p1.x - p2.x) < TOLERANCE &&
               Math.abs(p1.y - p2.y) < TOLERANCE &&
               Math.abs(p1.z - p2.z) < TOLERANCE;
    }

	public String toString() {
		return String.format("p0 %s p1 %s sv0 %s sv1 %s len %.4f]",
				p0, p1, sv0, sv1, pathLength());
	}


}
