package cnuphys.chimera.curve;

import cnuphys.chimera.util.Point3D;

/**
 * A utility class for generating parameterized curves on the sphere
 * that lie on a given face of the cell.
 */
public class CurveUtils {

    /**
     * Returns a parameterized curve on the sphere (of radius R, centered at the origin)
     * and on the given face (which is one of the canonical faces of the cell) that goes from p0 to p1.
     * The returned Curve's getPoint(t) will return points that (within tolerance) lie on both the sphere
     * and the face.
     *
     * @param p0   one intersection point on the sphere (supposed to lie on the face)
     * @param p1   the adjacent intersection point on the sphere (on the same face)
     * @param face the common face index (0–5) indicating which face (see GridSupport)
     * @param R    the sphere radius
     * @return a Curve such that curve.getPoint(t) returns a point on the arc from p0 to p1 (t in [0,1])
     * @throws IllegalArgumentException if p0 or p1 are not on the sphere (within tolerance) or if their
     *                                  face coordinate values differ by more than a tolerance.
     */
    public static Curve getSphereFaceCurve(Point3D.Double p0, Point3D.Double p1, int face, double R) {
        final double tol = 1e-6;  // tolerance for comparing radii and coordinates
        
        // Preliminary check: ensure that p0 and p1 are on the sphere within tolerance.
        double r0 = Math.sqrt(p0.x * p0.x + p0.y * p0.y + p0.z * p0.z);
        double r1 = Math.sqrt(p1.x * p1.x + p1.y * p1.y + p1.z * p1.z);
        if (Math.abs(r0 - R) > tol || Math.abs(r1 - R) > tol) {
            throw new IllegalArgumentException("One or both endpoints are not on the sphere (within tolerance). " +
                    "r0=" + r0 + " r1=" + r1 + " expected R=" + R);
        }
        
        // Determine which coordinate should be constant for the face.
        // For the canonical cell, the mapping is:
        //  - Face 0,1: constant z,
        //  - Face 2,3: constant y,
        //  - Face 4,5: constant x.
        Point3D.Double n; // unit normal to the face
        double h;         // the plane's constant coordinate (snapped from the endpoints)
        switch (face) {
            case 0:
            case 1:
                // Both points should have nearly the same z coordinate.
                if (Math.abs(p0.z - p1.z) > tol) {
                    throw new IllegalArgumentException("Endpoints do not agree on the z coordinate for a z=constant face.");
                }
                h = p0.z;  // snap to p0's z value
                // For a face at constant z, choose downward normal for face 0, upward for face 1.
                n = new Point3D.Double(0, 0, (face == 0 ? -1 : 1));
                break;
            case 2:
            case 3:
                // Both points should have nearly the same y coordinate.
                if (Math.abs(p0.y - p1.y) > tol) {
                    throw new IllegalArgumentException("Endpoints do not agree on the y coordinate for a y=constant face.");
                }
                h = p0.y;  // snap to p0's y value
                // For a face at constant y, choose negative y for face 2, positive y for face 3.
                n = new Point3D.Double(0, (face == 2 ? -1 : 1), 0);
                break;
            case 4:
            case 5:
                // Both points should have nearly the same x coordinate.
                if (Math.abs(p0.x - p1.x) > tol) {
                    throw new IllegalArgumentException("Endpoints do not agree on the x coordinate for an x=constant face.");
                }
                h = p0.x;  // snap to p0's x value
                // For a face at constant x, choose negative x for face 4, positive x for face 5.
                n = new Point3D.Double((face == 4 ? -1 : 1), 0, 0);
                break;
            default:
                throw new IllegalArgumentException("Invalid face index: " + face);
        }
        
        // The plane’s equation is n · x = h.
        // For a sphere centered at the origin, the projection of the origin onto the plane is:
        Point3D.Double c = new Point3D.Double(n.x * h, n.y * h, n.z * h);
        
        // The circle’s radius is given by: rcirc = sqrt(R^2 - h^2).
        final double rcirc = Math.sqrt(R * R - h * h);
        
        // Build an orthonormal basis on the plane.
        // Let u = normalized (p0 - c). Since p0 is on the circle, |p0-c| should be rcirc.
        Point3D.Double u = new Point3D.Double(p0.x - c.x, p0.y - c.y, p0.z - c.z);
        normalize(u);
        
        // Let v = n x u. Then {u, v} is an orthonormal basis for the plane.
        Point3D.Double v = cross(n, u);
        normalize(v);
        
        // Express p1 - c in the {u, v} basis.
        Point3D.Double p1c = new Point3D.Double(p1.x - c.x, p1.y - c.y, p1.z - c.z);
        double alpha = dot(p1c, u);
        double beta  = dot(p1c, v);
        
        // The angle (in radians) from p0 to p1 on the circle is given by:
        double theta = Math.atan2(beta, alpha);
        // Adjust theta so that we traverse the shorter arc.
        if (theta < 0) {
            theta += 2 * Math.PI;
        }
        if (theta > Math.PI) {
            theta -= 2 * Math.PI;
        }
        
        final double thetaFinal = theta;
        // Return the Curve. For t in [0, 1], the angle swept is t * theta.
        return new Curve() {
            @Override
            public Point3D.Double getPoint(double t) {
                double angle = t * thetaFinal;
                double cosA = Math.cos(angle);
                double sinA = Math.sin(angle);
                // f(t) = c + rcirc*(cos(angle)*u + sin(angle)*v)
                Point3D.Double pt = new Point3D.Double(
                        c.x + rcirc * (cosA * u.x + sinA * v.x),
                        c.y + rcirc * (cosA * u.y + sinA * v.y),
                        c.z + rcirc * (cosA * u.z + sinA * v.z)
                );
                return pt;
            }
        };
    }
    
    // --- Helper methods ---
    
    private static double dot(Point3D.Double a, Point3D.Double b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
    
    private static Point3D.Double cross(Point3D.Double a, Point3D.Double b) {
        return new Point3D.Double(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x
        );
    }
    
    private static void normalize(Point3D.Double a) {
        double mag = Math.sqrt(a.x * a.x + a.y * a.y + a.z * a.z);
        if (mag < 1e-12) {
            throw new IllegalArgumentException("Cannot normalize a zero-length vector.");
        }
        a.x /= mag;
        a.y /= mag;
        a.z /= mag;
    }
}
