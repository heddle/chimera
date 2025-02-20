package cnuphys.chimera.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;

import cnuphys.chimera.frame.Mosaic;
import cnuphys.chimera.grid.Cell;
import cnuphys.chimera.grid.SphericalGrid;
import cnuphys.chimera.util.Point3D;
import cnuphys.chimera.util.SphericalVector;
import cnuphys.chimera.util.Point3D.Double;

/**
 * Represents a general curve in 3D space. The curve will be on the face of a
 * rectangular prism (cell), and on the surface of the sphere of radius R. It is
 * not a grate circle.
 */
public class GeneralCurve implements Curve, ISegment {

	private static final double TOL = 1.0e-6;

	private static final int INTEGRATION_MAX_EVAL = 1000;

	public enum CurveType {
		GENERAL, CONSTANT_THETA, CONSTANT_PHI
	};

	// The face number of the cell in the range [0, 5]
	private int face;

	// The endpoints of the curve in Cartesian and spherical coordinates.
	private Point3D.Double p0;
	private Point3D.Double p1;
	private SphericalVector sv0;
	private SphericalVector sv1;

	// the radius of the sphere
	private double R;

	// theta indices of the endpoints
	private int theta0;
	private int theta1;

	// the type of curve
	private CurveType curveType = CurveType.GENERAL;

	// the arc length of the curve
	private double length;

	/**
	 * Constructs a curve on the face of a cell and on the sphere of type GENERAL.
	 * 
	 * @param cell the cell that the curve is on
	 * @param face the face of the cell in the range [0, 5] (0, 1: Z-normal; 2, 3:
	 *             Y-normal; 4, 5: X-normal
	 * @param p0   the starting point of the curve
	 * @param p1   the ending point of the curve
	 * @param R    the radius of the sphere
	 */
	public GeneralCurve(Cell cell, int face, Point3D.Double p0, Point3D.Double p1, double R) {
		this(cell, face, p0, p1, R, CurveType.GENERAL);
	}

	public GeneralCurve(Cell cell, int face, Point3D.Double p0, Point3D.Double p1, double R, CurveType curveType) {
		this.face = face;
		this.p0 = p0;
		this.p1 = p1;
		this.R = R;

		sv0 = new SphericalVector(p0);
		sv1 = new SphericalVector(p1);

		// Validate curve type
		if (curveType == CurveType.CONSTANT_THETA && Math.abs(sv0.theta - sv1.theta) >= TOL) {
			throw new IllegalArgumentException("CurveType is CONSTANT_THETA, but theta0 != theta1");
		}
		if (curveType == CurveType.CONSTANT_PHI && Math.abs(sv0.phi - sv1.phi) >= TOL) {
			throw new IllegalArgumentException("CurveType is CONSTANT_PHI, but phi0 != phi1");
		}

		SphericalGrid sphGrid = Mosaic.getInstance().getSphericalGrid();
		theta0 = sphGrid.getThetaGrid().locateInterval(sv0.theta);
		theta1 = sphGrid.getThetaGrid().locateInterval(sv1.theta);
		this.curveType = curveType;

		// cache the arc length
		length = pathLength();
	}

	/** Getter for one spherical endpoint. */
	public SphericalVector getSV0() {
		return sv0;
	}

	/** Getter for the other spherical endpoint. */
	public SphericalVector getSV1() {
		return sv1;
	}

