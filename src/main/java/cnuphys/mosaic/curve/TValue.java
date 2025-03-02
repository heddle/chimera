package cnuphys.mosaic.curve;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;

import cnuphys.mosaic.util.MathUtil;


public class TValue {
	
	public BaseCurve curve;
	public double t;
	public double value;
	private UnivariateFunction _func;
	
	
	public TValue(BaseCurve curve, UnivariateFunction func, double targetValue, double tmin, double tmax,
			double tolerance) {
		this.curve = curve;
		this.value = targetValue;
		this._func = func;
		t = MathUtil.computeT(func, value, tmin, tmax, tolerance); // Assign computed value to `this.t`
	}

	

	
	public String toString() {
		String out = String.format("t = %.3f, targVal = %.3f  interpVal = %.3f", 
				t, value, _func.value(t));
		return out;
	}
	
}
