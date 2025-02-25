package cnuphys.mosaic.util;

import cnuphys.mosaic.grid.GridSupport;

public class ClosestPointToOrigin {
	
	public static double[] closestPointOnFaceToOrigin(double[][] corners) {
		//get the closest face
		int face = GridSupport.getClosestFaceToOrigin(corners);
		double[][] faceCorners = GridSupport.getFaceCorners(corners, face);
	    double[] projectedPoint = projectOriginOntoPlane(faceCorners);

        if (isPointInsideFace(faceCorners, projectedPoint)) {
            return projectedPoint;
        } else {
            double[] closestPoint = faceCorners[0];
            double minDistance = distanceFromOrigin(closestPoint);

            for (int i = 0; i < 4; i++) {
                double[] edgeStart = faceCorners[i];
                double[] edgeEnd = faceCorners[(i + 1) % 4];

                double[] edgeClosest = closestPointOnEdge(edgeStart, edgeEnd);
                double distance = distanceFromOrigin(edgeClosest);

                if (distance < minDistance) {
                    minDistance = distance;
                    closestPoint = edgeClosest;
                }
            }

            return closestPoint;
        }		
		
	}
	
    public static boolean isPointInsideFace(double[][] faceCorners, double[] point) {
        double minX = Math.min(Math.min(faceCorners[0][0], faceCorners[1][0]), Math.min(faceCorners[2][0], faceCorners[3][0]));
        double maxX = Math.max(Math.max(faceCorners[0][0], faceCorners[1][0]), Math.max(faceCorners[2][0], faceCorners[3][0]));
        double minY = Math.min(Math.min(faceCorners[0][1], faceCorners[1][1]), Math.min(faceCorners[2][1], faceCorners[3][1]));
        double maxY = Math.max(Math.max(faceCorners[0][1], faceCorners[1][1]), Math.max(faceCorners[2][1], faceCorners[3][1]));
        double minZ = Math.min(Math.min(faceCorners[0][2], faceCorners[1][2]), Math.min(faceCorners[2][2], faceCorners[3][2]));
        double maxZ = Math.max(Math.max(faceCorners[0][2], faceCorners[1][2]), Math.max(faceCorners[2][2], faceCorners[3][2]));

        return (point[0] >= minX && point[0] <= maxX) &&
               (point[1] >= minY && point[1] <= maxY) &&
               (point[2] >= minZ && point[2] <= maxZ);
    }

	
	
	
    public static double distanceFromOrigin(double[] point) {
        return Math.sqrt(point[0] * point[0] + point[1] * point[1] + point[2] * point[2]);
    }
	
    public static double[] closestPointOnEdge(double[] A, double[] B) {
        double[] AB = { B[0] - A[0], B[1] - A[1], B[2] - A[2] };
        double[] AO = { -A[0], -A[1], -A[2] };

        double AB_dot_AO = AB[0] * AO[0] + AB[1] * AO[1] + AB[2] * AO[2];
        double AB_dot_AB = AB[0] * AB[0] + AB[1] * AB[1] + AB[2] * AB[2];

        double t = AB_dot_AO / AB_dot_AB;
        t = Math.max(0, Math.min(1, t));

        return new double[]{
            A[0] + t * AB[0],
            A[1] + t * AB[1],
            A[2] + t * AB[2]
        };
    }

	
    public static double[] projectOriginOntoPlane(double[][] faceCorners) {
        double[] normal = computeNormal(faceCorners);
        double d = -(normal[0] * faceCorners[0][0] +
                     normal[1] * faceCorners[0][1] +
                     normal[2] * faceCorners[0][2]);

        double t = -(normal[0] * 0 + normal[1] * 0 + normal[2] * 0 + d) /
                   (normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);

        return new double[]{normal[0] * t, normal[1] * t, normal[2] * t};
    }
    
    public static double[] computeNormal(double[][] faceCorners) {
        double[] edge1 = {
            faceCorners[1][0] - faceCorners[0][0],
            faceCorners[1][1] - faceCorners[0][1],
            faceCorners[1][2] - faceCorners[0][2]
        };

        double[] edge2 = {
            faceCorners[2][0] - faceCorners[0][0],
            faceCorners[2][1] - faceCorners[0][1],
            faceCorners[2][2] - faceCorners[0][2]
        };

        return new double[]{
            edge1[1] * edge2[2] - edge1[2] * edge2[1],
            edge1[2] * edge2[0] - edge1[0] * edge2[2],
            edge1[0] * edge2[1] - edge1[1] * edge2[0]
        };
    }


}
