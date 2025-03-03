package cnuphys.mosaic.curve;

import java.util.ArrayList;
import java.util.List;

public class CurveValidator {
    public static ArrayList<BaseCurve> returnValidLoop(ArrayList<BaseCurve> curves) {
    	
    	
 
        if (curves == null || curves.isEmpty()) {
            return null;
        }
        
		for (BaseCurve curve : curves) {
			System.out.println(curve);
		}
        
        ArrayList<BaseCurve> result = new ArrayList<>();
        boolean[] used = new boolean[curves.size()];
        if (findValidLoop(curves, result, used, null)) {
            return result;
        }
        return null; // No valid loop found
    }

    private static boolean findValidLoop(List<BaseCurve> curves, List<BaseCurve> result, boolean[] used, BaseCurve prev) {
        if (result.size() == curves.size()) {
            // Check if the last curve connects to the first curve
            return BaseCurve.pointsAreClose(result.get(result.size() - 1).getP1(), result.get(0).getP0());
        }

        for (int i = 0; i < curves.size(); i++) {
            if (used[i]) continue;
            
            BaseCurve original = curves.get(i);
            BaseCurve reversed = original.reverse();
            
            // Try both original and reversed orientations
            for (BaseCurve candidate : new BaseCurve[]{original, reversed}) {
                if (prev == null || BaseCurve.pointsAreClose(prev.getP1(), candidate.getP0())) {
                    result.add(candidate);
                    used[i] = true;
                    
                    if (findValidLoop(curves, result, used, candidate)) {
                        return true;
                    }
                    
                    // Backtrack
                    result.remove(result.size() - 1);
                    used[i] = false;
                }
            }
        }
        return false;
    }
}
