package cnuphys.chimera.util;


public class ClosestFacePoint {
	
	public static double TOL = 1.0e-6;

	public static double[] closestPointOnFaceToOrigin(double[][] corners, double tol) {
		// 1) Identify which coordinate is constant: x=0, y=1, or z=2
		int constCoord = findConstantCoordinate(corners, tol);
		if (constCoord < 0) {
			throw new IllegalArgumentException("Face is not parallel to any coordinate plane (within tol).");
		}

		// 2) The plane's constant value in that coordinate
		double planeVal = corners[0][constCoord];

		// Identify the other two coordinates (u and v).
		// If constCoord=0 => face is parallel to YZ => (u,v) = (1,2)
		// If constCoord=1 => face is parallel to XZ => (u,v) = (0,2)
		// If constCoord=2 => face is parallel to XY => (u,v) = (0,1)
		int uCoord, vCoord;
		if (constCoord == 0) {
			uCoord = 1; // y
			vCoord = 2; // z
		} else if (constCoord == 1) {
			uCoord = 0; // x
			vCoord = 2; // z
		} else { // constCoord == 2
			uCoord = 0; // x
			vCoord = 1; // y
		}

		// 3) Find the rectangle's bounding box in (uCoord, vCoord).
		double[] minMaxU = new double[2];
		double[] minMaxV = new double[2];
		findMinMax(corners, uCoord, vCoord, minMaxU, minMaxV);
		double minU = minMaxU[0];
		double maxU = minMaxU[1];
		double minV = minMaxV[0];
		double maxV = minMaxV[1];

		// 4) The origin's coordinate in (uCoord,vCoord) is (0,0).
		// Clamp each to [minU,maxU] and [minV,maxV].
		double closestU = clamp(0.0, minU, maxU);
		double closestV = clamp(0.0, minV, maxV);

		// 5) Construct the final (x,y,z) point:
		double[] result = new double[3];
		// Fill all coords from corners[0] just to get a base
		// or simply 0’s if you prefer, then set the needed coords:
		// result[x] = 0, result[y] = 0, result[z] = 0 initially
		// then fix them as needed:

		// The coordinate that is constant:
		result[constCoord] = planeVal;
		// The other two are closestU, closestV in the correct axes:
		result[uCoord] = closestU;
		result[vCoord] = closestV;

		return result;
	}

	// A small clamp utility:
	private static double clamp(double val, double minVal, double maxVal) {
		if (val < minVal)
			return minVal;
		if (val > maxVal)
			return maxVal;
		return val;
	}

	private static void findMinMax(double[][] corners, int u, int v, double[] minMaxU, double[] minMaxV) {
		double minU = Double.POSITIVE_INFINITY;
		double maxU = Double.NEGATIVE_INFINITY;
		double minV = Double.POSITIVE_INFINITY;
		double maxV = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < 4; i++) {
			double uVal = corners[i][u];
			double vVal = corners[i][v];
			if (uVal < minU)
				minU = uVal;
			if (uVal > maxU)
				maxU = uVal;
			if (vVal < minV)
				minV = vVal;
			if (vVal > maxV)
				maxV = vVal;
		}

		minMaxU[0] = minU;
		minMaxU[1] = maxU;
		minMaxV[0] = minV;
		minMaxV[1] = maxV;
	}

	/**
	 * Find the coordinate index (0 for x, 1 for y, 2 for z) that is the same for
	 * all 4 points in 'corners'. Returns -1 if no coordinate is perfectly constant
	 * (beyond tolerance).
	 */
	private static int findConstantCoordinate(double[][] corners, double tol) {
		for (int coord = 0; coord < 3; coord++) {
			double val0 = corners[0][coord];
			boolean allSame = true;
			for (int i = 1; i < 4; i++) {
				if (Math.abs(corners[i][coord] - val0) > tol) {
					allSame = false;
					break;
				}
			}
			if (allSame) {
				return coord; // Found which of x, y, z is constant
			}
		}
		return -1; // No coordinate is constant within 'tol'
	}
}
