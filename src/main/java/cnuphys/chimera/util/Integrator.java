package cnuphys.chimera.util;

import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;

import cnuphys.chimera.curve.GeneralCurve;

public class Integrator {


	  /**
     * Computes the enclosed spherical area for a closed loop formed by a list of GeneralCurve segments.
     * Each segment provides UnivariateFunction objects for theta(t), phi(t) and dphi/dt.
     *
     * The formula used is:
     *     A = R^2 * sum_{i=0}^{N-1} ∫_0^1 [1 - cos(theta_i(t))] * (dphi_i/dt)(t) dt,
     *
     * where the integration is performed over each segment and the results summed.
     *
     * @param curves  List of GeneralCurve segments forming a closed loop.
     * @return        The enclosed spherical area.
     */
    public static double computeEnclosedArea(List<GeneralCurve> curves) {
        if (curves == null || curves.isEmpty()) {
            return 0;
        }
        
        // Assume all curves are on the same sphere; get the radius from the first curve.
        double R = curves.get(0).getRadius();
        
        // Create a Romberg integrator.
        RombergIntegrator integrator = new RombergIntegrator();
        
        double totalIntegral = 0.0;
        
        // Loop over each segment.
        for (final GeneralCurve curve : curves) {
            // Define the integrand f(t) = [1 - cos(theta(t))] * (dphi/dt)(t).
            UnivariateFunction integrand = new UnivariateFunction() {
                @Override
                public double value(double t) {
                    double theta = curve.getThetaFunction().value(t);
                    double dphi = curve.getDPhiFunction().value(t);
                    return (1 - Math.cos(theta)) * dphi;
                }
            };
            
            // Integrate over t from 0 to 1.
            double segmentIntegral = integrator.integrate(1000000, integrand, 0.0, 1.0);
            totalIntegral += segmentIntegral;
        }
        
        // Multiply by R^2 to get the area.
        return R * R * totalIntegral;
    }


	/**
	 * Evaluate the area integral of the function r^2 sin(theta) dtheta dphi over
	 * the region This is the contribution from a curve
	 *
	 * @param R      the radius of the sphere
	 * @param phi0   the initial phi value of the general curve
	 * @param phi1   the final phi value of the general curve
	 * @param either xo or yo, the initial value of the general curve for the case
	 *               of x normal and y normal respectively
	 * @return the contribution to the area integral
	 */
	public static double areaIntegral(final double R, final double phi0, final double phi1, final double v0) {

		final double rr = Math.hypot(phi1, v0);

		// Define the function to integrate
		UnivariateFunction integrand = new UnivariateFunction() {
			@Override
			public double value(double t) {
				double phi_t = (1 - t) * phi0 + t * phi1;
				double dcosTheta_dt = (rr / R) * Math.sin(phi_t) * (phi1 - phi0);
				return phi_t * dcosTheta_dt;
			}
		};

		// Use Romberg integration to evaluate the integral
		RombergIntegrator integrator = new RombergIntegrator();
		return R * R * integrator.integrate(1000, integrand, 0, 1);
	}

}
