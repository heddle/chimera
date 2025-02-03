package cnuphys.chimera.grid;

import cnuphys.chimera.util.MathUtil;
import cnuphys.chimera.util.ThetaPhi;

public class SphericalGrid {



    private final Grid1D thetaGrid;  // Grid for theta (polar angle)
    private final Grid1D phiGrid;    // Grid for phi (azimuthal angle)
    private double alpha;     // Rotation about the x-axis
    private double beta;      // Rotation about the new z-axis

    private double sina, cosa;      // Sine and cosine of alpha
    private double sinb, cosb;      // Sine and cosine of beta
    
    private double radius; // Radius of the sphere

    /**
     * Constructor for the SphericalGrid class.
     *
     * @param numTheta Number of points in the theta grid.
     * @param numPhi   Number of points in the phi grid.
     * @param radius   Radius of the sphere.
     * @param alpha    Rotation angle about the x-axis in radians.
     * @param beta     Rotation angle about the new z-axis in radians.
     */
    public SphericalGrid(int numTheta, int numPhi, double radius, double alpha, double beta) {
        this.thetaGrid = new Grid1D(0.0, Math.PI, numTheta);
        this.phiGrid = new Grid1D(-Math.PI, Math.PI, numPhi);
        this.radius = radius;
        this.alpha = alpha;
        this.beta = beta;

        // Precompute sine and cosine values for alpha and beta
        sina = Math.sin(alpha);
        cosa = Math.cos(alpha);
        sinb = Math.sin(beta);
        cosb = Math.cos(beta);
    }
    
	public double getRadius() {
		return radius;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
		sina = Math.sin(alpha);
		cosa = Math.cos(alpha);
	}

	public void setBeta(double beta) {
		this.beta = beta;
		sinb = Math.sin(beta);
		cosb = Math.cos(beta);
	}

	public double getAlpha() {
		return alpha;
	}

	public double getBeta() {
		return beta;
	}

    public int getNumTheta() {
        return thetaGrid.getNum();
    }

    public double getThetaDel() {
        return thetaGrid.getSpacing();
    }

    public int getNumPhi() {
        return phiGrid.getNum();
    }

    public double getPhiDel() {
        return phiGrid.getSpacing();
    }

	public void setNumTheta(int numTheta) {
		thetaGrid.setNum(numTheta);
	}

	public void setNumPhi(int numPhi) {
		phiGrid.setNum(numPhi);
	}


    /**
     * Copy constructor to create a deep copy of the source SphericalGrid
     * @param source the source SphericalGrid to copy
     */
	public SphericalGrid(SphericalGrid source) {
		this.thetaGrid = new Grid1D(source.thetaGrid);
		this.phiGrid = new Grid1D(source.phiGrid);
		this.radius = source.radius;
		this.alpha = source.alpha;
		this.beta = source.beta;
		this.sina = source.sina;
		this.cosa = source.cosa;
		this.sinb = source.sinb;
		this.cosb = source.cosb;
	}

    /**
     * Gets the grid indices for the given ThetaPhi object.
     *
     * @param thetaPhi The ThetaPhi object with theta and phi values.
     * @param indices  An int array where the theta and phi indices will be stored.
     */
    public void getIndices(ThetaPhi thetaPhi, int[] indices) {
        getIndices(thetaPhi.getTheta(), thetaPhi.getPhi(), indices);
    }

    /**
     * Gets the grid indices for the given global theta and phi values.
     *
     * @param theta   The polar angle in global coordinates.
     * @param phi     The azimuthal angle in global coordinates.
     * @param indices An int array where the theta and phi indices will be stored.
     */
    public void getIndices(double theta, double phi, int[] indices) {
        // Rotate the global coordinates if alpha or beta are non-zero
        if (alpha != 0 || beta != 0) {
            double[] rotated = rotateGlobalToLocal(theta, phi);
            theta = rotated[0];
            phi = rotated[1];
        }

        // Get the indices from the grids
        indices[0] = thetaGrid.getIndex(theta);  // Theta index
        indices[1] = phiGrid.getIndex(phi);      // Phi index
    }

    /**
     * Rotates global spherical coordinates (theta, phi) to the local sphere coordinate system.
     *
     * @param theta The polar angle in global coordinates.
     * @param phi   The azimuthal angle in global coordinates.
     * @return A double array with rotated theta and phi values.
     */
    private double[] rotateGlobalToLocal(double theta, double phi) {
        // Convert spherical to Cartesian coordinates
        double x = radius * Math.sin(theta) * Math.cos(phi);
        double y = radius * Math.sin(theta) * Math.sin(phi);
        double z = radius * Math.cos(theta);

        // First rotation: Rotate about the x-axis by alpha
        double zRot1 = z * cosa - y * sina;
        double yRot1 = z * sina + y * cosa;
        double xRot1 = x;

        // Second rotation: Rotate about the new z-axis by beta
        double xRot2 = xRot1 * cosb - yRot1 * sinb;
        double yRot2 = xRot1 * sinb + yRot1 * cosb;
        double zRot2 = zRot1;

        // Convert back to spherical coordinates
        double thetaRot = Math.acos(zRot2 / radius);  // New theta
        double phiRot = Math.atan2(yRot2, xRot2);     // New phi
        return new double[]{thetaRot, MathUtil.normalizeAngle(phiRot)};
    }

    public static void main(String[] args) {
        // Example usage
        int numTheta = 10;
        int numPhi = 20;
        double radius = 1.0;
        double alpha = Math.PI / 4;  // 45 degrees
        double beta = Math.PI / 6;   // 30 degrees

        SphericalGrid sphericalGrid = new SphericalGrid(numTheta, numPhi, radius, alpha, beta);
        ThetaPhi thetaPhi = new ThetaPhi(radius, Math.PI / 3, Math.PI / 4);

        int[] indices = new int[2];
        sphericalGrid.getIndices(thetaPhi, indices);

        System.out.println("Theta index: " + indices[0]);
        System.out.println("Phi index: " + indices[1]);
    }
}
