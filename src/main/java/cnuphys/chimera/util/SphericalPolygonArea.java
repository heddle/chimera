package cnuphys.chimera.util;

import java.util.List;

public class SphericalPolygonArea {

    private static final double TWO_PI = 2 * Math.PI;

    /**
     * Computes the spherical area (as a fraction of the full sphere) of a polygon defined 
     * by vertices given in ThetaPhi coordinates.
     *
     * <p>The area returned is the smaller of the complementary regions of the sphere.</p>
     *
     * @param vertices the list of vertices (must have at least 3 vertices)
     * @return the area fraction of the sphere (between 0 and 0.5)
     * @throws IllegalArgumentException if fewer than 3 vertices are provided
     */
    public static double computeSphericalArea(List<ThetaPhi> vertices) {
        int n = vertices.size();
        if (n < 3) {
            throw new IllegalArgumentException("A polygon must have at least 3 vertices.");
        }

        double sum = 0;
        double prevColat = 0;
        double prevAz = 0;
        double firstColat = 0;
        double firstAz = 0;

        for (int i = 0; i < n; i++) {
            ThetaPhi vertex = vertices.get(i);
            // Use ThetaPhi#getLatitude() to obtain latitude (in radians)
            double lat = vertex.getLatitude(); // latitude = PI/2 - theta
            double lon = vertex.getPhi();        // phi already in radians

            // Compute "colatitude" using a haversine-like formula.
            double sinHalfLat = Math.sin(lat / 2);
            double sinHalfLon = Math.sin(lon / 2);
            double cosLat = Math.cos(lat);
            double numerator = Math.sqrt(sinHalfLat * sinHalfLat + cosLat * sinHalfLon * sinHalfLon);
            double denominator = Math.sqrt(1 - sinHalfLat * sinHalfLat - cosLat * sinHalfLon * sinHalfLon);
            double colat = 2 * Math.atan2(numerator, denominator);

            // Compute the azimuth.
            double az;
            if (lat >= Math.PI / 2) {
                az = 0;
            } else if (lat <= -Math.PI / 2) {
                az = Math.PI;
            } else {
                az = Math.atan2(Math.cos(lat) * Math.sin(lon), Math.sin(lat));
                az = normalizeAngle(az);
            }

            if (i == 0) {
                firstColat = colat;
                firstAz = az;
            } else {
                double deltaAz = Math.abs(az - prevAz);
                // Adjust deltaAz for periodicity.
                deltaAz = (deltaAz / Math.PI) - 2 * Math.ceil((deltaAz / Math.PI - 1) / 2.0);
                double term = (1 - Math.cos(prevColat + (colat - prevColat) / 2)) * Math.PI 
                        * deltaAz * Math.signum(az - prevAz);
                sum += term;
            }
            prevColat = colat;
            prevAz = az;
        }
        // Close the polygon by linking the last vertex with the first.
        sum += (1 - Math.cos(prevColat + (firstColat - prevColat) / 2)) * (firstAz - prevAz);

        double areaFraction = Math.abs(sum) / (4 * Math.PI);
        // Return the smaller of the two complementary areas.
        return Math.min(areaFraction, 1 - areaFraction);
    }

    /**
     * Normalizes an angle (in radians) to the range [0, 2π).
     *
     * @param angle the angle in radians
     * @return the normalized angle in radians
     */
    private static double normalizeAngle(double angle) {
        angle %= TWO_PI;
        if (angle < 0) {
            angle += TWO_PI;
        }
        return angle;
    }
}
