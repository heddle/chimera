package cnuphys.mosaic.patch;

import java.util.List;

import cnuphys.mosaic.curve.BaseCurve;
import cnuphys.mosaic.grid.CartesianGrid;
import cnuphys.mosaic.grid.SphericalGrid;

public class ThetaPatch extends BasePatch {

	/**
	 * Constructs a ThetaPatch from a list of Curve objects.
	 *
	 * @param cartGrid The Cartesian grid
	 * @param sphGrid  The Spherical grid
	 * @param curves   List of GeneralCurve objects forming a closed loop.
	 * @param nx       The x rectangular grid index
	 * @param ny       The y rectangular grid index
	 * @param nz       The z rectangular grid index
	 * @param ntheta   The theta spherical grid index
	 * @throws IllegalArgumentException if the curves do not form a closed loop.
	 */
	public ThetaPatch(CartesianGrid cartGrid, SphericalGrid sphGrid, List<BaseCurve> curves, int nx, int ny, int nz,
			int ntheta) {
		super(cartGrid, sphGrid, curves, nx, ny, nz, ntheta);
	}

	/**
	 * This is used for a prepatch that has no  crossings, covers the entire
	 * area of the prepatch.
	 * @param prepatch the prepatch with no theta crossings
	 * @return a ThetaPatch that covers the entire area of the prepatch
	 */
	public static ThetaPatch createFromPrepatch(Prepatch prepatch) {
		//check
		if (prepatch.getThetaCrossings().size() > 0) {
			throw new IllegalArgumentException("Prepatch has theta crossings.");
		}

		BaseCurve curve = prepatch.getCurves().get(0);
		double theta = curve.getSV0().theta;
		int itheta = prepatch.getSphericalGrid().getThetaGrid().locateInterval(theta);
		return new ThetaPatch(prepatch.getCartesianGrid(), prepatch.getSphericalGrid(), prepatch.getCurves(),
				prepatch.getTuple().getNx(), prepatch.getTuple().getNy(), prepatch.getTuple().getNz(), itheta);
	}

	/**
	 * Is a given point contained in this ThetaPatch?
	 *
	 * @param x     the x coordinate
	 * @param y     the y coordinate
	 * @param z     the z coordinate
	 * @return <code>true</code> if the point is contained in the theta patch
	 */
	@Override
	public boolean containsPoint(double x, double y, double z) {
		int ix = xGrid.locateInterval(x);
		int iy = yGrid.locateInterval(y);
		int iz = zGrid.locateInterval(z);
		double theta = Math.acos(z / R);
		int itheta = thetaGrid.locateInterval(theta);
		return tuple.matches(ix, iy, iz, itheta);
	}

	@Override
	public boolean curveContained(BaseCurve curve) {
		// TODO Auto-generated method stub
		return false;
	}

}
