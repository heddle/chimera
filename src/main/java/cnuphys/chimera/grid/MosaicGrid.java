package cnuphys.chimera.grid;


import java.util.ArrayList;
import java.util.List;

import cnuphys.bCNU.util.Bits;
import cnuphys.chimera.curve.Patch;
import cnuphys.chimera.util.ClosestFacePoint;
import cnuphys.chimera.util.Point3D;
/**
 * Represents a Chimera grid, which combines a CartesianGrid and a SphericalGrid.
 * This class identifies and stores the indices of Cartesian grid cells that intersect
 * the sphere defined by the SphericalGrid.
 */
public class MosaicGrid {
    
    private final CartesianGrid cartesianGrid;
    private final SphericalGrid sphericalGrid;
    
    //the list of all intersecting cells
    private List<Cell> intersectingCells = new ArrayList<>();
    
    private List<Patch> _prePatches = new ArrayList<>();

    public MosaicGrid(CartesianGrid cartesianGrid, SphericalGrid sphericalGrid) {
        this.cartesianGrid = new CartesianGrid(cartesianGrid); 
        this.sphericalGrid = new SphericalGrid(sphericalGrid);
    }

    public MosaicGrid(MosaicGrid other) {
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

        //get the bulk limits for efficiency
        int[] xBulkLims = cartesianGrid.getXGrid().bulkFilterLimits(radius);
        int[] yBulkLims = cartesianGrid.getYGrid().bulkFilterLimits(radius);
        int[] zBulkLims = cartesianGrid.getZGrid().bulkFilterLimits(radius);

		for (int iz = zBulkLims[0]; iz <= zBulkLims[1]; iz++) {
			for (int iy = yBulkLims[0]; iy <= yBulkLims[1]; iy++) {
				for (int ix = xBulkLims[0]; ix <= xBulkLims[1]; ix++) {
					
					double[][] cellCorners = GridSupport.getCellCorners(cartesianGrid, ix, iy, iz);

			        int cornerBits = 0;
					boolean hasInside = false;
					boolean hasOutside = false;

					// check each corner of the cell
					for (int canonicalCorner = 0; canonicalCorner < 8; canonicalCorner++) {
						double[] corner = cellCorners[canonicalCorner];
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
                    	Cell cell = new Cell(cartesianGrid, ix, iy, iz, cornerBits, radius);
                        intersectingCells.add(cell);
                        Patch prePatch = new Patch(cell.getBoundaryCurves(), ix, iy, iz, -1, -1);
                        _prePatches.add(prePatch);
                        cell.setPrepatch(prePatch);
                    }
                    else if (!hasInside) {
						Point3D.Double closestPoint = new Point3D.Double();
						int kissFace = kissTest(cellCorners, radius, closestPoint);
						if (kissFace >= 0) {
							Cell kissCell = new Cell(cartesianGrid, ix, iy, iz, cornerBits, radius);
							kissCell.setClosestPoint(closestPoint);
							intersectingCells.add(kissCell);
						}
					}

				} //x
            }// y
        } //z
        System.out.println("Intersecting cells count: " + intersectingCells.size());
        Cell.report(intersectingCells);
        
        //total area of the patches
        double totalArea = 0;
		for (Patch patch : _prePatches) {
			double patchArea = patch.areaEstimate(5);
			System.out.println("Patch area: " + patchArea);
			totalArea += patchArea;
		}
        System.out.println("Prepatch count: " + _prePatches.size() + " Total normalized area: " + totalArea);
        
    }

    //do the hideous kiss test (this is the test devised by chatGPT)
    private int kissTest(double[][] corners, double sphereRadius, Point3D.Double closestPoint) {
    	
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
		double[] closePoint = ClosestFacePoint.closestPointOnFaceToOrigin(faceCorners, ClosestFacePoint.TOL);
		double dist = Math
				.sqrt(closePoint[0] * closePoint[0] + closePoint[1] * closePoint[1] + closePoint[2] * closePoint[2]);
		if (dist < sphereRadius) {
			closestPoint.setLocation(closePoint[0], closePoint[1], closePoint[2]);
			return closestFace;
		}

		return -1;
    }
}
