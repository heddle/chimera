package cnuphys.mosaic.patch;

import java.util.ArrayList;
import java.util.List;

import cnuphys.mosaic.curve.GeneralCurve;
import cnuphys.mosaic.grid.CartesianGrid;
import cnuphys.mosaic.grid.Grid1D;
import cnuphys.mosaic.grid.SphericalGrid;
import cnuphys.mosaic.util.Point3D;
import cnuphys.mosaic.util.SphericalPolygonArea;
import cnuphys.mosaic.util.ThetaPhi;

public class BasePatch {

    protected List<GeneralCurve> curves; // The curves forming the patch
    protected double R; // Radius of the sphere
    
    //the grids
    protected CartesianGrid cartesianGrid;
    protected SphericalGrid sphericalGrid;
    
    //the 1D grids
    protected Grid1D xGrid;
    protected Grid1D yGrid;
    protected Grid1D zGrid;
    protected Grid1D thetaGrid;
    protected Grid1D phiGrid;
    
    //the tuple of indices
    protected Tuple tuple;


    /**
     * Constructs a BasePatch from a list of Curve objects.
     * 
     * @param curves List of GeneralCurve objects forming a closed loop.
     * @throws IllegalArgumentException if the curves do not form a closed loop.
     */
    public BasePatch(CartesianGrid cartGrid, SphericalGrid sphGrid, List<GeneralCurve> curves, int ... indices) {
        if (curves == null || curves.size() < 3) {
            throw new IllegalArgumentException("A Patch must have at least three curves to form a closed loop.");
        }
        this.cartesianGrid = cartGrid;
        this.sphericalGrid = sphGrid;
        this.curves = curves;
        this.tuple = new Tuple(indices);
        this.R = curves.get(0).getRadius(); // Assume all curves have the same radius
        validateLoop();
    }
    
    /**
     * Validates that the curves form a continuous closed loop.
     * Throws an exception if any curve does not connect properly.
     */
    private void validateLoop() {
        for (int i = 0; i < curves.size(); i++) {
            GeneralCurve current = curves.get(i);
            GeneralCurve next = curves.get((i + 1) % curves.size()); // Wrap around for last-to-first

            Point3D.Double currentEnd = current.getP1();
            Point3D.Double nextStart = next.getP0();

            if (!pointsAreClose(currentEnd, nextStart)) {
                throw new IllegalArgumentException("Curves do not form a continuous loop. Mismatch between endpoints.");
            }
        }
    }
    
    
    /**
     * Checks if two points are close enough to be considered the same.
     *
     * @param p1 First point.
     * @param p2 Second point.
     * @return true if the points are close within a tolerance, false otherwise.
     */
    private boolean pointsAreClose(Point3D.Double p1, Point3D.Double p2) {
        final double TOLERANCE = 1.0e-6;
        return Math.abs(p1.x - p2.x) < TOLERANCE &&
               Math.abs(p1.y - p2.y) < TOLERANCE &&
               Math.abs(p1.z - p2.z) < TOLERANCE;
    }

    /**
     * Computes the perimeter of the closed loop by summing the lengths of the curves.
     *
     * @return The perimeter of the patch.
     */
    public double perimeter() {
        double totalLength = 0.0;
        for (GeneralCurve curve : curves) {
            totalLength += curve.pathLength();
        }
        return totalLength;
    }
    
	/**
	 * Gets a list of vertices useful for estimating area or drawing
	 * 
	 * @return A list of ThetaPhi vertices.
	 */
	public List<ThetaPhi> getSphericalVertices(int n) {
		
		double R = curves.get(0).getRadius();
		List<ThetaPhi> spVertices = new ArrayList<>();

		double step = 1.0 / n;

		for (GeneralCurve curve : curves) {
			for (int i = 0; i < n; i++) {
				double t = i * step;
				double theta = curve.theta(t);
				double phi = curve.phi(t);
				ThetaPhi point = new ThetaPhi(R, theta, phi);
				spVertices.add(point);
			}
		}

		return spVertices;
	}
	
	
	/**
	 * Getter for the Cartesian grid
	 * @return the Cartesian grid
	 */
	public CartesianGrid getCartesianGrid() {
		return cartesianGrid;
	}
	
	/**
	 * Getter for the spherical grid
	 * 
	 * @return the spherical grid
	 */
	public SphericalGrid getSphericalGrid() {
		return sphericalGrid;
	}
	
	/**
	 * Getter for the tuple of indices
	 * 
	 * @return the tuple of indices
	 */
	public Tuple getTuple() {
		return tuple;
	}

    /**
	 * Estimates the area of the spherical patch using the area excess formula. It
	 * generates 'n' ThetaPhi points along the perimeter and calls
	 * computeSphericalPolygonArea.
	 *
	 * @param n Number of points to sample along the perimeter.
	 * @return The estimated area of the patch.
	 */
	public double areaEstimate(int n) {
		List<ThetaPhi> spVertices = getSphericalVertices(n);

		return SphericalPolygonArea.computeSphericalArea(spVertices);
	}

   
    /**
     * Returns the list of curves forming the patch.
     *
     * @return The list of curves.
     */
    public List<GeneralCurve> getCurves() {
        return curves;
    }
    
	
	/**
	 * Get the radius of the sphere
	 * 
	 * @return the radius
	 */
	public double getRadius() {
		return R;
	}

	/**
	 * Get the bounding box of the patch in spherical coordinates.
	 * @return the bounding box as an array of ThetaPhi objects
	 */
	public ThetaPhi[] getBoundingBox() {
	    List<ThetaPhi> vertices = getSphericalVertices(50); // Sample 50 points per curve for accuracy

	    if (vertices.isEmpty()) {
	        throw new IllegalStateException("Patch has no vertices.");
	    }

	    double thetaMin = Double.POSITIVE_INFINITY;
	    double thetaMax = Double.NEGATIVE_INFINITY;
	    double phiMin = Double.POSITIVE_INFINITY;
	    double phiMax = Double.NEGATIVE_INFINITY;

	    for (ThetaPhi tp : vertices) {
	        double theta = tp.getTheta();
	        double phi = tp.getPhi();
	        if (theta < thetaMin) thetaMin = theta;
	        if (theta > thetaMax) thetaMax = theta;

	        if (phi < phiMin) phiMin = phi;
	        if (phi > phiMax) phiMax = phi;
	    }

	    // Ensure the shortest path is taken in longitude
	    if (phiMax - phiMin > Math.PI) {
	        // Wrap around case, adjust phi limits
	        double newPhiMin = Double.MAX_VALUE;
	        double newPhiMax = Double.MIN_VALUE;
	        for (ThetaPhi tp : vertices) {
	            double phi = tp.getPhi();
	            if (phi < 0) phi += 2 * Math.PI; // Shift to [0, 2π] for correct min/max
	            if (phi < newPhiMin) newPhiMin = phi;
	            if (phi > newPhiMax) newPhiMax = phi;
	        }
	        phiMin = newPhiMin;
	        phiMax = newPhiMax;
	    }

	    ThetaPhi tp0 = new ThetaPhi(R, thetaMin, phiMin);
	    ThetaPhi tp1 = new ThetaPhi(R, thetaMax, phiMax);

	    return new ThetaPhi[]{tp0, tp1};
	}


}
