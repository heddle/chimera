package cnuphys.chimera.monteCarlo;

import java.awt.Color;
import java.util.Random;

import cnuphys.chimera.frame.Mosaic;
import cnuphys.chimera.grid.MosaicGrid;
import cnuphys.chimera.grid.Fivetuple;
import cnuphys.chimera.util.Point3D;
import cnuphys.chimera.util.ThetaPhi;


public class MonteCarloPoint {

	private static Color[] mapColors1 = {
            new Color(70, 130, 180),  // Ocean Blue
            new Color(34, 139, 34),   // Earth Green
            new Color(240, 230, 140), // Desert Yellow
            new Color(169, 169, 169), // Mountain Gray
            new Color(95, 158, 160),  // Dark Cyan
            new Color(220, 20, 60)    // Urban Red
        };
	
		private static Color[] monoChromeColors = { 
				new Color(32, 32, 32),
				new Color(64, 64, 64),
				new Color(96, 96, 96),
				new Color(128, 128, 128),
				new Color(160, 160, 160),
				new Color(192, 192, 193)
		};

	public ThetaPhi thetaPhi;
	public Fivetuple fiveplet;
	
	private Color mapColors[] = monoChromeColors;

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

		fiveplet = new Fivetuple(cIndices[0], cIndices[1], cIndices[2], sIndices[0], sIndices[1]);

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

	public int getColorIndex() {
		if (fiveplet.nx < 0 || fiveplet.ny < 0 || fiveplet.nz < 0 || fiveplet.ntheta < 0 || fiveplet.nphi < 0) {
			return -1;
		}
		int sum = fiveplet.nx + fiveplet.ny + fiveplet.nz + 2*fiveplet.ntheta + 2*fiveplet.nphi;
		return sum % mapColors.length;
	}

	public Color getColor() {
		int index = getColorIndex();
		if (index < 0) {
			return Color.BLACK;
		}
       return mapColors[index];
	}


	@Override
	public String toString() {
		return String.format("MonteCarloPoint: %s %s", thetaPhi, fiveplet);
	}
}
