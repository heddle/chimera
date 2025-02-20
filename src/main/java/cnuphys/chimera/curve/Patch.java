package cnuphys.chimera.curve;

import java.util.ArrayList;
import java.util.List;

import cnuphys.chimera.grid.Fivetuple;
import cnuphys.chimera.util.SphericalPolygonArea;
import cnuphys.chimera.util.Point3D;
import cnuphys.chimera.util.ThetaPhi;

/**
 * Represents a closed patch formed by a sequence of GeneralCurve objects.
 */
public class Patch {

    private List<GeneralCurve> curves;
    private double R; // Radius of the sphere
    
    private Fivetuple fivetuple;

    /**
     * Constructs a Patch from a list of GeneralCurve objects.
     * 
     * @param curves List of GeneralCurve objects forming a closed loop.
     * @throws IllegalArgumentException if the curves do not form a closed loop.
     */
    public Patch(List<GeneralCurve> curves, int nx, int ny, int nz, int ntheta, int nphi) {
        if (curves == null || curves.size() < 3) {
            throw new IllegalArgumentException("A Patch must have at least three curves to form a closed loop.");
        }
        this.curves = curves;
        this.fivetuple = new Fivetuple(nx, ny, nz, ntheta, nphi);
        this.R = curves.get(0).getRadius(); // Assume all curves have the same radius
        validateLoop();
    }
    
    /**
     * Get the patch's fivetuple
     * @return the fivetuple
     */
	public Fivetuple getFivetuple() {
		return fivetuple;
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
	public List<ThetaPhi> getSpehericalVertices(int n) {
		
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
	 * Estimates the area of the spherical patch using the area excess formula. It
	 * generates 'n' ThetaPhi points along the perimeter and calls
	 * computeSphericalPolygonArea.
	 *
	 * @param n Number of points to sample along the perimeter.
	 * @return The estimated area of the patch.
	 */
	public double areaEstimate(int n) {
		List<ThetaPhi> spVertices = getSpehericalVertices(n);

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
}

