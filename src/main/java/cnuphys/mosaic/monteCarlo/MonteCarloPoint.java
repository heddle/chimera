package cnuphys.mosaic.monteCarlo;

import java.awt.Color;
import java.util.Random;

import cnuphys.mosaic.frame.Mosaic;
import cnuphys.mosaic.graphics.Drawing;
import cnuphys.mosaic.grid.MosaicGrid;
import cnuphys.mosaic.patch.Tuple;
import cnuphys.mosaic.util.Point3D;
import cnuphys.mosaic.util.ThetaPhi;


public class MonteCarloPoint {


	public ThetaPhi thetaPhi;
	public Tuple tuple;


	private static Random random = new Random();

	public MonteCarloPoint(double radius) {

		thetaPhi = new ThetaPhi(radius, 0, 0);
		ThetaPhi.setRandomThetaPhi(random, thetaPhi);
		Point3D.Double cartesian = thetaPhi.toCartesian();

		MosaicGrid grid = Mosaic.getInstance().getMosaicGrid();

		int sIndices[] = new int[2];
		int cIndices[] = new int[3];
		grid.getSphericalGrid().getIndices(thetaPhi, sIndices);
		grid.getCartesianGrid().getIndices(cartesian, cIndices);

		tuple = new Tuple(cIndices[0], cIndices[1], cIndices[2], sIndices[0], sIndices[1]);

    }

	/**
	 * Gets the Cartesian coordinates of the Monte Carlo point.
	 *
	 * @return The Cartesian coordinates
	 * @see Point3D
	 */
	public Point3D.Double getCartesian() {
        return thetaPhi.toCartesian();
    }


	public Color getColor() {
       return Drawing.tupleColor(tuple);
	}


	@Override
	public String toString() {
		return String.format("MonteCarloPoint: %s %s", thetaPhi, tuple);
	}
}
