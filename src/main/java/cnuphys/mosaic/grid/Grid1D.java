package cnuphys.mosaic.grid;

import java.util.Arrays;

public class Grid1D {
    private final double[] _points;
    private double _maxSpacing;

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
        _points = Arrays.copyOf(points, points.length);
        Arrays.sort(_points);
        _maxSpacing = maxSpacing();
    }

    /**
     * Copy constructor.
     * @param other The Grid1D instance to copy.
     */
    public Grid1D(Grid1D other) {
    	this(other._points);
    }

	/**
	 * Returns the grid point at the specified index.
	 *
	 * @param index The index of the grid point.
	 * @return The grid point value.
	 * @throws IndexOutOfBoundsException if the index is out of range.
	 */
	public double gridValue(int index) {
		if (index < 0 || index >= _points.length) {
			throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for Grid1D.");
		}
		return _points[index];
	}

    /**
     * Returns the minimum grid point value.
     * @return The smallest value in the grid.
     */
    public double min() {
        return _points[0];
    }

    /**
     * Returns the maximum grid point value.
     * @return The largest value in the grid.
     */
    public double max() {
        return _points[_points.length - 1];
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
        if (value < _points[0] || value > _points[_points.length - 1]) {
            return -1;
        }

        int index = Arrays.binarySearch(_points, value);

        if (index >= 0) {
            // If it's the last element, no interval exists
            return (index == _points.length - 1) ? _points.length - 2 : index;
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
		for (int i = 1; i < _points.length; i++) {
			sum += _points[i] - _points[i - 1];
		}
		return sum / (_points.length - 1);
	}

	// Returns the maximum spacing between grid points.
	private double maxSpacing() {
		double max = 0;
		for (int i = 1; i < _points.length; i++) {
			double spacing = _points[i] - _points[i - 1];
			if (spacing > max) {
				max = spacing;
			}
		}
		return max;
	}

	/**
	 * Returns the maximum spacing between grid points.
	 *
	 * @return The maximum spacing between grid points.
	 */
	public double getMaxSpacing() {
		return _maxSpacing;
	}

	/**
	 * Returns the index of the closest grid point to the specified value.
	 *
	 * @param value The value to search for.
	 * @return The index of the closest grid point.
	 */
	public int closestIndex(double value) {
		int index = locateInterval(value);
		if (index == -1) {
			return -1;
		}
		// If the value is closer to the next point, return the next index
		if (index < _points.length - 1 && Math.abs(_points[index + 1] - value) < Math.abs(_points[index] - value)) {
			return index + 1;
		}
		return index;
	}

    /**
     * Returns the grid point at the specified index.
     * @param index The index of the grid point.
     * @return The grid point value.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public double valueAt(int index) {
        if (index < 0 || index >= _points.length) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for Grid1D.");
        }
        return _points[index];
    }

	/**
	 * Returns a copy of the grid points array.
	 *
	 * @return A copy of the grid points array.
	 */
	public double[] getPoints() {
		return Arrays.copyOf(_points, _points.length);
	}

    /**
     * Returns the total number of grid points.
     * @return The number of grid points.
     */
    public int numPoints() {
        return _points.length;
    }

    /**
     * Get bulk filter limits around an assumed sphere of
     * a given radius and centered on the origin.
     * @param radius The radius of the sphere.
     * @return a set if indices [lower, upper] of grid points
     * such that any outside the range will certainly be outside
     * the sphere.
     */
    public int[] bulkFilterLimits(double radius) {
		int[] limits = new int[2];
		limits[0] = locateInterval(-radius) - 1;
		limits[1] = locateInterval(radius) + 1;
		limits[0] = Math.max(0, limits[0]);
		limits[1] = Math.min(numPoints() - 1, limits[1]);
		return limits;
    }
}
