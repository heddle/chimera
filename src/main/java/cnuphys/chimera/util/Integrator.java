package cnuphys.chimera.util;

import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.UnivariateFunction;

public class Integrator {
	
	public static double AreaContribution() {

		return 0;
	}
	
	
	/**
	 * Evaluate the area integral of the function r^2 sin(theta) dtheta dphi over the region
	 * @param R the radius of the sphere
	 * @param phi0 the initial phi value of the general curve
	 * @param phi1 the final phi value of the general curve
	 * @param either xo or yo, the initial value of the general curve for
	 * the case of x normal and y normal respectively
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
	
	
	
    public static void main(String[] args) {
        // Define the function to integrate: f(x) = x^2
        UnivariateFunction function = x -> x * x;

        // Create the RombergIntegrator instance
        RombergIntegrator integrator = new RombergIntegrator();

        // Compute the integral from 0 to 2
        double result = integrator.integrate(1000, function, 0, 2);

        // Output the result (expected: 8/3 ≈ 2.6667)
        System.out.println("Integral result: " + result);
    }

}
