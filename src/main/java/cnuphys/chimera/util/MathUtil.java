package cnuphys.chimera.util;

public class MathUtil {

    // Private constructor to prevent instantiation
    private MathUtil() {}

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
	 * Converts a double array to a float array. This is useful for interfacing with OpenGL.
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


    // Test the normalizeAngle method
    public static void main(String[] args) {
        double[] testAngles = {0, Math.PI, -Math.PI, 2 * Math.PI, -2 * Math.PI, 3 * Math.PI, -3 * Math.PI, 10, -10};

        for (double angle : testAngles) {
            System.out.printf("Original: %f, Normalized: %f%n", angle, normalizeAngle(angle));
        }
    }
}
