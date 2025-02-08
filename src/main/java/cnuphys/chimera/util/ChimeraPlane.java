package cnuphys.chimera.util;

/**
 * A class representing a plane defined by the equation
 * Ax + By + Cz + D = 0.
 * <p>
 * The plane is defined by three points (p0, p1, p2) and maintains
 * a rotation matrix (rmat) used to align the plane's normal with the z-axis.
 * </p>
 */
public class ChimeraPlane {

    // Constants used for tolerance and limiting values.
    private static final double TOL = 1e-8;

    // Plane parameters for the equation Ax + By + Cz + D = 0.
    public double A = 0.0;
    public double B = 0.0;
    public double C = 0.0;
    public double D = 0.0;

    // Points used to create the plane.
    public Point3D.Double p0 = new Point3D.Double();
    public Point3D.Double p1 = new Point3D.Double();
    public Point3D.Double p2 = new Point3D.Double();

    // The rotation matrix (or rotation representation) used to align the normal with the z-axis.
    public ChimeraRotation rmat = new ChimeraRotation();

	/**
	 * Constructs a plane using three points.
	 *
	 * @param p0 One point of the plane.
	 * @param p1 A second point of the plane.
	 * @param p2 A third point of the plane.
	 */
	public ChimeraPlane(Point3D.Double p0, Point3D.Double p1, Point3D.Double p2) {
		setPlane(p0, p1, p2);
	}

	/**
	 * Default constructor. Initializes the plane with the x-y plane.
	 */
	public ChimeraPlane() {
		setPlane(new Point3D.Double(0.0, 0.0, 0.0), new Point3D.Double(1.0, 0.0, 0.0),
				new Point3D.Double(0.0, 1.0, 0.0));
	}
 
    /**
     * Sets the plane using three points. This method computes the plane's
     * parameters, stores the defining points, and computes the rotation matrix.
     *
     * @param p0 One point of the plane.
     * @param p1 A second point of the plane.
     * @param p2 A third point of the plane.
     */
    public void setPlane(Point3D.Double p0, Point3D.Double p1, Point3D.Double p2) {
        getPlaneParameters(p0, p1, p2);
        // Make copies of the points.
        this.p0 = new Point3D.Double(p0.x, p0.y, p0.z);
        this.p1 = new Point3D.Double(p1.x, p1.y, p1.z);
        this.p2 = new Point3D.Double(p2.x, p2.y, p2.z);
        getRotationMatrix();
    }

    /**
     * Computes the plane parameters (A, B, C, D) from three points.
     *
     * @param p0 One point of the plane.
     * @param p1 A second point of the plane.
     * @param p2 A third point of the plane.
     */
    private void getPlaneParameters(Point3D.Double p0, Point3D.Double p1, Point3D.Double p2) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dz1 = p1.z - p0.z;
        
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        double dz2 = p2.z - p0.z;
        
        // Compute cross-product components to get the normal.
        double dyz = dy1 * dz2 - dy2 * dz1;
        double dzx = dz1 * dx2 - dz2 * dx1;
        double dxy = dx1 * dy2 - dx2 * dy1;
        
        A = dyz;
        B = dzx;
        C = dxy;
        
