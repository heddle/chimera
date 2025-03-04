package cnuphys.mosaic.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.feedback.FeedbackControl;
import cnuphys.bCNU.feedback.FeedbackPane;
import cnuphys.bCNU.feedback.IFeedbackProvider;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.mosaic.graphics.Drawing;
import cnuphys.mosaic.grid.CartesianGrid;
import cnuphys.mosaic.grid.MosaicGrid;
import cnuphys.mosaic.grid.SphericalGrid;
import cnuphys.mosaic.grid.mapping.EProjection;
import cnuphys.mosaic.grid.mapping.MapProjectionMenu;
import cnuphys.mosaic.grid.mapping.MapView2D;
import cnuphys.mosaic.grid.mapping.MollweideProjection;
import cnuphys.mosaic.monteCarlo.MonteCarloPoint;
import cnuphys.mosaic.patch.Prepatch;
import cnuphys.mosaic.patch.ThetaPatch;
import cnuphys.mosaic.util.Point3D;
import cnuphys.mosaic.util.ThetaPhi;

public class MapOverlayView2D extends MapView2D implements IFeedbackProvider {

	private static final int WIDTH = 1200;

	//for selecting the map projection
	private MapProjectionMenu _projectionMenu;

	private FeedbackPane _feedbackPane;
	
	private JPanel _eastPanel;
	private JPanel _overlayPanel;
	
	private boolean overlayMonteCarlo = false;
	private boolean overlayPrepatches = false;
	private boolean overlayThetaPatches = false;
	private boolean overlayPatches = false;

	private static final Color prepatchLineColor = X11Colors.getX11Color("Powder Blue");
    private static final Color thetaPatchLineColor = X11Colors.getX11Color("Coral");

	/**
	 * Create a 2D view for Monte Carlo
	 */
	public MapOverlayView2D() {
		super(PropertySupport.TITLE, "Map View",
				PropertySupport.WORLDSYSTEM, getWorldSystem(EProjection.MOLLWEIDE),
				PropertySupport.ICONIFIABLE, true,
				PropertySupport.MAXIMIZABLE, true,
				PropertySupport.CLOSABLE, true,
				PropertySupport.RESIZABLE, true,
				PropertySupport.PROPNAME, "Map View",
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
		
		_eastPanel = new JPanel();
		
		_eastPanel.setLayout(new BorderLayout());
		makeOverlayPanel();

		_projectionMenu = new MapProjectionMenu(al);
		menuBar.add(_projectionMenu);

		//the default map projection
		_projection = new MollweideProjection(Mosaic.getInstance().getRadius());

		setFeedback();

		add(_eastPanel, BorderLayout.EAST);
		setBeforeDraw();
		setAfterDraw();
	}

	
	
	
	private void makeOverlayPanel() {
	    _overlayPanel = new JPanel();
	    // Set the layout to a vertical box layout
	    _overlayPanel.setLayout(new BoxLayout(_overlayPanel, BoxLayout.Y_AXIS));
	    _overlayPanel.setBorder(new CommonBorder("Map Overlays"));
	    
	    // Create the "Overlay MonteCarlo" checkbox
	    JCheckBox monteCarloCheck = new JCheckBox("Overlay MonteCarlo");
	    monteCarloCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
	    monteCarloCheck.setSelected(overlayMonteCarlo);  // default unselected
	    monteCarloCheck.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            // Callback: perform any required action when the checkbox is toggled
	            overlayMonteCarlo = monteCarloCheck.isSelected();
	            refresh();
	        }
	    });
	    _overlayPanel.add(monteCarloCheck);

	    // Create the "Overlay Prepatches" checkbox
	    JCheckBox prepatchesCheck = new JCheckBox("Overlay Prepatches");
	    prepatchesCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
	    prepatchesCheck.setSelected(overlayPrepatches);  // default unselected);
	    prepatchesCheck.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	        	overlayPrepatches = prepatchesCheck.isSelected();
	            refresh();
	        }
	    });
	    _overlayPanel.add(prepatchesCheck);

	    // Create the "Overlay Theta Patches" checkbox
	    JCheckBox thetaPatchesCheck = new JCheckBox("Overlay Theta Patches");
	    thetaPatchesCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
	    thetaPatchesCheck.setSelected(overlayThetaPatches);  // default unselected);
	    thetaPatchesCheck.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            overlayThetaPatches = thetaPatchesCheck.isSelected();
	            refresh();
	        }
	    });
	    _overlayPanel.add(thetaPatchesCheck);

	    // Create the "Overlay Patches" checkbox
	    JCheckBox patchesCheck = new JCheckBox("Overlay Patches");
	    patchesCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
	    patchesCheck.setSelected(overlayPatches);  // default unselected);
	    patchesCheck.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            overlayPatches =  patchesCheck.isSelected();
	            refresh();
	        }
	    });
	    _overlayPanel.add(patchesCheck);

	    // Add the overlay panel to the east panel at the NORTH position
	    _eastPanel.add(_overlayPanel, BorderLayout.NORTH);
	}

	//set up the feedback
	private void setFeedback() {
		FeedbackControl fbc = getContainer().getFeedbackControl();
		fbc.addFeedbackProvider(this);
		_feedbackPane = new FeedbackPane();
		getContainer().setFeedbackPane(_feedbackPane);

		Dimension dim = _feedbackPane.getPreferredSize();
		_feedbackPane.setPreferredSize(new Dimension(200, dim.height));
		
		_eastPanel.add(_feedbackPane, BorderLayout.CENTER);
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


		MosaicGrid grid = Mosaic.getInstance().getMosaicGrid();
		final MapOverlayView2D view = this;
		IDrawable afterDraw = new DrawableAdapter() {
			@Override
			public void draw(Graphics g, IContainer container) {
				if (overlayMonteCarlo) {
					drawMonteCarloPoints(g, container);
				}
				
				_projection.drawMapOutline(g, container);

				if (overlayPrepatches) {
					for (Prepatch prepatch : grid.getPrepatches()) {
						Drawing.drawPatch2D(g, view, prepatch, prepatchLineColor, Color.blue, 1);
					}
				}
				
				if (overlayThetaPatches) {
					for (ThetaPatch thetapatch : grid.getThetaPatches()) {
						Drawing.drawPatch2D(g, view, thetapatch, thetaPatchLineColor, Color.green, 1);
					}
				}
			}
		};

		getContainer().setAfterDraw(afterDraw);
	}
	
	public boolean thetaPhiToLocal(Point pp, ThetaPhi thetaPhi) {
		Point2D.Double xy = new Point2D.Double();
		xy.x = thetaPhi.getPhi();
		xy.y = thetaPhi.getLatitude();
		_projection.latLonToXY(xy, xy);
		getContainer().worldToLocal(pp, xy);
		return _projection.isPointVisible(xy);
	}

	//draw the Monte Carlo points
	private void drawMonteCarloPoints(Graphics g, IContainer container) {
		List<MonteCarloPoint> points = Mosaic.getInstance().getMonteCarloPoints();

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
		int patchCount= Mosaic.getInstance().getMonteCarloSeenSet().size();


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
		ThetaPhi tp = new ThetaPhi(Mosaic.getInstance().getRadius(),
				Math.PI/2 - latLon.y, latLon.x);
		Point3D.Double cartesian = tp.toCartesian();

		MosaicGrid grid = Mosaic.getInstance().getMosaicGrid();
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
