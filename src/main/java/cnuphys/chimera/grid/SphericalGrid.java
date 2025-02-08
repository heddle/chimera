package cnuphys.chimera.grid;

import cnuphys.chimera.util.MathUtil;
import cnuphys.chimera.util.ThetaPhi;
import java.util.Arrays;

/**
 * A grid in spherical coordinates. The grid is defined by:
 * <ul>
 *   <li>A theta (polar) grid whose values must span 0 to π (within 1e-6).</li>
 *   <li>A phi (azimuthal) grid whose values are normalized into the range [-π, π]
 *       and must span -π to π (within 1e-6).</li>
 *   <li>A fixed radius.</li>
 *   <li>Two rotation angles: alpha (rotation about the x-axis) and beta (rotation about the new z-axis).
 *       These rotate global spherical coordinates into the local grid coordinates.</li>
 * </ul>
 */
public class SphericalGrid {

    private final Grid1D thetaGrid;
    private final Grid1D phiGrid;
    private final double radius;
    private double alpha;  // Rotation about the x-axis (in radians)
    private double beta;   // Rotation about the new z-axis (in radians)

    private static final double TOL = 1e-6;

    /**
     * Constructs a SphericalGrid.
     *
     * @param thetaArray An array of polar angles (in radians). Must span 0 to π (within 1e-6).
     * @param phiArray   An array of azimuthal angles (in radians). Normalized to [-π, π] and must span -π to π (within 1e-6).
     * @param radius     The fixed radius (must be positive).
     * @param alpha      Rotation about the x-axis (in radians).
     * @param beta       Rotation about the new z-axis (in radians).
     * @throws IllegalArgumentException if the grid arrays do not span the required ranges or if radius is nonpositive.
     */
    public SphericalGrid(double[] thetaArray, double[] phiArray, double radius, double alpha, double beta) {
        if (thetaArray == null || thetaArray.length == 0) {
            throw new IllegalArgumentException("Theta grid must not be empty.");
        }
        if (phiArray == null || phiArray.length == 0) {
            throw new IllegalArgumentException("Phi grid must not be empty.");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be positive.");
        }
        this.radius = radius;
        this.alpha = alpha;
        this.beta = beta;

        // Process theta grid
        double[] thetaCopy = Arrays.copyOf(thetaArray, thetaArray.length);
        Arrays.sort(thetaCopy);
        if (Math.abs(thetaCopy[0]) > TOL || Math.abs(thetaCopy[thetaCopy.length - 1] - Math.PI) > TOL) {
            throw new IllegalArgumentException("Theta grid must span from 0 to π within tolerance " + TOL);
        }
        thetaGrid = new Grid1D(thetaCopy);

        // Process phi grid
        double[] phiCopy = Arrays.copyOf(phiArray, phiArray.length);
//        for (int i = 0; i < phiCopy.length; i++) {
//            phiCopy[i] = MathUtil.normalizeAngle(phiCopy[i]);
//        }
        Arrays.sort(phiCopy);
        if (Math.abs(phiCopy[0] + Math.PI) > TOL) {
            throw new IllegalArgumentException("phigrid[0] grid must be close to -π within tolerance " + TOL + " but is " + phiCopy[0]);
        }
        if (Math.abs(phiCopy[phiCopy.length - 1] - Math.PI) > TOL) {
			throw new IllegalArgumentException("phigrid[phigrid.length - 1] grid must be close to π within tolerance "
					+ TOL + " but is " + phiCopy[phiCopy.length - 1]);
		}

        phiGrid = new Grid1D(phiCopy);
    }
    
    /**
     * Copy constructor for SphericalGrid.
     *
     * @param other The SphericalGrid instance to copy.
     */
    public SphericalGrid(SphericalGrid other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null SphericalGrid.");
        }

        // Copy theta and phi grids
        this.thetaGrid = new Grid1D(other.thetaGrid);
        this.phiGrid = new Grid1D(other.phiGrid);

        // Copy primitive fields
        this.radius = other.radius;
        this.alpha = other.alpha;
        this.beta = other.beta;
    }
   

    public Grid1D getThetaGrid() { return thetaGrid; }
    public Grid1D getPhiGrid() { return phiGrid; }
    public double getRadius() { return radius; }
    public double getAlpha() { return alpha; }
    public double getBeta() { return beta; }
    public void setAlpha(double alpha) { this.alpha = alpha; }
    public void setBeta(double beta) { this.beta = beta; }
    public double getThetaMin() { return thetaGrid.min(); }
    public double getThetaMax() { return thetaGrid.max(); }
    public int getNumTheta() { return thetaGrid.numPoints(); }
    public double getPhiMin() { return phiGrid.min(); }
    public double getPhiMax() { return phiGrid.max(); }
    public int getNumPhi() { return phiGrid.numPoints(); }
    
    public double[] getThetaArray() { return thetaGrid.getPoints(); }
    public double[] getPhiArray() { return phiGrid.getPoints(); }

    /**
     * Converts global (theta, phi) to local grid coordinates.
     *
     * @param theta the global polar angle (in radians)
     * @param phi   the global azimuthal angle (in radians)
     * @return {localTheta, localPhi}
     */
    private double[] rotateGlobalToLocal(double theta, double phi) {
        double sinTheta = Math.sin(theta);
        double x = radius * sinTheta * Math.cos(phi);
        double y = radius * sinTheta * Math.sin(phi);
        double z = radius * Math.cos(theta);

        double y1 = y * Math.cos(alpha) - z * Math.sin(alpha);
        double z1 = y * Math.sin(alpha) + z * Math.cos(alpha);
        double x1 = x;

        double x2 = x1 * Math.cos(beta) - y1 * Math.sin(beta);
        double y2 = x1 * Math.sin(beta) + y1 * Math.cos(beta);
        double z2 = z1;

        double localTheta = Math.acos(z2 / radius);
        double localPhi = MathUtil.normalizeAngle(Math.atan2(y2, x2));

        return new double[]{localTheta, localPhi};
    }

    /**
     * Given global (theta, phi), computes the grid indices after rotation.
     *
     * @param theta   the global polar angle (in radians)
     * @param phi     the global azimuthal angle (in radians)
     * @param indices an int array of length at least 2; stores {thetaIndex, phiIndex}.
     */
    public void getIndices(double theta, double phi, int[] indices) {
        if (indices == null || indices.length < 2) {
            throw new IllegalArgumentException("indices array must have at least length 2");
        }
        double[] local = rotateGlobalToLocal(theta, phi);
        indices[0] = thetaGrid.locateInterval(local[0]);
        indices[1] = phiGrid.locateInterval(local[1]);
    }

    public void getIndices(ThetaPhi thetaPhi, int[] indices) {
        if (thetaPhi == null) {
            throw new IllegalArgumentException("ThetaPhi cannot be null.");
        }
        getIndices(thetaPhi.getTheta(), thetaPhi.getPhi(), indices);
    }
}
