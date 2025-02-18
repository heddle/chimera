package cnuphys.chimera.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;

public class SphericalLoopUtils {

    /**
     * Computes the enclosed area of the spherical loop.
     * @param loop The spherical loop.
     * @return The enclosed area.
     */
    public static double computeEnclosedArea(ISegment loop) {
        // retrieve the theta function and the derivative of phi.
        UnivariateFunction theta = loop.getThetaFunction();
        UnivariateFunction dphi = loop.getDPhiFunction();

        // We will use Simpson's rule with N subintervals.
        // (N must be even for Simpson's rule.)
        final int N = 10000; // you can adjust for desired accuracy (must be even)
        double h = 1.0 / N;
        double sum = 0.0;

        // Simpson's rule: approximate the integral f(t) dt from 0 to 1.
        // Our integrand is f(t) = (1 - cos(theta(t))) * dphi/dt.
        for (int i = 0; i <= N; i++) {
            double t = i * h;
            double f = (1.0 - Math.cos(theta.value(t))) * dphi.value(t);
            if (i == 0 || i == N) {
                sum += f;
            } else if (i % 2 == 1) {
                sum += 4.0 * f;
            } else {
                sum += 2.0 * f;
            }
        }
        double area = (h / 3.0) * sum;
        
        // Return the absolute value to ensure a positive area.
        return Math.abs(area);
    }
}