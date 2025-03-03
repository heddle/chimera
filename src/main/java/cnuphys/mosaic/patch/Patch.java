package cnuphys.mosaic.patch;

import java.util.List;

import cnuphys.mosaic.curve.BaseCurve;
import cnuphys.mosaic.grid.CartesianGrid;
import cnuphys.mosaic.grid.SphericalGrid;
import cnuphys.mosaic.util.Point3D;
import cnuphys.mosaic.util.SphericalVector;

/**
 * Represents a closed patch formed by a sequence of Curve objects.
 * This is the "full and final" patch that is used in the Mosaic algorithm.
 */

public class Patch extends BasePatch {



    /**
     * Constructs a Patch from a list of Curve objects.
     *
     * @param cartGrid The Cartesian grid
     * @param sphGrid The Spherical grid
     * @param curves List of GeneralCurve objects forming a closed loop.
     * @param nx The x rectangular grid index
     * @param ny The y rectangular grid index
     * @param nz The z rectangular grid index
     * @param ntheta The theta spherical grid index
     * @param nphi The phi spherical grid index
     * @throws IllegalArgumentException if the curves do not form a closed loop.
     */
    public Patch(CartesianGrid cartGrid, SphericalGrid sphGrid, List<BaseCurve> curves, int nx, int ny, int nz, int ntheta, int nphi) {
		super(cartGrid, sphGrid, curves, nx, ny, nz, ntheta, nphi);
	}

    /**
     * Is a given point contained in this ThetaPatch?
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return <code>true</code> if the point is contained in the theta patch
     */
	@Override
	public boolean containsPoint(double x, double y, double z) {
		int ix = xGrid.locateInterval(x);
		int iy = yGrid.locateInterval(y);
		int iz = zGrid.locateInterval(z);
		Point3D.Double p = new Point3D.Double(x, y, z);
        SphericalVector sv = new SphericalVector(p);

		int itheta = thetaGrid.locateInterval(sv.theta);
		int iphi = phiGrid.locateInterval(sv.phi);
		return tuple.matches(ix, iy, iz, itheta, iphi);
	}

	@Override
	public boolean curveContained(BaseCurve curve) {
		// TODO Auto-generated method stub
		return false;
	}
}

