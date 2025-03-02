package cnuphys.mosaic.patch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;

import cnuphys.mosaic.curve.GeneralCurve;
import cnuphys.mosaic.curve.TValue;
import cnuphys.mosaic.curve.ThetaCurve;
import cnuphys.mosaic.grid.Grid1D;
import cnuphys.mosaic.grid.MosaicGrid;
import cnuphys.mosaic.util.Point3D;
import cnuphys.mosaic.util.SphericalVector;

public class ThetaSplicer {
	
	public static final double TOL = 1.0e-8;
	
	public static List<Patch> thetaSplice(MosaicGrid grid) {
		return null;
	}
	
	/**
	 * Find the constant theta curves in a prepatch
	 * @param prepatch the prepatch
	 * @return the theta curves
	 */
	public static List<ThetaCurve> findThetaCurves(Prepatch prepatch) {


		ArrayList<ThetaCurve> curves = new ArrayList<ThetaCurve>();
		double R = prepatch.getRadius();
		List<GeneralCurve> ppCurves = prepatch.getCurves();
		final double dPhiMax = prepatch.getSphericalGrid().getPhiGrid().getMaxSpacing();

		//loop over the curves in the prepatch
		for (GeneralCurve gc : ppCurves) {

			// get the theta crossings for the current curve
			List<TValue> tcs = gc.getThetaCrossings();
			for (TValue tc : tcs) {
				double theta = tc.value;
				double phi0 = tc.curve.phi(tc.t);
				double dphi = 1e-5;

				// try both directions, the wrong one should take us out of the patch
				double phi1 = phi0 + dphi;
				double x = R * Math.sin(theta) * Math.cos(phi1);
				double y = R * Math.sin(theta) * Math.sin(phi1);
				double z = R * Math.cos(theta);

				boolean inPatch = prepatch.containsPoint(x, y, z);
				System.err.println("inPatch way 1: " + inPatch);

//				if (inPatch) {
//
//					dphi = 1.01 * dPhiMax;
//					UnivariateFunction phiFunction = new UnivariateFunction() {
//						@Override
//						public double value(double t) {
//							return phi0 + dPhiMax * t;
//						}
//					};
//					
//					double t = findTogglePoint(phiFunction, theta, prepatch, 1.0e-8);
//
//					continue;
//				}

				dphi = -1e-5;
				phi1 = phi0 + dphi;
				x = R * Math.sin(theta) * Math.cos(phi1);
				y = R * Math.sin(theta) * Math.sin(phi1);
				z = R * Math.cos(theta);
				
				inPatch = prepatch.containsPoint(x, y, z);
				System.err.println("inPatch way 2: " + inPatch);
				
				System.err.println("Theta curve didn't work either way ");
		//		System.exit(1);

			}
		}

		return null;
	}
	
	public static double findTogglePoint(UnivariateFunction phiFunction, double theta, Prepatch prepatch, double tol) {
		double tLow = 0.0;
		double tHigh = 1.0;

// Evaluate the initial containment state
		boolean initialTest = contained(phiFunction, theta, prepatch, tLow);

// Binary search to find the toggle point
		while (tHigh - tLow > tol) {
			double tMid = (tLow + tHigh) / 2.0;
			boolean midTest = contained(phiFunction, theta, prepatch, tMid);

			if (midTest == initialTest) {
				tLow = tMid; // Move right
			} else {
				tHigh = tMid; // Move left
			}
		}

		return (tLow + tHigh) / 2.0; // Best estimate of the toggle point
	}
	
	private static boolean contained(UnivariateFunction phiFunction, double theta, Prepatch prepatch, double t) {
        double R = prepatch.getRadius();
        double phi = phiFunction.value(t);
        double x = R * Math.sin(theta) * Math.cos(phi);
        double y = R * Math.sin(theta) * Math.sin(phi);
        double z = R * Math.cos(theta);
        return prepatch.containsPoint(x, y, z);
	}


//	public static List<ThetaCurve> findThetaCurves(Patch prepatch) {
//		
//		if (!prepatch.isPrepatch()) {
//			System.err.println("Not a prepatch in findThetaCurves");
//			(new Throwable()).printStackTrace();
//			System.exit(1);
//		}
//		
//		ArrayList<ThetaCurve> curves = new ArrayList<ThetaCurve>();
//		
//		List<GeneralCurve> ppCurves = prepatch.getCurves();
//		ArrayList<TValue> thetaCrossings = new ArrayList<TValue>();
//		
//		for (GeneralCurve gc : ppCurves) {
//			List<TValue> tcs = gc.getThetaCrossings();
//			if (tcs != null) {
//				thetaCrossings.addAll(tcs);
//			}
//		}
//
//		// now match thetas
//		while (thetaCrossings.size() > 1) {
//			TValue tc0 = thetaCrossings.remove(0);
//
//			double phi0;
//			double phi1;
//			double theta = tc0.value;
//			phi0 = tc0.curve.phi(tc0.t);
//			double R = prepatch.getRadius();
//			boolean foundMatch = false;
//
//			// find the matching theta
//			for (int i = 0; i < thetaCrossings.size(); i++) {
//				TValue tc1 = thetaCrossings.get(i);
//				if (Math.abs(tc1.value - theta) < TOL) {
//					System.err.println("FOUND MATCH for THETA " + theta);
//					foundMatch = true;
//					thetaCrossings.remove(i);
//					phi1 = tc1.curve.phi(tc1.t);
//					// create a theta curve
//					ThetaCurve tc = ThetaCurve.createThetaCurve(prepatch, theta, phi0, phi1, R);
//					curves.add(tc);
//					break;
//				}
//			}
//			
//			if (!foundMatch) {
//				System.err.println("NO MATCH for THETA " + theta);
//				System.exit(1);
//			}
//
//		} //end while
//
//		return curves;
//	}

}