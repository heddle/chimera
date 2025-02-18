package cnuphys.chimera.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;

public interface ISegment {
	
	public UnivariateFunction getThetaFunction();
	
	public UnivariateFunction getPhiFunction();
     
    public UnivariateFunction getDThetaFunction();
    
    public UnivariateFunction getDPhiFunction();


}
