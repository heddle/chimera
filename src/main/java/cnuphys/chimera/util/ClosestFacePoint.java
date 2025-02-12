package cnuphys.chimera.util;

public class ClosestFacePoint {

    // Tolerance used for floating-point comparisons.
    public static final double TOL = 1.0e-6;

    /**
     * Checks if all the points lie on a common plane (to within the given tolerance).
     * 
     * @param points a 4x3 array where points[i] = {x, y, z} for i=0,..,3.
     * @param tol the tolerance value
     * @return true if all points are coplanar, false otherwise.
     */
    public static boolean arePointsCoplanar(double[][] points, double tol) {
        // Use the first three points to compute the plane.
        double[] p0 = points[0];
        double[] p1 = points[1];
        double[] p2 = points[2];

        // Compute vectors along two edges.
        double[] u = subtract(p1, p0);  // p1 - p0
        double[] v = subtract(p2, p0);  // p2 - p0

        // Compute the normal vector (cross product).
        double[] n = cross(u, v);
        double normN = norm(n);
        if (normN < tol) {
            // The first three points are (nearly) collinear.
            return false;
        }
        
        // Normalize the normal vector.
        n = multiply(n, 1.0 / normN);
        
        // Check that every point lies in the plane defined by p0 and n.
        for (int i = 0; i < points.length; i++) {
            double[] diff = subtract(points[i], p0);
            double d = dot(diff, n);
            if (Math.abs(d) > tol) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds and returns the point on the rectangular face (defined by the four points)
     * that is closest to the origin.
     *
     * Assumes that the points are given in order such that:
     *   - p0 = points[0]
     *   - p1 = points[1] is adjacent to p0,
     *   - p3 = points[3] is the other vertex adjacent to p0.
     *
     * The rectangle is parametrized as:
     *     P(s, t) = p0 + s*(p1 - p0) + t*(p3 - p0),  with 0 <= s <= 1, 0 <= t <= 1.
     *
     * @param points a 4x3 array where each row is a point {x, y, z}.
     * @return the 3D point (as an array of length 3) on the face that is closest to the origin.
     */
    public static double[] closestPointOnFaceToOrigin(double[][] points) {
        double[] p0 = points[0];
        double[] p1 = points[1];
        double[] p3 = points[3];
        
        // Compute the edge vectors.
        double[] u = subtract(p1, p0);  // first edge from p0 to p1
        double[] v = subtract(p3, p0);  // second edge from p0 to p3
        
        // Compute the normal of the plane of the rectangle.
        double[] n = cross(u, v);
        double normN = norm(n);
        if (normN < TOL) {
            throw new IllegalArgumentException("The provided points do not form a valid rectangle (degenerate case).");
        }
        n = multiply(n, 1.0 / normN);
        
        // Project the origin (0,0,0) onto the plane.
        double[] origin = {0.0, 0.0, 0.0};
        double[] p0ToOrigin = subtract(origin, p0);
        double d = dot(p0ToOrigin, n);
        // Q is the projection of the origin onto the plane.
        double[] Q = subtract(origin, multiply(n, d));
        
        // Express Q in the rectangle's coordinate system.
        double[] Qp0 = subtract(Q, p0);
        double uDotU = dot(u, u);
        double vDotV = dot(v, v);
        
        // Compute parameters s and t (for an ideal rectangle without clamping).
        double s = dot(Qp0, u) / uDotU;
        double t = dot(Qp0, v) / vDotV;
        
        // Clamp s and t to lie within [0, 1] to ensure Q lies on the rectangle.
        double sClamped = clamp(s, 0.0, 1.0);
        double tClamped = clamp(t, 0.0, 1.0);
        
        // Compute the closest point on the rectangle.
        double[] closest = add(p0, add(multiply(u, sClamped), multiply(v, tClamped)));
        return closest;
    }

    // ========================
    // Helper Vector Functions
    // ========================

    public static double[] subtract(double[] a, double[] b) {
        return new double[]{ a[0] - b[0], a[1] - b[1], a[2] - b[2] };
    }
    
    public static double[] add(double[] a, double[] b) {
        return new double[]{ a[0] + b[0], a[1] + b[1], a[2] + b[2] };
    }
    
    public static double[] multiply(double[] a, double scalar) {
        return new double[]{ a[0] * scalar, a[1] * scalar, a[2] * scalar };
    }
    
    public static double dot(double[] a, double[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
    }
    
    public static double[] cross(double[] a, double[] b) {
        return new double[]{
            a[1]*b[2] - a[2]*b[1],
            a[2]*b[0] - a[0]*b[2],
            a[0]*b[1] - a[1]*b[0]
        };
    }
    
    public static double norm(double[] a) {
        return Math.sqrt(dot(a, a));
    }
    
    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
    
    // ========================
    // Main method for testing
    // ========================
    
    public static void main(String[] args) {
        // Define 4 points that form a rectangle.
        // For example, consider a rectangle in the plane z = 0.
        double[][] points = {
            { 1.0, 1.0, 0.0 },   // p0
            { 3.0, 1.0, 0.0 },   // p1 (adjacent to p0)
            { 3.0, 4.0, 0.0 },   // p2 (diagonally opposite to p0)
            { 1.0, 4.0, 0.0 }    // p3 (other vertex adjacent to p0)
        };
        
        // First, check that the points are coplanar.
        if (!arePointsCoplanar(points, TOL)) {
            System.out.println("The points do not lie on a single plane.");
            return;
        } else {
            System.out.println("The points are coplanar.");
        }
        
        // Now, find the closest point on the face to the origin.
        double[] closestPoint = closestPointOnFaceToOrigin(points);
        System.out.printf("The closest point on the rectangle to the origin is: (%.6f, %.6f, %.6f)%n",
                          closestPoint[0], closestPoint[1], closestPoint[2]);
    }
}
