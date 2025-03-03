package cnuphys.mosaic.curve;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurveSplitter {

    /**
     * Splits the given list of curves at the specified TValue locations and ensures
     * the resulting list forms a closed loop.
     *
     * @param curves The original list of BasicCurves forming a closed loop.
     * @param splitPoints The list of TValue objects indicating split locations.
     * @return A new list of BasicCurves forming a closed loop after splitting.
     */
    public static ArrayList<BaseCurve> getSplitCurves(List<BaseCurve> curves, List<TValue> splitPoints) {
        // Map to store split points for each curve
        Map<BaseCurve, List<Double>> splitMap = new HashMap<>();

        // Organize split points by curve
        for (TValue tValue : splitPoints) {
            splitMap.computeIfAbsent(tValue.curve, k -> new ArrayList<>()).add(tValue.t);
        }

        ArrayList<BaseCurve> splitCurves = new ArrayList<>();

        // Iterate over each curve and split at the designated points
        for (BaseCurve curve : curves) {
            if (splitMap.containsKey(curve)) {
                List<Double> tValues = splitMap.get(curve);
                tValues.sort(Double::compareTo); // Ensure t-values are in ascending order

                BaseCurve currentCurve = curve;

                for (double t : tValues) {
                    BaseCurve[] split = currentCurve.split(t);
                    splitCurves.add(split[0]); // First part of the split
                    currentCurve = split[1];  // Continue splitting the remaining portion
                }
                splitCurves.add(currentCurve); // Add the last remaining part
            } else {
                // No split points for this curve, keep it as is
                splitCurves.add(curve);
            }
        }

        // Ensure the split curves form a closed loop
        if (!BaseCurve.validateLoop(splitCurves)) {
            throw new RuntimeException("Split curves do not form a closed loop!");
        }

        return splitCurves;
    }
}
