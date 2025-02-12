package cnuphys.chimera.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.chimera.util.MathUtil;
import cnuphys.chimera.util.PanelKeys;
import item3D.Axes3D;
import item3D.Sphere;

public class ChimeraGridPanel3D extends Panel3D {
	
	private static SimpleDialog _dialog;
	
	private ChimeraGrid _grid;
	/*
	 * The panel that holds the 3D objects
	 *
	 * @param angleX the initial x rotation angle in degrees
	 *
	 * @param angleY the initial y rotation angle in degrees
	 *
	 * @param angleZ the initial z rotation angle in degrees
	 *
	 * @param xdist move viewpoint left/right
	 *
	 * @param ydist move viewpoint up/down
	 *
	 * @param zdist the initial viewer z distance should be negative
	 */
	public ChimeraGridPanel3D(ChimeraGrid grid, float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist,
			float xmax, float ymax, float zmax) {
		super(angleX, angleY, angleZ, xDist, yDist, zDist);
		setGrid(grid);
		
		String labels[] = { "X", "Y", "Z" };

		float ext = 1.5f;
		Axes3D axes = new Axes3D(this, -ext * xmax, ext * xmax, -ext * ymax, ext * ymax, -ext * zmax,
				ext * zmax, labels, Color.darkGray, 2f, 2, 2, 2, Color.black, Color.blue,
				new Font("SansSerif", Font.PLAIN, 14), 1);
		
		addItem(axes);
		
		Color fc = new Color(0, 0, 128, 32);
		SphericalGrid sphGrid = grid.getSphericalGrid();
		float fradius = (float) sphGrid.getRadius();
		float[] thetaVals = MathUtil.toFloatArray(sphGrid.getThetaArray());
		float[] phiVals = MathUtil.toFloatArray(sphGrid.getPhiArray());
		
		
		Sphere sphere = new Sphere(this, 0f, 0f, 0f, fradius, fc);
		sphere.setGridlines(thetaVals, phiVals);
		sphere.setGridColor(Color.black);
        addItem(sphere);
        
        float delta = 0.1f * fradius;
        PanelKeys.addKeyListener(this, delta, delta, delta);

	}
	
	public void setGrid(ChimeraGrid grid) {
		clearItems();
		_grid = grid;
	}
	
	@Override
	public void afterDraw(GLAutoDrawable drawable) {
		Support3D.prepareForTransparent(drawable);
		CartesianGrid cartGrid = _grid.getCartesianGrid();

		Color lc = new Color(0, 0, 0, 32);
		float lw = 0.1f;

		double xMin = cartGrid.getXMin();
		double xMax = cartGrid.getXMax();		
		for (int iy = 0; iy < cartGrid.getNumY(); iy++) {
			double y = cartGrid.getYGrid().valueAt(iy);
		    for (int iz = 0; iz < cartGrid.getNumZ(); iz++) {
				double z = cartGrid.getZGrid().valueAt(iz);
				Support3D.drawLine(drawable, xMin, y, z, xMax, y, z, lc, lw);
			}
		}
		
		
		double yMin = cartGrid.getYMin();
		double yMax = cartGrid.getYMax();
		for (int ix = 0; ix < cartGrid.getNumX(); ix++) {
			double x = cartGrid.getXGrid().valueAt(ix);
		    for (int iz = 0; iz < cartGrid.getNumZ(); iz++) {
				double z = cartGrid.getZGrid().valueAt(iz);
				Support3D.drawLine(drawable, x, yMin, z, x, yMax, z, lc, lw);
			}
		}
		

		double zMin = cartGrid.getZMin();
		double zMax = cartGrid.getZMax();
		for (int ix = 0; ix < cartGrid.getNumX(); ix++) {
			double x = cartGrid.getXGrid().valueAt(ix);
		    for (int iy = 0; iy < cartGrid.getNumY(); iy++) {
				double y = cartGrid.getYGrid().valueAt(iy);
				Support3D.drawLine(drawable, x, y, zMin, x, y, zMax, lc, lw);
			}
		}

		Support3D.prepareForOpaque(drawable);

	}

	public static void main(String arg[]) {
		final JFrame testFrame = new JFrame("ChimeraGrid Test");
		testFrame.setLayout(new BorderLayout(4, 4));
		
		final ChimeraGridPanel3D panel = createPanel(TestGrid.primaryTestGrid());
		testFrame.add(panel, BorderLayout.CENTER);

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.err.println("Done");
				System.exit(1);
			}
		};

		testFrame.addWindowListener(windowAdapter);
		testFrame.setBounds(200, 100, 1000, 900);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});

	}
	
	private static ChimeraGridPanel3D createPanel(ChimeraGrid grid) {
		CartesianGrid cartGrid = grid.getCartesianGrid();
		
		final float thetax = 45f;
		final float thetay = 45f;
		final float thetaz = 45f;
		final float xymax = (float) Math.max(cartGrid.getXMax(), cartGrid.getYMax());
		final float zmax = (float) cartGrid.getZMax();
		float xdist = 0.1f*xymax;
		float ydist = .1f*xymax;
		float zdist = -4f * zmax;
		
		return new ChimeraGridPanel3D(grid, thetax, thetay, thetaz, xdist, ydist, zdist, xymax, xymax, zmax) {
			
			@Override
			public float getZStep() {
				return zmax / 10f;
			}

		};
		
		
	}
	
	/**
     * Show the grid in a dialog
     * @param grid the grid to show
     */
	public static void showGrid(ChimeraGrid grid) {
		if (_dialog == null) {
			_dialog = new SimpleDialog("Chimera Grid",  false, "Close") {
				public Component createCenterComponent() {
					ChimeraGridPanel3D panel = createPanel(grid);

					return panel;
				}
				
				@Override
				public Dimension getPreferredSize() {
					return new Dimension(800, 800);
				}
				
			};
		}
		_dialog.setVisible(true);
	}


}