	/** Getter for the face number. */
	public int getFace() {
		return face;
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

	/** Get the type of curve. */
	public CurveType getCurveType() {
		return curveType;
	}

	/** Get the arc length of the curve. */
	public double getLength() {
		return length;
	}

	@Override
	public Double getPoint(double t) {
		double theta = theta(t);
		double phi = phi(t);
		double x = R * Math.sin(theta) * Math.cos(phi);
		double y = R * Math.sin(theta) * Math.sin(phi);
		double z = R * Math.cos(theta);
		return new Point3D.Double(x, y, z);
	}

	/**
	 * Get the path length of the curve
	 * 
	 * @return the path length
	 */
	@Override
	public double pathLength() {
		switch (curveType) {
		case GENERAL:
			return lengthGeneral();
		case CONSTANT_THETA:
			return R * Math.sin(sv0.theta) * Math.abs(sv1.phi - sv0.phi);
		case CONSTANT_PHI:
			return R * Math.abs(sv1.theta - sv0.theta);
		default:
			throw new IllegalStateException("Unexpected curve type: " + curveType);
		}
	}

	/**
	 * Compute the arc length of the curve using numerical integration.
	 * 
	 * @return the arc length
	 */
	private double lengthGeneral() {
		UnivariateFunction integrand = t -> {
			double theta = theta(t);
			double dPhi = getDPhiFunction().value(t);
			return R * Math.sqrt(Math.pow(getDThetaFunction().value(t), 2) + Math.pow(Math.sin(theta) * dPhi, 2));
		};

		SimpsonIntegrator integrator = new SimpsonIntegrator();
		return integrator.integrate(INTEGRATION_MAX_EVAL, integrand, 0, 1);
	}

	/**
	 * Interpolates angles to ensure the shortest path is used.
	 */
	private double interpolateAngle(double a0, double a1, double t) {
		double d = a1 - a0;
		if (d > Math.PI) {
			d -= 2 * Math.PI;
		} else if (d < -Math.PI) {
			d += 2 * Math.PI;
		}
		return a0 + t * d;
	}

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
	 * Returns a UnivariateFunction for the polar angle theta(t) (in radians) To be
	 * used in apache common maths integrator. in standard spherical coordinates: x
	 * = R sin(theta) cos(phi), y = R sin(theta) sin(phi), z = R cos(theta).
	 */
	@Override
	public UnivariateFunction getThetaFunction() {
		if (face == 0 || face == 1) {
			// Z-normal: theta is constant: theta = arccos(z0/R)
			final double theta0 = Math.acos(p0.z / R);
			return new UnivariateFunction() {
				@Override
				public double value(double t) {
					return theta0;
				}
			};
		} else if (face == 4 || face == 5) {
			// X-normal: using auxiliary angle psi(t) interpolated from
			// psi0 = atan2(p0.z, p0.y) and psi1 = atan2(p1.z, p1.y)
			final double psi0 = Math.atan2(p0.z, p0.y);
			final double psi1 = Math.atan2(p1.z, p1.y);
			final double x0 = p0.x;
			final double r = Math.sqrt(R * R - x0 * x0);
			return new UnivariateFunction() {
				@Override
				public double value(double t) {
					double psi = interpolateAngle(psi0, psi1, t);
					// In Cartesian: z = r sin(psi); so spherical theta = arccos(z/R)
					double arg = (r * Math.sin(psi)) / R;
					// Clamp to [-1, 1] to be safe.
					if (arg > 1)
						arg = 1;
					if (arg < -1)
						arg = -1;
					return Math.acos(arg);
				}
			};
		} else if (face == 2 || face == 3) {
			// Y-normal: using auxiliary angle psi(t) from
			// psi0 = atan2(p0.z, p0.x) and psi1 = atan2(p1.z, p1.x)
			final double psi0 = Math.atan2(p0.z, p0.x);
			final double psi1 = Math.atan2(p1.z, p1.x);
			final double y0 = p0.y;
			final double r = Math.sqrt(R * R - y0 * y0);
			return new UnivariateFunction() {
				@Override
				public double value(double t) {
					double psi = interpolateAngle(psi0, psi1, t);
					double arg = (r * Math.sin(psi)) / R;
					if (arg > 1)
						arg = 1;
					if (arg < -1)
						arg = -1;
					return Math.acos(arg);
				}
			};
		}
		return null;
	}

	/**
	 * Returns a UnivariateFunction for the azimuthal angle phi(t) (in radians) in
	 * standard spherical coordinates. To be used in apache common maths integrator.
	 */
	@Override
	public UnivariateFunction getPhiFunction() {
		if (face == 0 || face == 1) {
			// Z-normal: phi(t) is linear interpolation of the unwrapped angles.
			final double phi0 = sv0.phi;
			final double phi1 = sv1.phi;
			return new UnivariateFunction() {
				@Override
				public double value(double t) {
					return interpolateAngle(phi0, phi1, t);
				}
			};
		} else if (face == 4 || face == 5) {
			// X-normal: with auxiliary angle psi.
			// The Cartesian coordinates on the face: x = x0 (constant),
			// y = r cos(psi), z = r sin(psi) with r = sqrt(R^2 - x0^2).
			// Then spherical phi = arctan2(y, x) = arctan2(r cos(psi), x0).
			final double psi0 = Math.atan2(p0.z, p0.y);
			final double psi1 = Math.atan2(p1.z, p1.y);
			final double x0 = p0.x;
			final double r = Math.sqrt(R * R - x0 * x0);
			return new UnivariateFunction() {
				@Override
				public double value(double t) {
					double psi = interpolateAngle(psi0, psi1, t);
					return Math.atan2(r * Math.cos(psi), x0);
				}
			};
		} else if (face == 2 || face == 3) {
			// Y-normal: with auxiliary angle psi.
			// Cartesian coordinates: y = y0 (constant), x = r cos(psi), z = r sin(psi)
			// and spherical phi = arctan2(y, x) = arctan2(y0, r cos(psi)).
			final double psi0 = Math.atan2(p0.z, p0.x);
			final double psi1 = Math.atan2(p1.z, p1.x);
			final double y0 = p0.y;
			final double r = Math.sqrt(R * R - y0 * y0);
			return new UnivariateFunction() {
				@Override
				public double value(double t) {
					double psi = interpolateAngle(psi0, psi1, t);
					return Math.atan2(y0, r * Math.cos(psi));
				}
			};
		}
		return null;
	}

	@Override
	public UnivariateFunction getDThetaFunction() {
		return ParametrizationDerivative(getThetaFunction());
	}

	@Override
	public UnivariateFunction getDPhiFunction() {
		return ParametrizationDerivative(getPhiFunction());
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
	 * If true crosses theta boundary (may fail on pole cases).
	 * 
	 * @return true if crosses theta boundary
	 */
	public boolean crossesThetaBoundary() {
		return theta0 != theta1;
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
}
