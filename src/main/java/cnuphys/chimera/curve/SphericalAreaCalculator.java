package cnuphys.chimera.curve;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

import cnuphys.chimera.util.Point3D;

public class SphericalAreaCalculator {

    private static final double TOL = 1.0e-6;
    private static final int MAX_POINTS = 10000;
    private static final double REFINEMENT_FRACTION = 0.2;

    public static double computeEnclosedArea(List<GeneralCurve> curves, double R) {
        List<Point3D.Double> vertices = getInitialGreatCircleVertices(curves);
        double initialArea = computeSphericalPolygonArea(vertices, R);

        return refineAndComputeArea(vertices, R, initialArea);
    }

    private static List<Point3D.Double> getInitialGreatCircleVertices(List<GeneralCurve> curves) {
        List<Point3D.Double> vertices = new ArrayList<>();
        for (GeneralCurve curve : curves) {
            vertices.add(curve.getP0());
        }
        return vertices;
    }

    private static double refineAndComputeArea(List<Point3D.Double> vertices, double R, double prevArea) {
        int iteration = 0;

        while (true) {
            List<Point3D.Double> refinedVertices = new ArrayList<>();
            int n = vertices.size();
            if (n > MAX_POINTS) {
                System.out.println("Max points reached, stopping refinement.");
                break;
            }

            PriorityQueue<SegmentError> pq = new PriorityQueue<>(Comparator.comparingDouble(SegmentError::getError).reversed());

            for (int i = 0; i < n; i++) {
                Point3D.Double p1 = vertices.get(i);
                Point3D.Double p2 = vertices.get((i + 1) % n);
                Point3D.Double midpoint = getMidpointOnSphere(p1, p2, R);

                double error = greatCircleDistance(p1, midpoint) + greatCircleDistance(midpoint, p2) - greatCircleDistance(p1, p2);
                pq.add(new SegmentError(i, midpoint, error));
            }

            int refineCount = (int) Math.max(1, REFINEMENT_FRACTION * n);
            for (int i = 0; i < refineCount && !pq.isEmpty(); i++) {
                SegmentError worst = pq.poll();
                refinedVertices.add(vertices.get(worst.index));
                refinedVertices.add(worst.midpoint);
            }

            for (int i = 0; i < n; i++) {
                if (!containsPoint(refinedVertices, vertices.get(i))) {
                    refinedVertices.add(vertices.get(i));
                }
            }

            double newArea = computeSphericalPolygonArea(refinedVertices, R);
            double fractionalChange = Math.abs(newArea - prevArea) / prevArea;

            System.out.println("Iteration: " + iteration + ", Points: " + refinedVertices.size() + ", Area: " + newArea + ", Change: " + fractionalChange);

            if (Double.isNaN(newArea)) {
                throw new RuntimeException("Error: Computed area is NaN. Check input points.");
            }

            if (fractionalChange < TOL) {
                return newArea;
            }

            vertices = refinedVertices;
            prevArea = newArea;
            iteration++;
        }

        return prevArea;
    }

    private static double computeSphericalPolygonArea(List<Point3D.Double> vertices, double R) {
        int n = vertices.size();
        double E = 0.0;

        for (int i = 0; i < n; i++) {
            Point3D.Double A = vertices.get(i);
            Point3D.Double B = vertices.get((i + 1) % n);
            Point3D.Double C = vertices.get((i + 2) % n);
            E += computeSphericalExcess(A, B, C);
        }

        return (E - (n - 2) * Math.PI) * R * R;
    }

    private static double computeSphericalExcess(Point3D.Double A, Point3D.Double B, Point3D.Double C) {
        double a = greatCircleDistance(B, C);
        double b = greatCircleDistance(A, C);
        double c = greatCircleDistance(A, B);

        double s = (a + b + c) / 2;
        double tanEOver4 = Math.sqrt(Math.tan(s / 2) * Math.tan((s - a) / 2) * Math.tan((s - b) / 2) * Math.tan((s - c) / 2));

        if (Double.isNaN(tanEOver4)) {
            return 0.0; // Prevent NaN values
        }

        return 4 * Math.atan(tanEOver4);
    }

    private static double greatCircleDistance(Point3D.Double P, Point3D.Double Q) {
        double dot = (P.x * Q.x + P.y * Q.y + P.z * Q.z) / (magnitude(P) * magnitude(Q));
        dot = Math.max(-1.0, Math.min(1.0, dot)); // Clamp to avoid NaN errors

        return Math.acos(dot);
    }

    private static double magnitude(Point3D.Double P) {
        return Math.sqrt(P.x * P.x + P.y * P.y + P.z * P.z);
    }

    private static Point3D.Double getMidpointOnSphere(Point3D.Double P, Point3D.Double Q, double R) {
        double mx = (P.x + Q.x) / 2;
        double my = (P.y + Q.y) / 2;
        double mz = (P.z + Q.z) / 2;

        double norm = Math.sqrt(mx * mx + my * my + mz * mz);
        if (norm < 1e-10) {
            return P; // Prevent zero division
        }

        double scale = R / norm;
        return new Point3D.Double(mx * scale, my * scale, mz * scale);
    }

    private static boolean containsPoint(List<Point3D.Double> vertices, Point3D.Double point) {
        for (Point3D.Double v : vertices) {
            if (Math.abs(v.x - point.x) < 1e-9 &&
                Math.abs(v.y - point.y) < 1e-9 &&
                Math.abs(v.z - point.z) < 1e-9) {
                return true;
            }
        }
        return false;
    }

    private static class SegmentError {
        int index;
        Point3D.Double midpoint;
        double error;

        SegmentError(int index, Point3D.Double midpoint, double error) {
            this.index = index;
            this.midpoint = midpoint;
            this.error = error;
        }

        public double getError() {
            return error;
        }
    }
}
