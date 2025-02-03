package cnuphys.chimera.grid;
public class Grid1D {
    private double vmin;  // Minimum value of the grid
    private double vmax;  // Maximum value of the grid
    private int num;      // Number of points in the grid
    private double del;   // Spacing between grid points

    /**
     * Constructor to initialize the Grid1D with specified parameters.
     *
     * @param vmin Minimum value of the grid.
     * @param vmax Maximum value of the grid.
     * @param num Number of points in the grid (including endpoints).
     * @throws IllegalArgumentException if num is less than 2 or if vmin >= vmax.
     */
    public Grid1D(double vmin, double vmax, int num) {
    	setGrid(vmin, vmax, num);
    }

    public void setGrid(double vmin, double vmax, int num) {
		if (num < 2) {
			throw new IllegalArgumentException("num must be at least 2.");
		}
		if (vmin >= vmax) {
			throw new IllegalArgumentException("vmin must be less than vmax.");
		}
		this.vmin = vmin;
		this.vmax = vmax;
		this.num = num;
        computeDel();
    }

    /**
     * Copy constructor to create a deep copy of the source Grid1D.
     * @param source The Grid1D ro copy
     */
	public Grid1D(Grid1D source) {
		this(source.vmin, source.vmax, source.num);
	}

	/**
	 * Set the minimum value of the grid.
	 *
	 * @param vmin The minimum value to set.
	 */
	public void setMin(double vmin) {
		this.vmin = vmin;
		computeDel();
	}

	/**
     * Set the maximum value of the grid.
     *
     * @param vmax The maximum value to set.
     */
	public void setMax(double vmax) {
		this.vmax = vmax;
		computeDel();
	}

	/**
	 * Compute the spacing between grid points.
	 */
    private void computeDel() {
    	this.del = (vmax - vmin) / (num - 1);
    }

    /**
     * Get the index of the grid segment such that val lies between grid points at index n and n+1.
     *
     * @param val The value for which the index is needed.
     * @return The index (0 to num-2) if val is within the grid bounds; -1 otherwise.
     */
    public int getIndex(double val) {
        if (val < vmin || val > vmax) {
            return -1;  // Value is outside the grid
        }
        int index = (int) ((val - vmin) / del);
        if (index >= num - 1) {
            return num - 2;  // Handle edge case for max value
        }
        return index;
    }

    /**
     * Get the spacing (delta) of the grid.
     *
     * @return The spacing between grid points.
     */
    public double getSpacing() {
        return del;
    }

    /**
     * Get the minimum value of the grid.
     *
     * @return The minimum value of the grid.
     */
    public double getVmin() {
        return vmin;
    }

    /**
     * Get the maximum value of the grid.
     *
     * @return The maximum value of the grid.
     */
    public double getVmax() {
        return vmax;
    }

    /**
     * Get the number of points in the grid.
     *
     * @return The number of points in the grid.
     */
    public int getNum() {
        return num;
    }

    /**
     * Set the number of points in the grid.
     * @param num The number of points to set.
     */
	public void setNum(int num) {
		if (num < 2) {
			throw new IllegalArgumentException("num must be at least 2.");
		}
		this.num = num;
		computeDel();
	}

    // Test the class with a simple example
    public static void main(String[] args) {
        Grid1D grid = new Grid1D(0.0, 10.0, 11);
        System.out.println("Spacing (del): " + grid.getSpacing());
        System.out.println("Index for 0.5: " + grid.getIndex(0.5));  // Should be 0
        System.out.println("Index for 5.5: " + grid.getIndex(5.5));  // Should be 5
        System.out.println("Index for 9.9: " + grid.getIndex(9.9));  // Should be 9
        System.out.println("Index for 10.0: " + grid.getIndex(10.0)); // Should be 9 (last segment)
        System.out.println("Index for 8.0001: " + grid.getIndex(8.0001)); // Should be 8
        System.out.println("Index for 7.999: " + grid.getIndex(7.999)); // Should be 7


        System.out.println("Index for -1.0: " + grid.getIndex(-1.0)); // Should be -1 (out of bounds)
    }
}

