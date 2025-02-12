package cnuphys.chimera.grid;

import java.util.Arrays;

public class Grid1D {
    private final double[] pts;

    /**
     * Constructor: Takes an array of grid points, sorts them in ascending order.
     * @param points The array of grid points (not necessarily uniformly spaced).
     * @throws IllegalArgumentException if points array is empty.
     */
    public Grid1D(double[] points) {
        if (points == null || points.length == 0) {
            throw new IllegalArgumentException("Grid1D cannot be initialized with an empty array.");
        }
        // Copy the input array to avoid side-effects.
        pts = Arrays.copyOf(points, points.length);
        Arrays.sort(pts);
    }

    /**
     * Copy constructor.
     * @param other The Grid1D instance to copy.
     */
    public Grid1D(Grid1D other) {
        this.pts = Arrays.copyOf(other.pts, other.pts.length);
    }

    /**
     * Returns the minimum grid point value.
     * @return The smallest value in the grid.
     */
    public double min() {
        return pts[0];
    }

    /**
     * Returns the maximum grid point value.
     * @return The largest value in the grid.
     */
    public double max() {
        return pts[pts.length - 1];
    }

    /**
     * Finds the index n such that the provided value lies between pts[n] and pts[n+1].
     * If the value is out of range, returns -1.
     * Uses binary search for efficiency.
     *
     * @param value The value to locate.
     * @return The index n such that pts[n] <= value < pts[n+1], or -1 if out of range.
     */
    public int locateInterval(double value) {
        // Handle cases where the value is out of range
        if (value < pts[0] || value > pts[pts.length - 1]) {
            return -1;
        }

        int index = Arrays.binarySearch(pts, value);

        if (index >= 0) {
            // If it's the last element, no interval exists
            return (index == pts.length - 1) ? pts.length - 2 : index;
        }

        // If not found, determine the insertion point
        int insertionPoint = -index - 1;

        // If the insertion point is at the beginning, it's out of range
        if (insertionPoint == 0) {
            return -1;
        }

        // The interval is between insertionPoint - 1 and insertionPoint
        return insertionPoint - 1;
    }
    
    /**
     * Returns the average spacing between grid points.
     * 
     * @return The average spacing between grid points
     */
	public double getAverageSpacing() {
		double sum = 0;
		for (int i = 1; i < pts.length; i++) {
			sum += pts[i] - pts[i - 1];
		}
		return sum / (pts.length - 1);
	}

    /**
     * Returns the grid point at the specified index.
     * @param index The index of the grid point.
     * @return The grid point value.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public double valueAt(int index) {
        if (index < 0 || index >= pts.length) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for Grid1D.");
        }
        return pts[index];
    }
    
	/**
	 * Returns a copy of the grid points array.
	 * 
	 * @return A copy of the grid points array.
	 */
	public double[] getPoints() {
		return Arrays.copyOf(pts, pts.length);
	}

    /**
     * Returns the total number of grid points.
     * @return The number of grid points.
     */
    public int numPoints() {
        return pts.length;
    }
}
