package cnuphys.mosaic.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;

import cnuphys.mosaic.frame.Mosaic;
import cnuphys.mosaic.patch.Prepatch;
import cnuphys.mosaic.util.MathUtil;
import cnuphys.mosaic.util.Point3D;
import cnuphys.mosaic.util.SphericalVector;

/**
 * A curve on the surface of the sphere that is constant in theta and linear in phi.
 */
public class ThetaCurve extends BaseCurve {

	private double theta; // the constant theta value

	private int thetaIndex;

	private double dPhi;

	private boolean flipped = false;
	
	/**
     * Constructs a curve on the face of a cell and on the sphere of the given type.
     *
     * @param p0        the starting point of the curve
     * @param p1        the ending point of the curve
     * @param R         the radius of the sphere
     */
	public ThetaCurve(Point3D.Double p0, Point3D.Double p1, double R) {
		super(p0, p1, R);

		double thetaCheck = Math.abs(sv0.theta - sv1.theta);
		if (thetaCheck > Math.PI) {
			thetaCheck = 2 * Math.PI - thetaCheck;
		}

		if (thetaCheck > TOL) {
			throw new IllegalArgumentException("The endpoints are not at the same theta.");
		}

		theta = sv0.theta;

		thetaIndex = Mosaic.getInstance().getSphericalGrid().getThetaGrid().closestIndex(theta);

		dPhi = sv1.phi - sv0.phi;

		if (dPhi > Math.PI) {
			dPhi = dPhi - 2 * Math.PI;
		}
		if (dPhi < -Math.PI) {
			dPhi = dPhi + 2 * Math.PI;
		}
	}

	/**
	 * Create a theta curve from spherical coordinates.
	 *
	 * @param theta the polar angle in radians
	 * @param phi0  one azimuthal angle in radians
	 * @param phi1  the other azimuthal angle in radians
	 * @param R     the radius of the sphere
	 * @return a theta curve
	 */
	public static ThetaCurve createThetaCurve(double theta, double phi0, double phi1, double R) {

		phi0 = MathUtil.normalizeAngle(phi0);
		phi1 = MathUtil.normalizeAngle(phi1);

		Point3D.Double p0 = new SphericalVector(theta, phi0, R).toCartesian();
		Point3D.Double p1 = new SphericalVector(theta, phi1, R).toCartesian();
		ThetaCurve tc = new ThetaCurve(p0, p1, R);
		return tc;
	}

	@Override
	public UnivariateFunction getThetaFunction() {
		return new UnivariateFunction() {
			@Override
			public double value(double t) {
				return theta;
			}
		};
	}

	/**
	 * Get a copy of the curve but with the endpoints reversed.
	 * @return a reversed copy of the curve
	 */
	@Override
	public ThetaCurve reverse() {
		ThetaCurve tc = new ThetaCurve(p1, p0, R);
		if (flipped) {
			tc.flip();
		}
		return tc;
	}

	/**
	 * Get the theta index of the theta grid
	 *
	 * @return the theta index
	 */
	public int getThetaIndex() {
		return thetaIndex;
	}

	/**
	 * Flip the curve to make it go the other way around the sphere.
	 */
	public void flip() {
		if (flipped) {
			System.err.println("Already flipped");
			System.exit(1);
		}

		flipped = true;
		if (dPhi > 0) {
			dPhi = dPhi - 2 * Math.PI;
		} else {
			dPhi = dPhi + 2 * Math.PI;
		}
	}

	@Override
	public UnivariateFunction getPhiFunction() {
		return new UnivariateFunction() {
			@Override
			public double value(double t) {
				double phi = sv0.phi + dPhi * t;
				return MathUtil.normalizeAngle(phi);
			}
		};
	}

	@Override
	public double pathLength() {
	//	double dPhi = MathUtil.normalizedAngleDifference(sv0.phi, sv1.phi);
		return Math.abs(Math.sin(theta) * dPhi);
	}

	@Override
	public BaseCurve[] split(double t) {
		// TODO Auto-generated method stub
		return null;
	}


}
