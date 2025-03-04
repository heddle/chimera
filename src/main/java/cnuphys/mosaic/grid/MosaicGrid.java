package cnuphys.mosaic.grid;


import java.util.ArrayList;
import java.util.List;

import cnuphys.bCNU.util.Bits;
import cnuphys.mosaic.patch.Patch;
import cnuphys.mosaic.patch.Prepatch;
import cnuphys.mosaic.patch.ThetaPatch;
import cnuphys.mosaic.util.ClosestPointToOrigin;
import cnuphys.mosaic.util.Point3D;
/**
 * Represents a ChimMosaicera grid, which combines a CartesianGrid and a SphericalGrid.
 * This class identifies and stores the indices of Cartesian grid cells that intersect
 * the sphere defined by the SphericalGrid.
 */
public class MosaicGrid {

	//the two comprising grids
    private final CartesianGrid _cartesianGrid;
    private final SphericalGrid _sphericalGrid;

    //the list of all intersecting cells
    private List<Cell> _intersectingCells = new ArrayList<>();

    //prepatches from the intersecting cell
    private List<Prepatch> _prepatches = new ArrayList<>();

    //theta patches from theta splicing the prepatches

    private List<ThetaPatch> _thetaPatches = new ArrayList<>();

    //the final patches from phi splicing the theta patches
    private List<Patch> _patches = new ArrayList<>();

    public MosaicGrid(CartesianGrid cartesianGrid, SphericalGrid sphericalGrid) {
        _cartesianGrid = new CartesianGrid(cartesianGrid);
        _sphericalGrid = new SphericalGrid(sphericalGrid);
    }

	/**
	 * Get the Cartesian grid
	 *
	 * @return the Cartesian grid
	 */
	public CartesianGrid getCartesianGrid() {
		return _cartesianGrid;
	}

	/**
	 * Get the spherical grid
	 *
	 * @return the spherical grid
	 */
	public SphericalGrid getSphericalGrid() {
		return _sphericalGrid;
	}

	/**
	 * Get the intersecting cells
	 *
	 * @return the intersecting cells
	 */
	public List<Cell> getIntersectingCells() {
		return _intersectingCells;
	}

	/**
	 * Get the prepatches
	 *
	 * @return the prepatches
	 */
	public List<Prepatch> getPrepatches() {
		return _prepatches;
	}

	/**
	 * Get the theta patches
	 *
	 * @return the theta patches
	 */
	public List<ThetaPatch> getThetaPatches() {
		return _thetaPatches;
	}

	/**
	 * Get the patches
	 *
	 * @return the patches
	 */
	public List<Patch> getPatches() {
		return _patches;
	}

	/**
	 * Reset to initial states, clearing all lists
	 */
    public void reset() {
        _intersectingCells.clear();
        _prepatches.clear();
        _thetaPatches.clear();
        _patches.clear();
    }

	/**
	 * Identifies the Cartesian grid cells that intersect the sphere defined by the
	 * SphericalGrid. This is the first step of the algorithm.
	 */

    public void runMosaicAlgorithm() {
        reset();

        //step1 fine prepatches
        findPrepatches();
        
        //step2 find theta patches
        findThetaPatches();
        
        //step3 find patches
        findPatches();
     
    }

    //find the intersecting cells and the prepatches
	private void findPrepatches() {
		int kisscount = 0;
		
		System.out.println("\nStep 1: Finding prepatches");
		double radius = _sphericalGrid.getRadius();
		double rSquared = radius * radius;

		// get the bulk limits for efficiency
		int[] xBulkLims = _cartesianGrid.getXGrid().bulkFilterLimits(radius);
		int[] yBulkLims = _cartesianGrid.getYGrid().bulkFilterLimits(radius);
		int[] zBulkLims = _cartesianGrid.getZGrid().bulkFilterLimits(radius);

		for (int iz = zBulkLims[0]; iz <= zBulkLims[1]; iz++) {
			for (int iy = yBulkLims[0]; iy <= yBulkLims[1]; iy++) {
				for (int ix = xBulkLims[0]; ix <= xBulkLims[1]; ix++) {

					double[][] cellCorners = GridSupport.getCellCorners(_cartesianGrid, ix, iy, iz);

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
					} // end loop over corners

					// if have corners on both sides of the sphere, then the cell intersects
					// if no traditional intersection, must do the kiss test!
					// for cells with no inside points
					if (hasInside && hasOutside) {
						Cell cell = new Cell(_cartesianGrid, ix, iy, iz, cornerBits, radius);
						_intersectingCells.add(cell);
						Prepatch prePatch = new Prepatch(_cartesianGrid, _sphericalGrid, cell.getBoundaryCurves(), ix,
								iy, iz);
						_prepatches.add(prePatch);
						cell.setPrepatch(prePatch);
					} else if (!hasInside) {
						Point3D.Double closestPoint = new Point3D.Double();
						int kissFace = kissTest(cellCorners, radius, closestPoint);
						if (kissFace >= 0) {
							Cell kissCell = new Cell(_cartesianGrid, ix, iy, iz, cornerBits, radius);
							kissCell.setClosestPoint(closestPoint);
							_intersectingCells.add(kissCell);
						}
					}

				} // x
			} // y
		} // z
		
		System.out.println("Intersecting cells count: " + _intersectingCells.size());
		Cell.report(_intersectingCells);

		// total area of the prepatches
		double totalArea = 0;
		for (Prepatch patch : _prepatches) {
			double patchArea = patch.areaEstimate(10);
			totalArea += patchArea;
		}
		System.out.println("Prepatch count: " + _prepatches.size() + " Total normalized area: " + totalArea);

	}
	
	//step 2 find theta patches
	private void findThetaPatches() {
		System.out.println("\nStep 2: Finding theta patches");
		for (Prepatch prepatch : _prepatches) {
			List<ThetaPatch> thetaPatches =  prepatch.getThetaPatches();
			_thetaPatches.addAll(thetaPatches);
		}
		// total area of the prepatches
		double totalArea = 0;
		for (ThetaPatch patch : _thetaPatches) {
			double patchArea = patch.areaEstimate(10);
			totalArea += patchArea;
		}
		System.out.println("ThetaPatch count: " + _thetaPatches.size() + " Total normalized area: " + totalArea);
	}
	
	//step 3 find patches
	private void findPatches() {
		System.out.println("\nStep 3: Finding patches");
//		for (ThetaPatch thetaPatch : _thetaPatches) {
//			List<Patch> patches = thetaPatch.getPatches();
//			_patches.addAll(patches);
//		}
		System.out.println("Patch count: " + _patches.size());
	}


    //do the hideous kiss test (this is the test devised by chatGPT)
    private int kissTest(double[][] corners, double sphereRadius, Point3D.Double closestPoint) {

    	//get the closest face
    	int closestFace = GridSupport.getClosestFaceToOrigin(corners);

//		double[][] faceCorners = GridSupport.getFaceCorners(corners, closestFace);
//		double[] closePoint = ClosestFacePoint.closestPointOnFaceToOrigin(faceCorners, ClosestFacePoint.TOL);
		double [] closePoint = ClosestPointToOrigin.closestPointOnFaceToOrigin(corners);
		double dist = Math
				.sqrt(closePoint[0] * closePoint[0] + closePoint[1] * closePoint[1] + closePoint[2] * closePoint[2]);
		if (dist < sphereRadius) {
			closestPoint.setLocation(closePoint[0], closePoint[1], closePoint[2]);
			return closestFace;
		}

		return -1;
    }
}
