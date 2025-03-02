package cnuphys.mosaic.patch;

import java.util.List;

import cnuphys.mosaic.curve.GeneralCurve;
import cnuphys.mosaic.grid.CartesianGrid;
import cnuphys.mosaic.grid.SphericalGrid;

public class ThetaPatch extends BasePatch {


    
	 
    /**
     * Constructs a ThetaPatch from a list of Curve objects.
     * 
     * @param cartGrid The Cartesian grid
     * @param sphGrid The Spherical grid
     * @param curves List of GeneralCurve objects forming a closed loop.
     * @param nx The x rectangular grid index
     * @param ny The y rectangular grid index
     * @param nz The z rectangular grid index
     * @param ntheta The theta spherical grid index
     * @throws IllegalArgumentException if the curves do not form a closed loop.
     */
    public ThetaPatch(CartesianGrid cartGrid, SphericalGrid sphGrid, List<GeneralCurve> curves, int nx, int ny, int nz, int ntheta) {
		super(cartGrid, sphGrid, curves, nx, ny, nz, ntheta);
	}
    
    /**
     * Is a given point contained in this ThetaPatch?
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param theta the theta coordinate
     * @return <code>true</code> if the point is contained in the theta patch
     */
	public boolean containsPoint(double x, double y, double z, double theta) {
		int ix = xGrid.locateInterval(x);
		int iy = yGrid.locateInterval(y);
		int iz = zGrid.locateInterval(z);
		int itheta = thetaGrid.locateInterval(theta);
		return tuple.matches(ix, iy, iz, itheta);
	}

}
