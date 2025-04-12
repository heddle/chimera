package cnuphys.mosaic.patch;

import java.util.ArrayList;
import java.util.List;

import cnuphys.mosaic.curve.BaseCurve;
import cnuphys.mosaic.curve.PhiCurve;
import cnuphys.mosaic.curve.TValue;
import cnuphys.mosaic.curve.ThetaCurve;
import cnuphys.mosaic.grid.CartesianGrid;
import cnuphys.mosaic.grid.SphericalGrid;

public class ThetaPatch extends BasePatch {

	private ArrayList<TValue> _phiCrossings;

	// the phi curves
	private ArrayList<PhiCurve> _phiCurves;

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
	public ThetaPatch(CartesianGrid cartGrid, SphericalGrid sphGrid, List<BaseCurve> curves, int nx, int ny, int nz) {
		super(cartGrid, sphGrid, curves, nx, ny, nz, avgThetaIndex(curves, sphGrid));
		getPhiCrossings();
	}
	
	/**
	 * Get the t values where a patch curve crosses a phi grid line
	 *
	 * @return a list of t values
	 */
	public List<TValue> getPhiCrossings() {
		if (_phiCrossings == null) {
			_phiCrossings = new ArrayList<>();

			for (BaseCurve curve : curves) {
				_phiCrossings.addAll(curve.getPhiCrossings());
			}

			// TODO deal with edge case where curve is its own patch
		}

		return _phiCrossings;
	}
	
	
	/**
	 * Get the theta curves that will be used to make theta patches
	 *
	 * @return the theta curves
	 */
	public List<PhiCurve> getPhiCurves() {
		if (_phiCurves == null) {
			_phiCurves = new ArrayList<>();

			// step one: make a shallow copy of the phi crossings
			ArrayList<TValue> tvals = new ArrayList<>(getPhiCrossings());
			
			for (int i = 0; i < tvals.size(); i++) {
                TValue tval0 = tvals.get(i);
				double phi0 = tval0.value;
				double theta0 = tval0.curve.theta(tval0.t);
				boolean isPole = (Math.abs(theta0) < 1.0e04) || (Math.abs(Math.PI - theta0) < 1.0e-04);				
				
				
				//TODO only consider the first pole
				//TODO only connEct to the max theta not a pole
				
				
				for (int j = 1; j < tvals.size(); j++) {
					if (i == j) {
						continue;
					}
					TValue tval1 = tvals.get(j);
					double phi1 = tval1.value;
					double theta1 = tval1.curve.theta(tval1.t);
					if (!isPole) {
						isPole = (Math.abs(theta1) < 1.0e04) || (Math.abs(Math.PI - theta1) < 1.0e-04);
					}
					
					if (isPole || Math.abs(phi1 - phi0) < TOL) {
						PhiCurve tc = PhiCurve.createPhiCurve(theta0, theta1, phi0, R);

						if (tc.pathLength() < 1.0e-05) {
							System.err.println("Phi curve too short: " + tc);
						} else {
							_phiCurves.add(tc);
							System.out.println("Phi curve: " + tc);
						}
					}
				}
            }
			
			
			

//			ArrayList<TValue> unused = new ArrayList<>();
//			while (!tvals.isEmpty()) {
//				TValue tval = tvals.remove(0);
//				double theta0 = tval.curve.theta(tval.t);
//				
//				boolean isPole = (Math.abs(theta0) < 1.0e04) || (Math.abs(Math.PI - theta0) < 1.0e-04);
//				double phi0 = tval.value;
//				
//				boolean connected = false;
//				for (int i = 0; i < tvals.size(); i++) {
//					double phi1 = tvals.get(i).value;
//					if (isPole || Math.abs(phi1 - phi0) < TOL) {
//						double theta1 = tvals.get(i).curve.theta(tvals.get(i).t);
//						PhiCurve tc = PhiCurve.createPhiCurve(theta0, theta1, phi0, R);
//						if (tc.pathLength() < 1.0e-05) {
//							System.err.println("Phi curve too short: " + tc);
//						} else {
//							_phiCurves.add(tc);
//							System.out.println("Phi curve: " + tc);
//							tvals.remove(i);
//							connected = true;
//							break;
//						}
////						if (curveContained(tc)) {
////							_phiCurves.add(tc);
////							tvals.remove(i);
////							break;
////						} else { // try other way
////							System.err.println("Need a phi curve flip?");
////						}
//					} // if close enough
//				} //end loop
//				if (!connected) {
//					unused.add(tval);
//				}
//			}
//
//			System.out.println("Unused: " + unused.size());
		}
		
		BaseCurve.removeDuplicateCurves(curves);
		return _phiCurves;
	}
	


	/**
	 * This is used for a prepatch that has no  crossings, covers the entire
	 * area of the prepatch.
	 * @param prepatch the prepatch with no theta crossings
	 * @return a ThetaPatch that covers the entire area of the prepatch
	 */
	public static ThetaPatch createFromPrepatch(Prepatch prepatch) {
		//check
		return new ThetaPatch(prepatch.getCartesianGrid(), prepatch.getSphericalGrid(), prepatch.getCurves(),
				prepatch.getTuple().getNx(), prepatch.getTuple().getNy(), prepatch.getTuple().getNz());
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
