package cnuphys.chimera.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;
import java.util.List;
import java.util.function.Function;

public class PiecewiseFunction implements UnivariateFunction {
    private final List<ISegment> segments;
    private final Function<ISegment, UnivariateFunction> functionSelector;
    
    public PiecewiseFunction(List<ISegment> segments, Function<ISegment, UnivariateFunction> functionSelector) {
        this.segments = segments;
        this.functionSelector = functionSelector;
    }

    @Override
    public double value(double t) {
        int N = segments.size();
        double segmentT = t * N;  // Scale t to [0, N]
        int segmentIndex = Math.min((int) segmentT, N - 1); // Ensure index is in range
        double localT = segmentT - segmentIndex; // Local t in [0,1] for the segment
        
        UnivariateFunction localFunction = functionSelector.apply(segments.get(segmentIndex));
        return localFunction.value(localT);
    }
}