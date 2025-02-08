package cnuphys.chimera.grid;

public class ClosestPointOnFace {
    
	   /**
     * Returns the closest point on the rectangular face (defined by its four corners)
     * to the origin. The corners must be ordered so that corner 0 is opposite corner 2,
     * and corner 1 is opposite corner 3.
     *
     * @param faceCorners a 4x3 array of the face's corner coordinates
     * @return a double array representing the closest point on the face to the origin
     */
    public static double[] closestPointOnFace(double[][] faceCorners) {
        // Step 1: Compute the plane's normal vector.
        double[] v1 = subtract(faceCorners[1], faceCorners[0]);
        double[] v2 = subtract(faceCorners[3], faceCorners[0]);
        double[] normal = crossProduct(v1, v2);
        normalize(normal);
        
        // Step 2: Determine the plane equation constant.
        double d = dotProduct(normal, faceCorners[0]);
        
        // Step 3: Project the origin onto the plane.
        // Since the normal is unit length, the projection is d * normal.
        double[] projected = scale(normal, d);
        
        // Step 4: Check if the projection is inside the face.
        if (isInsideFace(projected, faceCorners)) {
            return projected;
        }
        
//        // Step 5: Otherwise, find the closest point on the face's boundary.
//        double[] closest = null;
//        double minDist = Double.MAX_VALUE;
//        for (int i = 0; i < 4; i++) {
//            double[] p1 = faceCorners[i];
//            double[] p2 = faceCorners[(i + 1) % 4];
//            double[] closestOnEdge = closestPointOnSegment(projected, p1, p2);
//            double dist = distance(closestOnEdge, new double[]{0, 0, 0});
//            if (dist < minDist) {
//                minDist = dist;
//                closest = closestOnEdge;
//            }
//        }
//        return closest;
        
        return null;
    }    
    // Helper functions
    
    private static double[] subtract(double[] a, double[] b) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }
    
    private static double dotProduct(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
    
    private static double[] crossProduct(double[] a, double[] b) {
        return new double[]{
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        };
    }
    
    private static void normalize(double[] v) {
        double length = Math.sqrt(dotProduct(v, v));
        v[0] /= length;
        v[1] /= length;
        v[2] /= length;
    }
    
    private static boolean isInsideFace(double[] point, double[][] corners) {
        double[] u = subtract(corners[1], corners[0]);
        double[] v = subtract(corners[3], corners[0]);

        double[] w = subtract(point, corners[0]);
        
        double u_dot_u = dotProduct(u, u);
        double u_dot_v = dotProduct(u, v);
        double v_dot_v = dotProduct(v, v);
        double u_dot_w = dotProduct(u, w);
        double v_dot_w = dotProduct(v, w);

        double denom = u_dot_u * v_dot_v - u_dot_v * u_dot_v;
        double s = (u_dot_w * v_dot_v - v_dot_w * u_dot_v) / denom;
        double t = (v_dot_w * u_dot_u - u_dot_w * u_dot_v) / denom;

        return s >= 0 && s <= 1 && t >= 0 && t <= 1;
    }

    private static double[] scale(double[] v, double s) {
        return new double[]{v[0] * s, v[1] * s, v[2] * s};
    }
    
    private static double[] closestPointOnSegment(double[] p, double[] a, double[] b) {
        double[] ab = subtract(b, a);
        double[] ap = subtract(p, a);
        double t = dotProduct(ap, ab) / dotProduct(ab, ab);
        t = Math.max(0, Math.min(1, t));
        return new double[]{a[0] + t * ab[0], a[1] + t * ab[1], a[2] + t * ab[2]};
    }

    private static double distance(double[] a, double[] b) {
        return Math.sqrt(Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2) + Math.pow(a[2] - b[2], 2));
    }
}
