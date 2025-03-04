package cnuphys.mosaic.curve;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurveSplitter {

	   /**
     * Splits each curve in the provided list at the t values given by its TValue objects.
     * It returns a new list of curves that, when taken in order, form a continuous closed loop.
     *
     * Assumptions:
     * <ul>
     *   <li>Each curve contains its split points (here, we use getThetaCrossings() as an example).</li>
     *   <li>The curve.split(t) method returns an array of two curves corresponding to the split:
     *       the left segment covering [0,t] and the right segment covering [t,1), with the latter
     *       reparameterized to [0,1).</li>
     *   <li>If multiple splits occur on one curve, the original parameter t is mapped to the current
     *       segment using: newT = (t - previousT) / (1 - previousT).</li>
     * </ul>
     *
     * @param curves the original list of curves forming a closed loop
     * @return a new list of curves resulting from splitting
     */
    public static ArrayList<BaseCurve> getSplitCurves(List<BaseCurve> curves) {
        ArrayList<BaseCurve> splitCurves = new ArrayList<>();

        for (BaseCurve curve : curves) {
            // Get the split points for this curve.
            // (Here we assume the split points are the theta crossings;
            // adjust if your TValue list comes from elsewhere.)
            List<TValue> splitPoints = curve.getThetaCrossings();

            // Sort the split points by t.
            Collections.sort(splitPoints, (a, b) -> Double.compare(a.t, b.t));

            // If there are no split points, simply add the original curve.
            if (splitPoints.isEmpty()) {
                splitCurves.add(curve);
            } else {
                double previousT = 0.0;
                BaseCurve currentCurve = curve;
                for (TValue tv : splitPoints) {
                    double t = tv.t;
                    // Skip if this t is not greater than the last one.
                    if (t <= previousT) {
                        continue;
                    }
                    // Map t from the original parameterization to the current segment.
                    double newT = (t - previousT) / (1.0 - previousT);
                    BaseCurve[] segments = currentCurve.split(newT);
                    // Add the left segment.
                    splitCurves.add(segments[0]);
                    // Continue with the right segment.
                    currentCurve = segments[1];
                    previousT = t;
                }
                // Add the final segment.
                splitCurves.add(currentCurve);
            }
        }

        // Optionally validate that the new list forms a closed loop.
        if (!BaseCurve.validateLoop(splitCurves)) {
            throw new IllegalStateException("Split curves do not form a continuous closed loop.");
        }

        return splitCurves;
    }


}
