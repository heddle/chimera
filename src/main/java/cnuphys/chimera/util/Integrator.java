package cnuphys.chimera.util;

import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.UnivariateFunction;

public class Integrator {
	
	
	
	
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