        D = -(A * p0.x + B * p0.y + C * p0.z);
    }

    /**
     * Computes the rotation matrix that aligns the plane's normal with the z-axis.
     */
    public void getRotationMatrix() {
        double len = Math.sqrt(A * A + B * B + C * C);
        // Clamp the ratio to the interval [-1, 1] to avoid numerical issues.
        double ratio = Math.max(-1, Math.min(1, C / len));
        double theta = Math.acos(ratio);

        double phi;
        if ((Math.abs(B) < TOL) && (Math.abs(A) < TOL)) {
            phi = 0;
        } else {
            phi = Math.atan2(B, A);
        }
        
        // Assumes ChimeraRotation.rotationFromAngles sets up the rotation matrix appropriately.
        ChimeraRotation.rotationFromAngles(rmat, phi, ChimeraRotation.CR_Z_AXIS, theta, ChimeraRotation.CR_Y_AXIS);
    }

    /**
     * Computes the point in the plane at which the radial vector is perpendicular.
     * The result is stored in the provided point 'v'.
     *
     * @param v A Point3D.Double that will receive the computed perpendicular point.
     */
    public void getPerpendicular(Point3D.Double v) {
        double lsq = (A * A + B * B + C * C);

        if (Math.abs(A) > TOL) {
            v.x = -(D * A) / lsq;
            v.y = B * v.x / A;
            v.z = C * v.x / A;
        } else if (Math.abs(B) > TOL) {
            v.y = -(D * B) / lsq;
            v.x = A * v.y / B;
            v.z = C * v.y / B;
        } else {
            v.z = -(D * C) / lsq;
            v.x = A * v.z / C;
            v.y = B * v.z / C;
        }
    }

    /**
     * Checks whether a given point lies in the plane (within a small tolerance).
     *
     * @param p The point to test.
     * @return True if the point lies in the plane; otherwise, false.
     */
    public boolean isIn(Point3D.Double p) {
        return Math.abs(deviation(p)) < TOL;
    }

    /**
     * Computes the deviation of a point from the plane.
     *
     * @param p The point to test.
     * @return The deviation (should be near 0 if the point is in the plane).
     */
    public double deviation(Point3D.Double p) {
        return (A * p.x + B * p.y + C * p.z + D);
    }

    /**
     * Checks whether a point is both in the plane and contained by the defining rectangular face.
     * <p>
     * The algorithm checks collinearity along the edges and uses cross-product tests.
     * </p>
     *
     * @param p The point to check.
     * @return True if the point is on the plane and within the rectangular bounds.
     */
    public boolean isInAndContained(Point3D.Double p) {
        if (!isIn(p)) {
            return false;
        }

        // Create working vectors.
        Point3D.Double s1 = new Point3D.Double();
        Point3D.Double s2 = new Point3D.Double();
        Point3D.Double n = new Point3D.Double();
        Point3D.Double s = new Point3D.Double();
        Point3D.Double v = new Point3D.Double();
        Point3D.Double sb = new Point3D.Double();
        Point3D.Double p3 = new Point3D.Double();
        
        // s1 = p1 - p0
        p1.minus(p0, s1);
        // s2 = p2 - p0
        
        p2.minus(p0, s2);
        
        // Construct the fourth corner: p3 = p2 + s1.
        p2.add(s1, p3);
        
        // Check collinearity on the edges.
        if (collinear(p0, p1, p)) {
            return true;
        }
        if (collinear(p0, p2, p)) {
            return true;
        }
        if (collinear(p3, p1, p)) {
            return true;
        }
        if (collinear(p3, p2, p)) {
            return true;
        }
        
        // By construction, s1 x s2 gives an outward normal.
        Point3D.Double.crossProduct(s1, s2, n);
        
        // s = p - p0
        p.minus(p0, s);
        
        // For the point to be inside, (s x s2) should be in the same direction as n.
        Point3D.Double.crossProduct(s, s2, v);
        if (!sameDirection(v, n)) {
            return false;
        }
        
        // Similarly, (s1 x s) should be in the same direction.
        Point3D.Double.crossProduct(s1, s, v);
        if (!sameDirection(v, n)) {
            return false;
        }
        
        // Shift the local origin to the new corner: sb = p - p3.
        p.minus(p3, sb);
        
        // Check cross-product conditions.
        Point3D.Double.crossProduct(sb, s1, v);
        if (!sameDirection(v, n)) {
            return false;
        }
        
        Point3D.Double.crossProduct(s2, sb, v);
        if (!sameDirection(v, n)) {
            return false;
        }
        
        return true;
    }

    /**
     * Checks for collinearity of three points.
     * <p>
     * Returns true if the points are collinear (within tolerance) and the test point lies between
     * the first two endpoints.
     * </p>
     *
     * @param p0    One endpoint.
     * @param p1    The other endpoint.
     * @param ptest The test point.
     * @return True if the points are collinear; otherwise, false.
     */
    public static boolean collinear(Point3D.Double p0, Point3D.Double p1, Point3D.Double ptest) {
        Point3D.Double del1 = new Point3D.Double();
        Point3D.Double del2 = new Point3D.Double();
        
        // del2 = ptest - p0
        ptest.minus(p0, del2);
        double len2 = del2.length();
        if (len2 < TOL) {
            return true;
        }
        
        // del1 = p1 - p0
        p1.minus(p0, del1);
        double len1 = del1.length();
        if (len1 < TOL) {
            return false;
        }
        
        double cost =  del1.dot(del2)/ (len1 * len2);
        return (Math.abs(cost - 1.0) < TOL);
    }

    /**
     * Computes the "length" of the plane, defined as sqrt(A^2 + B^2 + C^2).
     *
     * @return The computed length.
     */
    public double planeLength() {
        return Math.sqrt(A * A + B * B + C * C);
    }

    /**
     * Computes the value of z' (the constant z value in the rotated frame).
     *
     * @return The computed zprime value.
     */
    public double zprime() {
        return -D / planeLength();
    }


    /**
     * Checks whether two vectors have (approximately) the same direction.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return True if the vectors are pointing in nearly the same direction; otherwise, false.
     */
    private static boolean sameDirection(Point3D.Double a, Point3D.Double b) {
        // If either vector is nearly zero, we consider them to not have a well-defined direction.
    	
    	double alen = a.length();
    	double blen = b.length();
    	
        if (alen < TOL || blen < TOL) {
            return false;
        }
        double cosTheta = a.dot(b) /(alen * blen);
        return Math.abs(cosTheta - 1.0) < TOL;
    }
}

