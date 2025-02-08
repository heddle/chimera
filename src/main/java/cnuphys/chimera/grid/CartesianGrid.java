package cnuphys.chimera.grid;

import cnuphys.chimera.util.Point3D;

/**
 * Represents a Cartesian grid defined by 1D grids in x, y, and z directions,
 * along with an offset for each coordinate.
 */
public class CartesianGrid {
    private final Grid1D xGrid;
    private final Grid1D yGrid;
    private final Grid1D zGrid;
    private final double xOffset, yOffset, zOffset;

    /**
     * Constructs a CartesianGrid.
     *
     * @param xArray an array of x grid points (must not be empty)
     * @param yArray an array of y grid points (must not be empty)
     * @param zArray an array of z grid points (must not be empty)
     * @param xo     the x-offset from the global origin
     * @param yo     the y-offset from the global origin
     * @param zo     the z-offset from the global origin
     * @throws IllegalArgumentException if any input array is empty
     */
    public CartesianGrid(double[] xArray, double[] yArray, double[] zArray, double xo, double yo, double zo) {
        if (xArray == null || xArray.length == 0) {
            throw new IllegalArgumentException("xArray must not be empty.");
        }
        if (yArray == null || yArray.length == 0) {
            throw new IllegalArgumentException("yArray must not be empty.");
        }
        if (zArray == null || zArray.length == 0) {
            throw new IllegalArgumentException("zArray must not be empty.");
        }

        this.xGrid = new Grid1D(xArray);
        this.yGrid = new Grid1D(yArray);
        this.zGrid = new Grid1D(zArray);
        this.xOffset = xo;
        this.yOffset = yo;
        this.zOffset = zo;
    }

    /**
     * Copy constructor.
     *
     * @param other the CartesianGrid instance to copy.
     */
    public CartesianGrid(CartesianGrid other) {
        this.xGrid = new Grid1D(other.xGrid);
        this.yGrid = new Grid1D(other.yGrid);
        this.zGrid = new Grid1D(other.zGrid);
        this.xOffset = other.xOffset;
        this.yOffset = other.yOffset;
        this.zOffset = other.zOffset;
    }

    // ------------------ Accessors for the Grid1D objects ------------------

    public Grid1D getXGrid() { return xGrid; }
    public Grid1D getYGrid() { return yGrid; }
    public Grid1D getZGrid() { return zGrid; }

    // ------------------ Accessors for the Offsets ------------------

    public double getXOffset() { return xOffset; }
    public double getYOffset() { return yOffset; }
    public double getZOffset() { return zOffset; }

    // ------------------ Convenience Methods ------------------

    public double getXMin() { return xGrid.min() + xOffset; }
    public double getXMax() { return xGrid.max() + xOffset; }
    public int getNumX() { return xGrid.numPoints(); }

    public double getYMin() { return yGrid.min() + yOffset; }
    public double getYMax() { return yGrid.max() + yOffset; }
    public int getNumY() { return yGrid.numPoints(); }

    public double getZMin() { return zGrid.min() + zOffset; }
    public double getZMax() { return zGrid.max() + zOffset; }
    public int getNumZ() { return zGrid.numPoints(); }

    // ------------------ Index and Coordinate Methods ------------------

    /**
     * Given global coordinates (x, y, z), locates the corresponding indices in the grids.
     *
     * @param x       the global x-coordinate.
     * @param y       the global y-coordinate.
     * @param z       the global z-coordinate.
     * @param indices an int array of length at least 3 in which the method stores the x, y, and z
     *                interval indices. If a coordinate is out of range, the corresponding index will be -1.
     * @return the same indices array passed in.
     * @throws IllegalArgumentException if the indices array is null or has length less than 3.
     */
    public int[] getIndices(double x, double y, double z, int[] indices) {
        if (indices == null || indices.length < 3) {
            throw new IllegalArgumentException("indices array must have at least length 3.");
        }

        // Initialize indices as -1 in case of out-of-range values
        indices[0] = -1;
        indices[1] = -1;
        indices[2] = -1;

        // Convert global coordinates to local grid coordinates and locate intervals
        indices[0] = xGrid.locateInterval(x - xOffset);
        indices[1] = yGrid.locateInterval(y - yOffset);
        indices[2] = zGrid.locateInterval(z - zOffset);
        return indices;
    }

    /**
     * Given global coordinates (x, y, z) in a Point3D object, locates the corresponding indices in the
     * grids.
     *
     * @param point   the global coordinates as a Point3D.
     * @param indices an array of length at least 3 to store the indices.
     * @return the indices array with updated values.
     */
    public int[] getIndices(Point3D.Double point, int[] indices) {
        return getIndices(point.x, point.y, point.z, indices);
    }

    /**
     * Returns the global coordinates corresponding to the specified indices.
     *
     * @param ix the x-index.
     * @param iy the y-index.
     * @param iz the z-index.
     * @return a Point3D.Double holding the global (x, y, z) coordinates.
     * @throws IndexOutOfBoundsException if any index is out of range.
     */
    public Point3D.Double getCoordinates(int ix, int iy, int iz) {
        if (ix < 0 || ix >= getNumX()) {
            throw new IndexOutOfBoundsException("X index " + ix + " is out of bounds.");
        }
        if (iy < 0 || iy >= getNumY()) {
            throw new IndexOutOfBoundsException("Y index " + iy + " is out of bounds.");
        }
        if (iz < 0 || iz >= getNumZ()) {
            throw new IndexOutOfBoundsException("Z index " + iz + " is out of bounds.");
        }

        double xVal = xGrid.valueAt(ix) + xOffset;
        double yVal = yGrid.valueAt(iy) + yOffset;
        double zVal = zGrid.valueAt(iz) + zOffset;
        return new Point3D.Double(xVal, yVal, zVal);
    }
}
