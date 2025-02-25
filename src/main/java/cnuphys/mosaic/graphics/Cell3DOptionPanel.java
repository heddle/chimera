package cnuphys.mosaic.graphics;

import bCNU3D.Panel3D;
import cnuphys.mosaic.graphics.OptionPanel.OptionPanelCallback;

public class Cell3DOptionPanel extends OptionPanel implements OptionPanelCallback {
	
	private static String[] labels = {"Monochrome", "Spherical Polygon Points", "Theta Intersections", "Phi Intersections", "Curve Numbering"};
	private static boolean[] selected = {false, false, false, false, true};
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
	
	public boolean isSphericalPolygonPoints() {
		return isSelected(1);
	}
	
	public boolean isThetaIntersections() {
		return isSelected(2);
	}
	
	public boolean isPhiIntersections() {
		return isSelected(3);
	}
	
	public boolean isCurveNumbering() {
		return isSelected(4);
	}

}
