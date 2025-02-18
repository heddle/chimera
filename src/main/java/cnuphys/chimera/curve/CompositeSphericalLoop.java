package cnuphys.chimera.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;
import java.util.List;

public class CompositeSphericalLoop implements ISegment {
    private final List<ISegment> segments;
    
    public CompositeSphericalLoop(List<ISegment> segments) {
        this.segments = segments;
    }

    @Override
    public UnivariateFunction getThetaFunction() {
        return new PiecewiseFunction(segments, ISegment::getThetaFunction);
    }

    @Override
    public UnivariateFunction getPhiFunction() {
        return new PiecewiseFunction(segments, ISegment::getPhiFunction);
    }

    @Override
    public UnivariateFunction getDThetaFunction() {
        return new PiecewiseFunction(segments, ISegment::getDThetaFunction);
    }

    @Override
    public UnivariateFunction getDPhiFunction() {
        return new PiecewiseFunction(segments, ISegment::getDPhiFunction);
    }
}