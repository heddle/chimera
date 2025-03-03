package cnuphys.mosaic.graphics;

import bCNU3D.Panel3D;
import cnuphys.mosaic.graphics.OptionPanel.OptionPanelCallback;

public class Cell3DOptionPanel extends OptionPanel implements OptionPanelCallback {

	private static String[] labels = { "Monochrome", "Prepatch", "ThetaPatches", "Spherical Polygon Points", "Theta Curves", "Phi Intersections",
			"Curve Numbering", "Theta Splicings" };
	private static boolean[] selected = { false, true, false, false, false, false, false, false};
	private Panel3D panel3D;

	public Cell3DOptionPanel(Panel3D panel3D) {
		super(labels, selected);
		this.panel3D = panel3D;
		setCallback(this);
	}
	@Override
	public void onToggle() {
		panel3D.refresh();
	}

	public boolean isMonochrome() {
		return isSelected(0);
	}

	public boolean isPrepatch() {
		return isSelected(1);
	}

	public boolean isThetaPatches() {
		return isSelected(2);
	}

	public boolean isSphericalPolygonPoints() {
		return isSelected(3);
	}

	public boolean isThetaCurves() {
		return isSelected(4);
	}

	public boolean isPhiIntersections() {
		return isSelected(5);
	}

	public boolean isCurveNumbering() {
		return isSelected(6);
	}

	public boolean isThetaSplicings() {
		return isSelected(7);
	}



}
