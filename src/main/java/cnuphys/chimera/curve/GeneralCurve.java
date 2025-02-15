package cnuphys.chimera.curve;

import cnuphys.chimera.grid.Cell;
import cnuphys.chimera.util.Point3D;
import cnuphys.chimera.util.SphericalVector;
import cnuphys.chimera.util.Point3D.Double;

public class GeneralCurve implements Curve {

    private int face;
    
    private Point3D.Double p0;
    private Point3D.Double p1;
    private SphericalVector sv0;
    private SphericalVector sv1;
    private double R;

    public GeneralCurve(Cell cell, int face, Point3D.Double p0, Point3D.Double p1, double R) {
        this.face = face;
        this.p0 = p0;
        this.p1 = p1;
        this.R = R;
        
        sv0 = new SphericalVector(p0);
        sv1 = new SphericalVector(p1);
    }

    /**
     * Helper method that interpolates between two angles a0 and a1
     * ensuring that the shortest angular distance is used.
     */
    private double interpolateAngle(double a0, double a1, double t) {
        double d = a1 - a0;
        // Adjust if the difference is not the shortest route.
        if (d > Math.PI) {
            d -= 2 * Math.PI;
        } else if (d < -Math.PI) {
            d += 2 * Math.PI;
        }
        return a0 + t * d;
    }

    @Override
    public Double getPoint(double t) {
        // For face 0 and 1, the face is horizontal (normal in z).
        if (face == 0 || face == 1) {
            // Interpolate the azimuthal angle phi using the helper method.
            double phi = interpolateAngle(sv0.phi, sv1.phi, t);
            double zo = p0.z;  // constant
            double rho = Math.sqrt(R * R - zo * zo);
            double x = rho * Math.cos(phi);
            double y = rho * Math.sin(phi);
            return new Point3D.Double(x, y, zo);
        }
        
        // For face 4 and 5, the face has constant x (normal in x).
        else if (face == 4 || face == 5) {
            double xo = p0.x;  // constant x coordinate
            double r = Math.sqrt(R * R - xo * xo);
            // Compute the effective angular coordinate in the (y,z) plane.
            double angle0 = Math.atan2(p0.z, p0.y);
            double angle1 = Math.atan2(p1.z, p1.y);
            double phi = interpolateAngle(angle0, angle1, t);
            double y = r * Math.cos(phi);
            double z = r * Math.sin(phi);
            return new Point3D.Double(xo, y, z);
        }
        
        // For face 2 and 3, the face has constant y (normal in y).
        else if (face == 2 || face == 3) {
            double yo = p0.y;  // constant y coordinate
            double r = Math.sqrt(R * R - yo * yo);
            // Compute the effective angular coordinate in the (x,z) plane.
            double angle0 = Math.atan2(p0.z, p0.x);
            double angle1 = Math.atan2(p1.z, p1.x);
            double phi = interpolateAngle(angle0, angle1, t);
            double x = r * Math.cos(phi);
            double z = r * Math.sin(phi);
            return new Point3D.Double(x, yo, z);
        }
        else {
            System.err.println("Fatal error: invalid face number: " + face);
            System.exit(1);
        }
        return null;  // unreachable, but needed for compilation.
    }

    /**
     * Get the points of the curve as a polyline for jOGL 3D drawing.
     * @param n the number of points
     * @return the polyline points
     */
    public float[] getPolyline(int n) {
        double dt = 1.0 / (n - 1);
        float[] points = new float[3 * n];
        
        for (int i = 0; i < n; i++) {
            double t = i * dt;
            Point3D.Double p = getPoint(t);
            points[3 * i] = (float) p.x;
            points[3 * i + 1] = (float) p.y;
            points[3 * i + 2] = (float) p.z;
        }
        return points;
    }
}
