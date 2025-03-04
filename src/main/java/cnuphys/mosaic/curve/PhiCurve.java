package cnuphys.mosaic.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;

import cnuphys.mosaic.frame.Mosaic;
import cnuphys.mosaic.util.MathUtil;
import cnuphys.mosaic.util.Point3D;
import cnuphys.mosaic.util.SphericalVector;

/**
 *
 */
public class PhiCurve extends BaseCurve {

	private double phi; // the constant phi value
	
	private int phiIndex;

	/**
     * Constructs a curve on the face of a cell and on the sphere of the given type.
     *
     * @param p0        the starting point of the curve
     * @param p1        the ending point of the curve
     * @param R         the radius of the sphere
     */
	public PhiCurve(Point3D.Double p0, Point3D.Double p1, double R) {
		super(p0, p1, R);

		double phiCheck = Math.abs(sv0.phi - sv1.phi);
		if (phiCheck > Math.PI) {
			phiCheck = 2 * Math.PI - phiCheck;
		}

		if (phiCheck > TOL) {
			throw new IllegalArgumentException("The endpoints are not at the same phi.");
		}

		phi = MathUtil.normalizeAngle(sv0.phi);
		
		phiIndex = Mosaic.getInstance().getSphericalGrid().getPhiGrid().closestIndex(phi);

	}
	
	/**
	 * Create a phi curve from spherical coordinates.
	 *
	 * @param theta the polar angle in radians
	 * @param phi0  one azimuthal angle in radians
	 * @param phi1  the other azimuthal angle in radians
	 * @param R     the radius of the sphere
	 * @return a theta curve
	 */
	public static PhiCurve createPhiCurve(double theta0, double theta1, double phi, double R) {

		phi = MathUtil.normalizeAngle(phi);

		Point3D.Double p0 = new SphericalVector(theta0, phi, R).toCartesian();
		Point3D.Double p1 = new SphericalVector(theta1, phi, R).toCartesian();
		PhiCurve pc = new PhiCurve(p0, p1, R);
		return pc;
	}


	/**
	 * Get a copy of the curve but with the endpoints reversed.
	 * @return a reversed copy of the curve
	 */
	@Override
	public PhiCurve reverse() {
		PhiCurve pc = new PhiCurve(p1, p0, R);
		return pc;
	}

	/**
	 * Get the phi index of the phi grid
	 *
	 * @return the phi index
	 */
	public int getPhiIndex() {
		return phiIndex;
	}


	@Override
	public UnivariateFunction getThetaFunction() {
		return new UnivariateFunction() {
			@Override
			public double value(double t) {
				double dTheta = sv1.theta - sv0.theta;
				return sv0.theta + dTheta * t;
			}
		};
	}

	@Override
	public UnivariateFunction getPhiFunction() {
		return new UnivariateFunction() {
			@Override
			public double value(double t) {
				return phi;
			}
		};
	}

	@Override
	public double pathLength() {
		double dTheta = Math.abs(sv1.theta - sv0.theta);
		return R * dTheta;
	}


	@Override
	public BaseCurve[] split(double t) {
		// TODO Auto-generated method stub
		return null;
	}


}
