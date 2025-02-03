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
        // Use modulo to wrap the angle within [-π, π]
        angle = angle % (2 * Math.PI);

        if (angle > Math.PI) {
            angle -= 2 * Math.PI;
        } else if (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }

        return angle;
    }

    // Test the normalizeAngle method
    public static void main(String[] args) {
        double[] testAngles = {0, Math.PI, -Math.PI, 2 * Math.PI, -2 * Math.PI, 3 * Math.PI, -3 * Math.PI};

        for (double angle : testAngles) {
            System.out.printf("Original: %f, Normalized: %f%n", angle, normalizeAngle(angle));
        }
    }
}
