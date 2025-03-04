package cnuphys.mosaic.patch;

import java.util.ArrayList;
import java.util.List;

import cnuphys.mosaic.curve.BaseCurve;
import cnuphys.mosaic.curve.CurveSplitter;
import cnuphys.mosaic.curve.CurveValidator;
import cnuphys.mosaic.curve.GeneralCurve;
import cnuphys.mosaic.curve.TValue;
import cnuphys.mosaic.curve.ThetaCurve;
import cnuphys.mosaic.grid.CartesianGrid;
import cnuphys.mosaic.grid.SphericalGrid;
import cnuphys.mosaic.util.Point3D;

public class Prepatch extends BasePatch {

	private ArrayList<TValue> _thetaCrossings;

	// the theta curves
	private ArrayList<ThetaCurve> _thetaCurves;

	// the split curves (prepatch boundary general curves
	// split at theta crossings)
	private ArrayList<BaseCurve> _splitCurves;

	// the theta patches
	private ArrayList<ThetaPatch> _thetaPatches;

	/**
	 * Constructs a Prepatch from a list of Curve objects.
	 *
	 * @param cartGrid The Cartesian grid
	 * @param sphGrid  The Spherical grid
	 * @param curves   List of GeneralCurve objects forming a closed loop.
	 * @param nx       The x rectangular grid index
	 * @param ny       The y rectangular grid index
	 * @param nz       The z rectangular grid index
	 * @throws IllegalArgumentException if the curves do not form a closed loop.
	 */
	public Prepatch(CartesianGrid cartGrid, SphericalGrid sphGrid, List<BaseCurve> curves, int nx, int ny, int nz) {
		super(cartGrid, sphGrid, curves, nx, ny, nz);
		getThetaCrossings();
	}

	/**
	 * Is a given point contained in this Prepatch?
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return <code>true</code> if the point is contained in the Prepatch
	 */
	@Override
	public boolean containsPoint(double x, double y, double z) {
		int ix = xGrid.locateInterval(x);
		int iy = yGrid.locateInterval(y);
		int iz = zGrid.locateInterval(z);
		return tuple.matches(ix, iy, iz);
	}

	/**
	 * Get the split curves that will be used to make the theta patches
	 *
	 * @return the split curves
	 */
	public List<BaseCurve> getSplitCurves() {
		if (_splitCurves == null) {
//			_splitCurves = CurveSplitter.getSplitCurves(curves, getThetaCrossings());
			_splitCurves = CurveSplitter.getSplitCurves(curves);
		}
//		//they should form a valid loop
		if (!BaseCurve.validateLoop(_splitCurves)) {
			// try to fix
			_splitCurves = CurveValidator.returnValidLoop(_splitCurves);
			if (!BaseCurve.validateLoop(_splitCurves)) {
				System.err.println("Split curves do not form a loop");
				System.exit(1);
			}
		}

		return _splitCurves;
	}

	/**
	 * Get the theta curves that will be used to make theta patches
	 *
	 * @return the theta curves
	 */
	public List<ThetaCurve> getThetaCurves() {
		if (_thetaCurves == null) {
			_thetaCurves = new ArrayList<>();

			// step one: make a shallow copy of the theta crossings
			ArrayList<TValue> tvals = new ArrayList<>(getThetaCrossings());

			while (!tvals.isEmpty()) {
				TValue tval = tvals.remove(0);
				double theta0 = tval.value;
				double phi0 = tval.curve.phi(tval.t);
				for (int i = 0; i < tvals.size(); i++) {
					double theta1 = tvals.get(i).value;
					if (Math.abs(theta1 - theta0) < TOL) {
						double phi1 = tvals.get(i).curve.phi(tvals.get(i).t);
						ThetaCurve tc = ThetaCurve.createThetaCurve(theta0, phi0, phi1, R);
						if (curveContained(tc)) {
							_thetaCurves.add(tc);
							tvals.remove(i);
							break;
						} else { // try other way
							tc.flip();
							if (curveContained(tc)) {
								_thetaCurves.add(tc);
								tvals.remove(i);
								break;
							}
						}
					}
				}
			}

			// step two: find the theta curves
		}
		return _thetaCurves;
	}

	/**
	 * Get the t values where a patch curve crosses a theta grid line
	 *
	 * @return a list of t values
	 */
	public List<TValue> getThetaCrossings() {
		if (_thetaCrossings == null) {
			_thetaCrossings = new ArrayList<>();

			for (BaseCurve curve : curves) {
				_thetaCrossings.addAll(curve.getThetaCrossings());
			}

			// TODO deal with edge case where curve is its own patch
		}

		return _thetaCrossings;
	}
	
	/**
	 * Get the t values where a patch curve crosses a phi grid line
	 * Get the from any theta patches
	 * @return a list of t values
	 */
	public List<TValue> getPhiCrossings() {
		ArrayList<TValue> phiCrossings = new ArrayList<>();
		for (ThetaPatch tp : getThetaPatches()) {
			phiCrossings.addAll(tp.getPhiCrossings());
		}

		return phiCrossings;
	}
	

