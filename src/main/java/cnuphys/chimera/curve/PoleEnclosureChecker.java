
package cnuphys.chimera.curve;

import java.util.List;

public class PoleEnclosureChecker {

    private static final double TOL = 1e-6; // Tolerance for pole proximity
    private static final double WINDING_TOL = 0.1; // Tolerance for full winding detection

    /**
     * Checks whether a set of connected curves encloses a pole using the winding number method.
     *
     * @param curves List of GeneralCurve objects forming a closed loop
     * @return 0 if no pole is enclosed,
     *         1 if the north pole (theta = 0) is enclosed,
     *         2 if the south pole (theta = PI) is enclosed,
     *        -1 if the north pole is on a curve within TOL,
     *        -2 if the south pole is on a curve within TOL.
     */
    public static int checkPoleEnclosure(List<GeneralCurve> curves) {
        double totalWinding = 0.0;
        double thetaSum = 0.0;
        int thetaCount = 0;

        for (GeneralCurve curve : curves) {
            if (containsPole(curve, 0)) return -1; // North pole is on a curve
            if (containsPole(curve, Math.PI)) return -2; // South pole is on a curve

            totalWinding += computeWindingNumber(curve);
            thetaSum += computeAverageTheta(curve);
            thetaCount++;
        }

        double avgTheta = thetaSum / thetaCount;

        // Check if the winding number is close to ±2π
        if (Math.abs(totalWinding - 2 * Math.PI) < WINDING_TOL) {
            return (avgTheta < Math.PI / 2) ? 1 : 2; // Check whether it's in the north or south
        } else if (Math.abs(totalWinding + 2 * Math.PI) < WINDING_TOL) {
            return (avgTheta < Math.PI / 2) ? 1 : 2; // Check whether it's in the north or south
        }

        return 0; // No pole enclosed
    }

    /**
     * Checks if a curve contains a given pole within a tolerance.
     *
     * @param curve The GeneralCurve object
     * @param poleTheta The theta coordinate of the pole (0 for north, PI for south)
     * @return true if the pole lies on the curve within tolerance, false otherwise
     */
    private static boolean containsPole(GeneralCurve curve, double poleTheta) {
        for (double t = 0; t <= 1; t += 0.01) { // Sample points along the curve
            if (Math.abs(curve.theta(t) - poleTheta) < TOL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes the winding number of the curve segment in spherical coordinates.
     * Uses azimuthal angle differences to track how much the curve winds around the pole.
     *
     * @param curve The GeneralCurve object
     * @return Winding angle contribution of the curve (in radians)
     */
    private static double computeWindingNumber(GeneralCurve curve) {
 
        double totalWinding = 0.0;
        int N = 100;
        double dt = 1.0 / N;
        double prevPhi = curve.phi(0);

        for (int i = 1; i <= N; i++) {
            double t = i * dt;
            double currPhi = curve.phi(t);
            double deltaPhi = unwrapAngle(currPhi - prevPhi);
            totalWinding += deltaPhi;
            prevPhi = currPhi;
        }

        return totalWinding;
    }

    /**
     * Computes the average theta value along a curve segment.
     * This is used to determine if the loop is mostly in the northern or southern hemisphere.
     *
     * @param curve The GeneralCurve object
     * @return The average theta value along the curve
     */
    private static double computeAverageTheta(GeneralCurve curve) {

        double sumTheta = 0.0;
        int N = 100;
        double dt = 1.0 / N;

        for (int i = 0; i <= N; i++) {
            double t = i * dt;
            sumTheta += curve.theta(t);
        }

        return sumTheta / (N + 1);
    }

    /**
     * Unwraps angle differences to ensure the shortest path is used in azimuthal angle changes.
     *
     * @param deltaPhi The difference between two consecutive azimuthal angles
     * @return The unwrapped azimuthal angle difference
     */
    private static double unwrapAngle(double deltaPhi) {
        if (deltaPhi > Math.PI) {
            deltaPhi -= 2 * Math.PI;
        } else if (deltaPhi < -Math.PI) {
            deltaPhi += 2 * Math.PI;
        }
        return deltaPhi;
    }
}
