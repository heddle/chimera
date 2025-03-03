package cnuphys.mosaic.util;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;

public class MathUtil {

	// Private constructor to prevent instantiation
	private MathUtil() {
	}

	/**
	 * Normalizes an azimuthal angle to be within the range [-π, π].
	 *
	 * @param angle the angle in radians to normalize
	 * @return the normalized angle in the range [-π, π]
	 */
	public static double normalizeAngle(double angle) {

		while (angle > Math.PI) {
			angle -= 2 * Math.PI;
		}
		while (angle <= -Math.PI) {
			angle += 2 * Math.PI;
		}

		return angle;
	}

	/**
	 * Normalizes an angle difference to be within the range [0, 2π].
	 *
	 * @param angle1 the first angle in radians
	 * @param angle2 the second angle in radians
	 * @return the normalized angle difference in the range [0, 2π]
	 */
	public static double normalizedAngleDifference(double angle1, double angle2) {
		angle1 = normalizeAngle(angle1);
		angle2 = normalizeAngle(angle2);
		double diff = normalizeAngle(angle1 - angle2);

		if (diff < 0) {
			diff += 2 * Math.PI;
		}
		return diff;
	}

	/**
	 * Converts a double array to a float array. This is useful for interfacing with
	 * OpenGL.
	 *
	 * @param doubleArray the double array to convert
	 * @return the float array
	 */
	public static float[] toFloatArray(double[] doubleArray) {
		float[] floatArray = new float[doubleArray.length];
		for (int i = 0; i < doubleArray.length; i++) {
			floatArray[i] = (float) doubleArray[i];
		}
		return floatArray;
	}

	/**
	 * Compute the t value for a given value and tolerance. If the value is within
	 * the tolerance of the function, the t value is set to the t value that
	 * corresponds to the value. If it cannot be found in [0,1), the t value is set
	 * to Double.NaN.
	 *
	 * @param func      the parameterized function
	 * @param value     the value to search for
	 * @param tolerance the tolerance
	 * @return the t value
	 */
	public static double computeT(UnivariateFunction func, double value, double tmin, double tmax, double tolerance) {
		BrentSolver solver = new BrentSolver(tolerance);
		UnivariateFunction rootFunction = t -> func.value(t) - value;

		// Search within smaller subintervals to ensure a valid root bracket
		int numIntervals = 100; // More intervals for better root detection
		double step = (tmax - tmin) / numIntervals;

		for (int i = 0; i < numIntervals; i++) {
			double t1 = tmin + i * step;
			double t2 = tmin + (i + 1) * step;
			double f1 = rootFunction.value(t1);
			double f2 = rootFunction.value(t2);

			// Check for sign change in this subinterval
			if (f1 * f2 <= 0) {
				try {
					return solver.solve(100, rootFunction, t1, t2);
				} catch (Exception e) {
					// If solver fails, continue to the next interval
					continue;
				}
			}
		}

		// No valid solution found in [0,1)
		return Double.NaN;
	}

	/**
	 * Finds the point where a function transitions from satisfying a test to not
	 * @param phi the function
	 * @param test the test function
	 * @param tol the tolerance
	 * @return the point where the function transitions
	 */
	public static double findTogglePoint(UnivariateFunction phi, java.util.function.Predicate<Double> test,
			double tol) {
		double tLow = 0.0;
		double tHigh = 1.0;
		boolean initialTest = test.test(phi.value(tLow));

// Binary search for the transition point
		while (tHigh - tLow > tol) {
			double tMid = (tLow + tHigh) / 2.0;
			boolean midTest = test.test(phi.value(tMid));

			if (midTest == initialTest) {
				tLow = tMid; // Move right
			} else {
				tHigh = tMid; // Move left
			}
		}

		return (tLow + tHigh) / 2.0; // Best estimate of the toggle point
	}

	// Test the normalizeAngle method
	public static void main(String[] args) {
		double[] testAngles = { 0, Math.PI, -Math.PI, 2 * Math.PI, -2 * Math.PI, 3 * Math.PI, -3 * Math.PI, 10, -10 };

		for (double angle : testAngles) {
			System.out.printf("Original: %f, Normalized: %f%n", angle, normalizeAngle(angle));
		}

		for (int i = 0; i < testAngles.length - 1; i++) {
			System.out.printf("Angle difference between %f and %f: %f%n", testAngles[i], testAngles[i + 1],
					normalizedAngleDifference(testAngles[i], testAngles[i + 1]));
		}
	}
}
