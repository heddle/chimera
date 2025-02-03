package cnuphys.chimera.grid;

import java.util.ArrayList;
import java.util.List;


public class ChimeraGrid {
    private CartesianGrid cartGrid;
    private SphericalGrid sphGrid;
    private List<Threeplet> intersectingCells = new ArrayList<>();

    public ChimeraGrid(CartesianGrid cartGrid, SphericalGrid sphGrid) {
        if (cartGrid == null || sphGrid == null) {
            throw new IllegalArgumentException("Both grids must be non-null.");
        }
        this.cartGrid = cartGrid;
        this.sphGrid = sphGrid;
    }

    public void findIntersectingCells() {
        double sphereRadius = sphGrid.getRadius();
        
        for (int ix = 0; ix < cartGrid.getNumX() - 1; ix++) {
            for (int iy = 0; iy < cartGrid.getNumY() - 1; iy++) {
                for (int iz = 0; iz < cartGrid.getNumZ() - 1; iz++) {
                    double[][] corners = getCellCorners(ix, iy, iz);
                    int inside = 0, outside = 0;
                    for (double[] c : corners) {
                        double distSquared = c[0] * c[0] + c[1] * c[1] + c[2] * c[2];
                        if (distSquared < sphereRadius * sphereRadius) inside++;
                        else outside++;
                    }
                    if (inside > 0 && outside > 0) intersectingCells.add(new Threeplet(ix, iy, iz));
                }
            }
        }
        System.out.println("Number of intersecting cells: " + intersectingCells.size());
    }
    
	public void reset() {
		intersectingCells.clear();
	}
    
    private double[][] getCellCorners(int ix, int iy, int iz) {
        double xMin = cartGrid.getXMin();
        double yMin = cartGrid.getYMin();
        double zMin = cartGrid.getZMin();
        double xDel = cartGrid.getXDel();
        double yDel = cartGrid.getYDel();
        double zDel = cartGrid.getZDel();
        
        double[][] corners = new double[8][3];
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    int index = dx * 4 + dy * 2 + dz;
                    corners[index][0] = xMin + (ix + dx) * xDel;
                    corners[index][1] = yMin + (iy + dy) * yDel;
                    corners[index][2] = zMin + (iz + dz) * zDel;
                }
            }
        }
        return corners;
    }
    
	public CartesianGrid getCartesianGrid() {
		return cartGrid;
	}
	
	public void setCartesianGrid(CartesianGrid cartGrid) {
		this.cartGrid = cartGrid;
	}
	
	public SphericalGrid getSphericalGrid() {
		return sphGrid;
	}
	
	public void setSphericalGrid(SphericalGrid sphGrid) {
		this.sphGrid = sphGrid;
	}
}