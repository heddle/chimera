package cnuphys.mosaic.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;

import cnuphys.mosaic.frame.Mosaic;
import cnuphys.mosaic.grid.Cell;
import cnuphys.mosaic.grid.SphericalGrid;
import cnuphys.mosaic.util.Point3D;

/**
 * Represents a general curve in 3D space. A general curve of type GENERAL will be on the face of a
 * rectangular prism (cell), and on the surface of the sphere of radius R. It is
 * not a great circle.
 */
public class GeneralCurve extends BaseCurve {


	private static final int INTEGRATION_MAX_EVAL = 1000;

	// The face number of the cell in the range [0, 5]
	//only relevant for GENERAL curves. By construction they are simultaneously on
	//a face of a cell and on the surface of a sphere.
	private int face;



	/**
	 * Constructs a curve on the face of a cell and on the surface of the sphere.
	 *
	 * @param cell      the cell that the curve is on
	 * @param face      the face of the cell in the range [0, 5] (0, 1: Z-normal; 2,
	 *                  3: Y-normal; 4, 5: X-normal
	 * @param p0        the starting point of the curve
	 * @param p1        the ending point of the curve
	 * @param R         the radius of the sphere
	 */
	public GeneralCurve(Cell cell, int face, Point3D.Double p0, Point3D.Double p1, double R) {
		super(p0, p1, R);
		this.face = face;
	}

	/**
	 * Get the path length of the curve
	 *
	 * @return the path length
	 */
	@Override
	public double pathLength() {
		UnivariateFunction integrand = t -> {
			double theta = theta(t);
			double dPhi = getDPhiFunction().value(t);
			return R * Math.sqrt(Math.pow(getDThetaFunction().value(t), 2) + Math.pow(Math.sin(theta) * dPhi, 2));
		};

		SimpsonIntegrator integrator = new SimpsonIntegrator();
		return integrator.integrate(INTEGRATION_MAX_EVAL, integrand, 0, 1);
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
					if (arg > 1) {
						arg = 1;
					}
					if (arg < -1) {
						arg = -1;
					}
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
					if (arg > 1) {
						arg = 1;
					}
					if (arg < -1) {
						arg = -1;
					}
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
	

	
	
}