	/**
	 * Get the theta patches
	 *
	 * @return the theta patches
	 */
	public List<ThetaPatch> getThetaPatches() {
		if (_thetaPatches == null) {
			_thetaPatches = new ArrayList<>();

			// simplest case, no theta crossings
			List<ThetaCurve> thetaCurves = getThetaCurves();
			if (thetaCurves.isEmpty()) {
				ThetaPatch tp = ThetaPatch.createFromPrepatch(this);
				_thetaPatches.add(tp);
			} else {
				// make reverse direction copies of all the theta curves
				List<ThetaCurve> allThetaCurves = new ArrayList<>();
				for (ThetaCurve tc : thetaCurves) {
					allThetaCurves.add(tc);
					allThetaCurves.add(tc.reverse());
				}

				// get all all the split curves
				List<BaseCurve> allSplitCurves = new ArrayList<>();
				for (BaseCurve gc : getSplitCurves()) {
					allSplitCurves.add(gc);
				}

				// validate split curves form a valid loop
				if (!BaseCurve.validateLoop(allSplitCurves)) {
					System.err.println("Split curves do not form a loop");
					System.exit(1);
				}

				// now make the theta patches
				// always start with a theta curve
				while (!allThetaCurves.isEmpty()) {

					// the curves that will make up the patch
					List<BaseCurve> patchCurves = new ArrayList<>();

					// always start with a theta curve
					ThetaCurve thetaCurve = allThetaCurves.remove(0);
					BaseCurve currentCurve = thetaCurve;

					boolean success = false;
					patchCurves.add(currentCurve);

					// TODO: check pathological case where curve is its own patch

					boolean done = false;
					while (!done) {

						boolean foundCurve = false;

						// always jump on a theta curve from a split curve if possible
						if (currentCurve instanceof GeneralCurve) {
							for (ThetaCurve tc : allThetaCurves) {
								if (curvesConnect(currentCurve, tc)) {
									allThetaCurves.remove(tc);
									foundCurve = true;
									patchCurves.add(tc);
									done = (tc.connectsTo(thetaCurve));
									if (done) {
										System.err.println("Done on a Theta Curve?????");
										success = true;
									}
									currentCurve = tc;
									break;
								}
							}
						}

						if (!foundCurve) {
							for (BaseCurve splitCurve : allSplitCurves) {
								if (curvesConnect(currentCurve, splitCurve)) {
									allSplitCurves.remove(splitCurve);
									patchCurves.add(splitCurve);
									foundCurve = true;
									done = (splitCurve.connectsTo(thetaCurve));
									if (done) {
										success = true;
									}
									currentCurve = splitCurve;
									break;
								}

							}
						}

						if (!foundCurve) {
							done = true;
							success = false;
							System.err.println("No curve found to connect to, bad theta patch");
						}

					}
					if (success) {
						ThetaPatch tp = new ThetaPatch(cartesianGrid, sphericalGrid, patchCurves, tuple.getNx(),
								tuple.getNy(), tuple.getNz(), thetaCurve.getThetaIndex());
						_thetaPatches.add(tp);
					}

				} // theta curves not empty

				// check area
				double prepatchArea = areaEstimate(5);
				double thetaPatchArea = 0;
				for (ThetaPatch tp : _thetaPatches) {
					thetaPatchArea += tp.areaEstimate(5);
				}
				double ratio = thetaPatchArea / prepatchArea;
				if (Math.abs(ratio - 1) > 0.01) {
					System.err.println(this + "  Bad Theta Patch Area Ratio: " + ratio);
				} 

//				System.out.println("Prepatch Area: " + prepatchArea + " Theta Patch Area: " + thetaPatchArea
//						+ "  Ratio: " + (thetaPatchArea / prepatchArea));

			} // else
		}
		return _thetaPatches;
	}

	// connects with matching theta index
	private boolean curvesConnect(BaseCurve c0, BaseCurve c1) {

		if (c0 instanceof ThetaCurve && c1 instanceof ThetaCurve) {
			return false;
		}

		return c0.connectsTo(c1);

	}

	// a split curve by construction does not cross theta grid line
	private int getSplitCurveThetaIndex(BaseCurve curve) {
		double theta = curve.theta(0.5);
		return sphericalGrid.getThetaGrid().locateInterval(theta);
	}

	@Override
	public boolean curveContained(BaseCurve curve) {
		double delta = 1. / (TEST_N + 1);
		// skip endpoints since they are always contained
		for (int i = 1; i <= TEST_N; i++) {
			double t = i * delta;
			Point3D.Double p = curve.getPoint(t);
			if (!containsPoint(p.x, p.y, p.z)) {
				return false;
			}
		}
		return true;
	}

}
