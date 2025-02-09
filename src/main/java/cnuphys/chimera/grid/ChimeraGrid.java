package cnuphys.chimera.grid;


import java.util.ArrayList;
import java.util.List;

import cnuphys.bCNU.util.Bits;
import cnuphys.chimera.util.ChimeraPlane;
import cnuphys.chimera.util.Point3D;

/**
 * Represents a Chimera grid, which combines a CartesianGrid and a SphericalGrid.
 * This class identifies and stores the indices of Cartesian grid cells that intersect
 * the sphere defined by the SphericalGrid.
 */
public class ChimeraGrid {
    
    private final CartesianGrid cartesianGrid;
    private final SphericalGrid sphericalGrid;
    
    private List<Cell> intersectingCells = new ArrayList<>();

    public ChimeraGrid(CartesianGrid cartesianGrid, SphericalGrid sphericalGrid) {
        this.cartesianGrid = new CartesianGrid(cartesianGrid); 
        this.sphericalGrid = new SphericalGrid(sphericalGrid);
    }

    public ChimeraGrid(ChimeraGrid other) {
        this.cartesianGrid = new CartesianGrid(other.cartesianGrid);
        this.sphericalGrid = new SphericalGrid(other.sphericalGrid);
        this.intersectingCells = new ArrayList<>(other.intersectingCells);
    }
    
	public CartesianGrid getCartesianGrid() {
		return cartesianGrid;
	}
	
	public SphericalGrid getSphericalGrid() {
		return sphericalGrid;
	}

	public List<Cell> getIntersectingCells() {
		return intersectingCells;
	}

    public void reset() {
        intersectingCells.clear();
    }

	/**
	 * Identifies the Cartesian grid cells that intersect the sphere defined by the
	 * SphericalGrid.
	 */
    
    int kisscount = 0;
    public void findIntersectingCells() {
        reset();

        double radius = sphericalGrid.getRadius();
        double rSquared = radius * radius;

        int nx = cartesianGrid.getNumX() - 1;
        int ny = cartesianGrid.getNumY() - 1;
        int nz = cartesianGrid.getNumZ() - 1;

		for (int iz = 0; iz < nz; iz++) {
			for (int iy = 0; iy < ny; iy++) {
				for (int ix = 0; ix < nx; ix++) {
					
					double[][] cell = GridSupport.getCellCorners(cartesianGrid, ix, iy, iz);

			        int cornerBits = 0;
					boolean hasInside = false;
					boolean hasOutside = false;

					// check each corner of the cell
					for (int canonicalCorner = 0; canonicalCorner < 8; canonicalCorner++) {
						double[] corner = cell[canonicalCorner];
						double x = corner[0], y = corner[1], z = corner[2];
						double distSquared = x * x + y * y + z * z;

	                    if (distSquared < rSquared) {
	                    	cornerBits = Bits.setBit(cornerBits, GridSupport.CORNERBITS[canonicalCorner]);
                            hasInside = true;
                        } else {
                            hasOutside = true;
                        }
					} //end loop over corners

					//if have corners on both sides of the sphere, then the cell intersects
                    //if no traditional intersection, must do the kiss test!
                    //for cells with no inside points
                    if (hasInside && hasOutside) {
                        intersectingCells.add(new Cell(cartesianGrid, ix, iy, iz, cornerBits, radius));
                    }
                    else if (!hasInside) {
						int kissFace = kissTest(cell, radius);
						if (kissFace >= 0) {
							intersectingCells.add(new Cell(cartesianGrid, ix, iy, iz, cornerBits, radius));
						}
					}

				} //x
            }// y
        } //z
        System.out.println("Intersecting cells count: " + intersectingCells.size());
        Cell.report(intersectingCells);
    }

    //do the hideous kiss test (this is the test devised by chatGPT)
    private int kissTestGPT(double[][] corners, double sphereRadius) {
    	
    	//get the closest face
    	int closestFace = -1;
    	double minDist = Double.MAX_VALUE;
		for (int face = 0; face < 6; face++) {
			double distsq = GridSupport.faceAverageDistanceSquare(corners, face);
			if (distsq < minDist) {
				minDist = distsq;
				closestFace = face;
			}
		}
		
		double[][] faceCorners = GridSupport.getFaceCorners(corners, closestFace);
		double[] closePoint = ClosestPointOnFace.closestPointOnFace(faceCorners);
		
		if (closePoint == null) {
			return -1;
		}
		double x = closePoint[0], y = closePoint[1], z = closePoint[2];
		double distsq = x * x + y * y + z * z;
		if (distsq < sphereRadius * sphereRadius) {
			return closestFace;
        }
   	
    	return -1;
    }
    
    //do the hideous kiss test (this is the test used previously)
    private int kissTest(double[][] corners, double sphereRadius) {

    	//get the closest face
    	int closestFace = -1;
    	double minDist = Double.MAX_VALUE;
		for (int face = 0; face < 6; face++) {
			double distsq = GridSupport.faceAverageDistanceSquare(corners, face);
			if (distsq < minDist) {
				minDist = distsq;
				closestFace = face;
			}
		}
		
		double[][] faceCorners = GridSupport.getFaceCorners(corners, closestFace);

		Point3D.Double p0 = new Point3D.Double(faceCorners[0][0], faceCorners[0][1], faceCorners[0][2]);
		Point3D.Double p1 = new Point3D.Double(faceCorners[1][0], faceCorners[1][1], faceCorners[1][2]);
		Point3D.Double p2 = new Point3D.Double(faceCorners[2][0], faceCorners[2][1], faceCorners[2][2]);
		Point3D.Double v = new Point3D.Double();
		
		ChimeraPlane plane = new ChimeraPlane(p0, p1, p2);
		plane.getPerpendicular(v);
		
		if (plane.isInAndContained(v) && v.length() < sphereRadius) {
			return closestFace;
		}
    	
    	return -1;
    }

    	 
    

}
