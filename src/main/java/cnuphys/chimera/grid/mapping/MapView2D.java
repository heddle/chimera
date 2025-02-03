package cnuphys.chimera.grid.mapping;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.view.BaseView;
import cnuphys.chimera.grid.SphericalGrid;

public class MapView2D extends BaseView implements MouseMotionListener {

	//the map projection
	protected IMapProjection _projection;

	public MapView2D(Object... properties) {
		super(properties);

		getContainer().getComponent().addMouseMotionListener(this);

	}


	/**
	 * Get the map projection
	 * @return the map projection
	 */
	public IMapProjection getProjection() {
		return _projection;
	}


	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	/**
     * Get the default world system
     * @param eprojection the map projection
     * @return the world system
     */
	protected static Rectangle2D.Double getWorldSystem(EProjection eprojection) {

		double xlim = getRadius();
		double ylim = getRadius();

		switch (eprojection) {
		case MOLLWEIDE:
			xlim *= 2.1;
			ylim *= 1.4;
			break;
		case MERCATOR:
			xlim = 1.1*Math.PI;
			ylim = 1.1*Math.PI;
			break;
		case ORTHOGRAPHIC:
			xlim *= 1.1;
			ylim *= 1.1;
			break;
		default:
			xlim *= 2.1;
			ylim *= 1.4;
		}
		return new Rectangle2D.Double(-xlim, -ylim, 2 * xlim, 2 * ylim);
	}

	//return the radius, though at least for now keep fixed at 1
	protected static double getRadius() {
		return SphericalGrid.R;
	}



}
