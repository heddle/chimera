package cnuphys.mosaic.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;

import cnuphys.mosaic.frame.Mosaic;
import cnuphys.mosaic.grid.CartesianGrid;
import cnuphys.mosaic.patch.Patch;
import cnuphys.mosaic.patch.Tuple;
import cnuphys.mosaic.util.MathUtil;
import cnuphys.mosaic.util.Point3D;
import cnuphys.mosaic.util.SphericalVector;

/**
 * A curve on the surface of the sphere that is constant in theta and linear in phi.
 */
public class ThetaCurve extends BaseCurve {
	
	private double theta; // the constant theta value
	
	private double dPhi;
	
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
	public static ThetaCurve createThetaCurve(Patch prePatch, double theta, double phi0, double phi1, double R) {

		phi0 = MathUtil.normalizeAngle(phi0);
		phi1 = MathUtil.normalizeAngle(phi1);
				
		Point3D.Double p0 = new SphericalVector(theta, phi0, R).toCartesian();
		Point3D.Double p1 = new SphericalVector(theta, phi1, R).toCartesian();
		ThetaCurve tc = new ThetaCurve(p0, p1, R);
		
		System.err.println("flip: " + tc.prepatchContainsMidpoint(prePatch));
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
		double dPhi = MathUtil.normalizedAngleDifference(sv0.phi, sv1.phi);
		return R * Math.sin(theta) * dPhi;
	}
	
	private boolean prepatchContainsMidpoint(Patch prepatch) {
		double phi = phi(0.5);
		double theta = sv0.theta;
		double R = getRadius();
		
		double x = R * Math.sin(theta) * Math.cos(phi);
		double y = R * Math.sin(theta) * Math.sin(phi);
		double z = R * Math.cos(theta);
		

		CartesianGrid grid = Mosaic.getInstance().getCartesianGrid();
		int ix = grid.getXGrid().locateInterval(x);
		int iy = grid.getYGrid().locateInterval(y);
		int iz = grid.getZGrid().locateInterval(z);
		Tuple ft = prepatch.getTuple();
		boolean match = ft.matches(ix, iy, iz);
		return match;
	}

}
