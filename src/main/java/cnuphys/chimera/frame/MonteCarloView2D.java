package cnuphys.chimera.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JMenuBar;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.feedback.FeedbackControl;
import cnuphys.bCNU.feedback.FeedbackPane;
import cnuphys.bCNU.feedback.IFeedbackProvider;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.chimera.grid.CartesianGrid;
import cnuphys.chimera.grid.ChimeraGrid;
import cnuphys.chimera.grid.SphericalGrid;
import cnuphys.chimera.grid.mapping.EProjection;
import cnuphys.chimera.grid.mapping.MapProjectionMenu;
import cnuphys.chimera.grid.mapping.MapView2D;
import cnuphys.chimera.grid.mapping.MollweideProjection;
import cnuphys.chimera.monteCarlo.MonteCarloPoint;
import cnuphys.chimera.util.Point3D;
import cnuphys.chimera.util.ThetaPhi;

public class MonteCarloView2D extends MapView2D implements IFeedbackProvider {

	private static final int WIDTH = 1200;

	//for selecting the map projection
	private MapProjectionMenu _projectionMenu;

	private FeedbackPane _feedbackPane;

	/**
	 * Create a 2D view for Monte Carlo
	 */
	public MonteCarloView2D() {
		super(PropertySupport.TITLE, "MonteCarlo 2D",
				PropertySupport.WORLDSYSTEM, getWorldSystem(EProjection.MOLLWEIDE),
				PropertySupport.ICONIFIABLE, true,
				PropertySupport.MAXIMIZABLE, true,
				PropertySupport.CLOSABLE, true,
				PropertySupport.RESIZABLE, true,
				PropertySupport.PROPNAME, "MonteCarlo 2D",
				PropertySupport.BACKGROUND, Color.white,
				PropertySupport.WIDTH, WIDTH,
				PropertySupport.HEIGHT, (int)(0.66325 * WIDTH),
				PropertySupport.TOOLBAR, true,
				PropertySupport.VISIBLE, false);

		JMenuBar menuBar = new JMenuBar();

		setJMenuBar(menuBar);

		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = e.getActionCommand();
				EProjection eproj = EProjection.getValue(name);
				getContainer().setWorldSystem(getWorldSystem(eproj));
				_projection = EProjection.getProjection(name);
				refresh();
			}
		};

		_projectionMenu = new MapProjectionMenu(al);
		menuBar.add(_projectionMenu);

		//the default map projection
		_projection = new MollweideProjection();

		setFeedback();

		setBeforeDraw();
		setAfterDraw();
	}


	//set up the feedback
	private void setFeedback() {
		FeedbackControl fbc = getContainer().getFeedbackControl();
		fbc.addFeedbackProvider(this);
		_feedbackPane = new FeedbackPane();
		getContainer().setFeedbackPane(_feedbackPane);

		Dimension dim = _feedbackPane.getPreferredSize();
		_feedbackPane.setPreferredSize(new Dimension(200, dim.height));
		add(_feedbackPane, BorderLayout.EAST);
	}


	/**
	 * Set the views before draw
	 */
	private void setBeforeDraw() {
		IDrawable beforeDraw = new DrawableAdapter() {
			@Override
			public void draw(Graphics g, IContainer container) {
			}
		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	/**
	 * Set the views after draw
	 */
	private void setAfterDraw() {


		IDrawable afterDraw = new DrawableAdapter() {
			@Override
			public void draw(Graphics g, IContainer container) {
				drawMonteCarloPoints(g, container);
				_projection.drawMapOutline(g, container);
			}
		};

		getContainer().setAfterDraw(afterDraw);
	}

	//draw the Monte Carlo points
	private void drawMonteCarloPoints(Graphics g, IContainer container) {
		List<MonteCarloPoint> points = Chimera.getInstance().getMonteCarloPoints();

		Point2D.Double xy = new Point2D.Double();
		Point2D.Double latLon = new Point2D.Double();
		Point pp = new Point();

		for (MonteCarloPoint mcp : points) {
			ThetaPhi thetaPhi = mcp.thetaPhi;
			latLon.x = thetaPhi.getPhi();
			latLon.y = thetaPhi.getLatitude();
			if (!_projection.isPointVisible(latLon)) {
				continue;
			}

			_projection.latLonToXY(latLon, xy);
			container.worldToLocal(pp, xy);
			g.setColor(mcp.getColor());
			g.fillRect(pp.x - 1, pp.y - 1, 2, 2);
		}
	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double xy,
			List<String> feedbackStrings) {

		boolean onMap = _projection.isPointOnMap(xy);
		int patchCount= Chimera.getInstance().getMonteCarloSeenSet().size();


		String projStr = String.format("projection: %s", _projection.name());
		String screenStr = String.format("screen: [%d, %d] ", pp.x, pp.y);
		String worldStr = String.format("xy: [%6.2f, %6.2f] ", xy.x, xy.y);
		String patchStr = String.format("Patch Count: %d", patchCount);
		feedbackStrings.add(projStr);
		feedbackStrings.add(screenStr);
		feedbackStrings.add(worldStr);
		feedbackStrings.add("$red$" + patchStr);

		//if not on map we are done
		if (!onMap) {
			return;
		}

		Point2D.Double latLon = new Point2D.Double();
		_projection.latLonFromXY(latLon, xy);

		double lat = Math.toDegrees(latLon.y);
		double lon = Math.toDegrees(latLon.x);
		double theta = 90 - lat;
		double phi = lon;
		ThetaPhi tp = new ThetaPhi(Math.PI/2 - latLon.y, latLon.x);
		Point3D.Double cartesian = tp.toCartesian();

		ChimeraGrid grid = Chimera.getInstance().getChimeraGrid();
		CartesianGrid cgrid = grid.getCartesianGrid();
		SphericalGrid sgrid = grid.getSphericalGrid();
		int cindices[] = new int[3];
		cgrid.getIndices(cartesian, cindices);
		int sindices[] = new int[2];
		sgrid.getIndices(tp, sindices);


		String latStr = String.format("Lat: %.2f ", lat);
		String lonStr = String.format("Lon: %.2f ", lon);
		String thetaStr = String.format("%s: %.2f ", ThetaPhi.SMALL_THETA, theta);
		String phiStr = String.format("%s: %.2f ", ThetaPhi.SMALL_PHI, phi);
		String cindexStr = String.format("$yellow$(nx, ny, nz): (%d, %d, %d) ", cindices[0], cindices[1], cindices[2]);
		String sindexStr = String.format("$yellow$(n%s, n%s): (%d, %d) ",
				ThetaPhi.SMALL_THETA, ThetaPhi.SMALL_PHI, sindices[0], sindices[1]);

		feedbackStrings.add(latStr);
		feedbackStrings.add(lonStr);
		feedbackStrings.add(thetaStr);
		feedbackStrings.add(phiStr);
		feedbackStrings.add(cindexStr);
		feedbackStrings.add(sindexStr);

	}

}
