package cnuphys.chimera.curve;

import cnuphys.chimera.util.Point3D;

public interface Curve {
    /**
     * Returns a point on the curve for a given parameter t (0 <= t <= 1).
     */
    Point3D.Double getPoint(double t);
}
