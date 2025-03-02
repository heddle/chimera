package cnuphys.mosaic.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;

import cnuphys.mosaic.util.MathUtil;
import cnuphys.mosaic.util.Point3D;

/**
 * 
 */
public class PhiCurve extends BaseCurve {
	
	private double phi; // the constant phi value
	
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
		
		phi = sv0.phi;
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
		double dTheta = MathUtil.normalizedAngleDifference(sv0.theta, sv1.theta);
		return R * dTheta;
	}

}
