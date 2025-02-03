package cnuphys.chimera.grid;

import cnuphys.chimera.util.Point3D;

public class CartesianGrid {
    private Grid1D xGrid;
    private Grid1D yGrid;
    private Grid1D zGrid;
    public double xo, yo, zo; // Offsets for x, y, and z

    // Constructor to initialize x, y, z grids with offsets
    public CartesianGrid(double xMin, double xMax, int numX,
                         double yMin, double yMax, int numY,
                         double zMin, double zMax, int numZ,
                         double xo, double yo, double zo) {
        xGrid = new Grid1D(xMin, xMax, numX);
        yGrid = new Grid1D(yMin, yMax, numY);
        zGrid = new Grid1D(zMin, zMax, numZ);

        this.xo = xo;
        this.yo = yo;
        this.zo = zo;
    }

    /**
     * Copy constructor to create a deep copy of the source CartesianGrid
     * @param source the source CartesianGrid to copy
     */
	public CartesianGrid(CartesianGrid source) {
        xGrid = new Grid1D(source.xGrid);
        yGrid = new Grid1D(source.yGrid);
        zGrid = new Grid1D(source.zGrid);
        xo = source.xo;
        yo = source.yo;
        zo = source.zo;
    }

    // Convenience methods for the X grid
    public void setXGrid(double vmin, double vmax, int num) {
    	xGrid.setGrid(vmin, vmax, num);
    }

	public void setXOffset(double xo) {
		this.xo = xo;
	}

	public double getXOffset() {
		return xo;
	}

    public double getXMin() {
        return xGrid.getVmin();
    }

    public double getXMax() {
        return xGrid.getVmax();
    }

	public void setXMax(double xmax) {
		xGrid.setMax(xmax);
	}

	public void setXMin(double xmin) {
		xGrid.setMin(xmin);
	}

    public int getNumX() {
        return xGrid.getNum();
    }

    public double getXDel() {
        return xGrid.getSpacing();
    }

    // Convenience methods for the Y grid
    public void setYGrid(double vmin, double vmax, int num) {
    	yGrid.setGrid(vmin, vmax, num);
    }

	public void setYOffset(double yo) {
		this.yo = yo;
	}

	public double getYOffset() {
		return yo;
	}

    public double getYMin() {
        return yGrid.getVmin();
    }

    public double getYMax() {
        return yGrid.getVmax();
    }

	public void setYMax(double ymax) {
		yGrid.setMax(ymax);
	}

	public void setYMin(double ymin) {
		yGrid.setMin(ymin);
	}
    public int getNumY() {
        return yGrid.getNum();
    }

    public double getYDel() {
        return yGrid.getSpacing();
    }

    // Convenience methods for the Z grid
    public void setZGrid(double vmin, double vmax, int num) {
    	zGrid.setGrid(vmin, vmax, num);
    }

	public void setZOffset(double zo) {
		this.zo = zo;
	}

	public double getZOffset() {
		return zo;
	}

    public double getZMin() {
        return zGrid.getVmin();
    }

    public double getZMax() {
        return zGrid.getVmax();
    }

	public void setZMax(double zmax) {
		zGrid.setMin(zmax);
	}

	public void setZMin(double zmin) {
		zGrid.setMax(zmin);
	}
    public int getNumZ() {
        return zGrid.getNum();
    }

    public double getZDel() {
        return zGrid.getSpacing();
    }

	public void setNumX(int numX) {
		xGrid.setNum(numX);
	}

	public void setNumY(int numY) {
		yGrid.setNum(numY);
	}

	public void setNumZ(int numZ) {
		zGrid.setNum(numZ);
	}

    // Get indices for a Point3D.Double
    public void getIndices(Point3D.Double point, int[] indices) {
        if (indices == null || indices.length < 3) {
            throw new IllegalArgumentException("Indices array must have at least three elements.");
        }
        getIndices(point.x, point.y, point.z, indices);
    }

    // Get indices for explicit coordinates
    public void getIndices(double x, double y, double z, int[] indices) {
        if (indices == null || indices.length < 3) {
            throw new IllegalArgumentException("Indices array must have at least three elements.");
        }

        indices[0] = xGrid.getIndex(x - xo); // X index
        indices[1] = yGrid.getIndex(y - yo); // Y index
        indices[2] = zGrid.getIndex(z - zo); // Z index

        for (int index : indices) {
            if (index == -1) {
                throw new IllegalArgumentException("Point is outside the grid bounds.");
            }
        }
    }

    // Get global coordinates from grid indices
    public Point3D.Double getCoordinates(int ix, int iy, int iz) {
        if (ix < 0 || ix >= xGrid.getNum() ||
            iy < 0 || iy >= yGrid.getNum() ||
            iz < 0 || iz >= zGrid.getNum()) {
            throw new IllegalArgumentException("Indices are out of bounds.");
        }

        double x = xGrid.getVmin() + ix * xGrid.getSpacing() + xo;
        double y = yGrid.getVmin() + iy * yGrid.getSpacing() + yo;
        double z = zGrid.getVmin() + iz * zGrid.getSpacing() + zo;

        return new Point3D.Double(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("CartesianGrid[xGrid=%s, yGrid=%s, zGrid=%s, offsets=(%.4f, %.4f, %.4f)]",
                             xGrid, yGrid, zGrid, xo, yo, zo);
    }
}


