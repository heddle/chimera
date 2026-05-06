
package cnuphys.mosaic.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.mosaic.curve.BaseCurve;
import cnuphys.mosaic.curve.PhiCurve;
import cnuphys.mosaic.curve.TValue;
import cnuphys.mosaic.curve.ThetaCurve;
import cnuphys.mosaic.grid.CartesianGrid;
import cnuphys.mosaic.grid.Cell;
import cnuphys.mosaic.grid.Edge;
import cnuphys.mosaic.grid.GridSupport;
import cnuphys.mosaic.grid.MosaicGrid;
import cnuphys.mosaic.patch.Prepatch;
import cnuphys.mosaic.patch.ThetaPatch;
import cnuphys.mosaic.util.PanelKeys;
import cnuphys.mosaic.util.Point3D;
import cnuphys.mosaic.util.ThetaPhi;
import item3D.Item3D;

/**
 * A 3D item that displays a single Cell.
 * <p>
 * This class accepts a Cell (which you may have obtained from a MosaicGrid)
 * and displays the cell with its 12 edges drawn. In addition, for each
 * intersecting edge (the Edge objects stored in the cell) a small red marker is
 * drawn at the sphere intersection. Also, for every cell corner that is inside
 * the sphere (as indicated by the cell's bit mask) a blue marker is drawn.
 * <p>
 * The cell is “centered” in the 3D panel by computing its eight corners,
 * averaging them to obtain the center, and then translating the drawing so that
 * the cell center is at the origin.
 */
public class Cell3D extends Item3D {

	
	private static final Color thetaIntersectColor = X11Colors.getX11Color("Light Sea Green");
	private static final Color phiIntersectColor = X11Colors.getX11Color("Light Blue");

	// Static dialog-related fields for display; only one dialog instance is used.
	private static Panel3D oneCellPanel3D;
	private static Panel3D cellListPanel3D;
	private static SimpleDialog oneCellDialog;
	private static SimpleDialog cellListDialog;
	private static Cell3D cell3D;
	private static Cell3DOptionPanel cell3DOptionPanel;

	private Cell _cell;
	// The eight cell corners (each is a double[3]: {x, y, z})
	private double[][] _corners;
	private double _radius;
	private boolean _translate;
	boolean _showSphere;
	boolean _showClip;
	float _markerSize = 5f;
	boolean _annotations;

	private boolean drawPrepatch = true;

	/**
	 * Constructs a Cell3D.
	 *
	 * @param panel the Panel3D on which to draw this item.
	 * @param cell  the Cell to be displayed.
	 */
	public Cell3D(Panel3D panel, Cell cell, boolean annotations, boolean translate, boolean showSphere, boolean showClip,
			float markerSize) {
		super(panel);
		_annotations = annotations;
		_translate = translate;
		_showSphere = showSphere;
		_markerSize = markerSize;
		_showClip = showClip;
		setCell(cell);
	}

	/**
	 * Sets the cell to be displayed.
	 *
	 * @param cell the cell.
	 */
	public void setCell(Cell cell) {
		_cell = cell;
		_corners = GridSupport.getCellCorners(cell.getCartesianGrid(), cell.nx, cell.ny, cell.nz);
		_radius = cell.getRadius();
	}

	/**
	 * Draws the cell.
	 * <p>
	 * This method computes the center of the cell and translates the coordinate
	 * system so that the cell is drawn at the center of the panel. Then, it draws
	 * the rectangular cell, the sphere (with a different color inside the cell)
	 * and, for each intersecting edge, a small red marker at the sphere
	 * intersection. Finally, it also places a marker on every cell corner that is
	 * inside the sphere.
	 *
	 * @param drawable the OpenGL drawable.
	 */
	@Override
	public void draw(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glPushMatrix();

		// Compute cell center and bounds.

		double[] center = computeCenter(_corners);

		// if just displaying one cell, translate the origin to center of cell
		if (_translate) {
			gl.glTranslatef((float) -center[0], (float) -center[1], (float) -center[2]);
		}
		double[] bounds = computeBounds(_corners); // [xmin, xmax, ymin, ymax, zmin, zmax]

		// Translate so that the cell center is at the origin.

		// Draw the cell as a rectangular solid.
		Color cellFaceColor = new Color(0, 0, 0, 28);
		Support3D.drawRectangularSolid(drawable, (float) center[0], (float) center[1], (float) center[2],
				(float) (bounds[1] - bounds[0]), (float) (bounds[3] - bounds[2]), (float) (bounds[5] - bounds[4]),
				cellFaceColor, Color.black, 1f, true);

		// Draw the sphere with different colors outside and inside the cell.
		if (_showSphere) {
			Support3D.wireSphere(drawable, 0f, 0f, 0f, (float) _radius, 50, 50, Color.gray);
		}

		int intType = _cell.getIntersectionType();

		if (intType == Cell.kiss) {
			Point3D.Double kissPoint = _cell.getClosestPoint();
			Support3D.drawPoint(drawable, kissPoint.x, kissPoint.y, kissPoint.z, Color.green, 10f, true);
		} else {
			Edge[] edges = _cell.getEdges();
			int index = 0;
			for (Edge edge : edges) {
				Point3D.Double ip = edge.getIntersection();
				if (ip != null) {
					Color markerColor = Color.black;
					if (_annotations && cell3DOptionPanel.isCurveNumbering()) {
						Support3D.drawMarker(drawable, (float) ip.x, (float) ip.y, (float) ip.z, markerColor,
								_markerSize, true, "" + index, .3f, Color.black);
					} else {
						Support3D.drawPoint(drawable, (float) ip.x, (float) ip.y, (float) ip.z, markerColor,
								_markerSize, true);
					}
				}
				index++;
			}

			// Draw markers on cell corners that are inside the sphere.
			// Use the cell's bit mask for inside corners.
			// The bit mask encodes (from bit 0 to bit 7) which of the 8 corners are inside.
			// Since we already translated so that the center is at (0,0,0),
			// we simply subtract the center from each corner to get the relative position.
			for (int i = 0; i < _corners.length; i++) {
				// Check if corner 'i' is inside according to the bit mask.
				if ((_cell.getInsideCorners() & (1 << i)) != 0) {
					double rx = _corners[i][0];
					double ry = _corners[i][1];
					double rz = _corners[i][2];
					Color color = _annotations && cell3DOptionPanel.isMonochrome() ? Color.white : Color.cyan;

					Support3D.drawPoint(drawable, (float) rx, (float) ry, (float) rz, color, _markerSize+1, true);
				}
			}

			drawPrepatch(drawable);
		} // not kiss

		gl.glPopMatrix();
	}

	private void drawPrepatch(GLAutoDrawable drawable) {
		if (!drawPrepatch) {
			return;
		}

		Prepatch prepatch = _cell.getPrepatch();
		if (prepatch == null) {
			return;
		}

		//draw the prepatch
		if (_annotations && cell3DOptionPanel.isPrepatch()) {
			Drawing.drawPatch3D(drawable, prepatch, Color.black, 3f);
		}

		if (_annotations && cell3DOptionPanel.isThetaPatches()) {
			List<ThetaPatch> thetaPatches = prepatch.getThetaPatches();
			for (ThetaPatch tp : thetaPatches) {
				Drawing.drawPatch3D(drawable, tp, Color.black, 3f);
//				Drawing.drawPatch3D(drawable, tp, Color.black, Drawing.randomColor(), 3f);
			}
		}



		//show some spherical polygon approximation points?
		if (_annotations && cell3DOptionPanel.isSphericalPolygonPoints()) {
			List<ThetaPhi> vertices = prepatch.getSphericalVertices(5);
			for (ThetaPhi tp : vertices) {
				Point3D.Double p = tp.toCartesian();
				Color color = cell3DOptionPanel.isMonochrome() ? Color.black : Color.yellow;
				Support3D.drawPoint(drawable, (float) p.x, (float) p.y, (float) p.z, color, 5f);
			}
		}

		// draw bounding box
		if (_annotations && cell3DOptionPanel.isThetaSplicings()) {
		}


		if (_annotations && cell3DOptionPanel.isThetaCurves()) {


			System.out.println("Theta crossings size: " + prepatch.getThetaCrossings().size());
			for (TValue tv : prepatch.getThetaCrossings()) {
				Point3D.Double p = tv.point();
				Color color = cell3DOptionPanel.isMonochrome() ? Color.gray : thetaIntersectColor;
				Support3D.drawPoint(drawable, (float) p.x, (float) p.y, (float) p.z, color, 12f);
			}

			List<ThetaCurve> thetaCurves = prepatch.getThetaCurves();
			for (ThetaCurve tc : thetaCurves) {
				Drawing.curveDraw3D(drawable, tc, Color.orange, 3f);
			}


		}
		
		if (_annotations && cell3DOptionPanel.isPhiIntersections()) {
			List<TValue> phiCrossings = prepatch.getPhiCrossings();
			System.out.println("Phi crossings size: " + phiCrossings.size());
            for (TValue tv : phiCrossings) {
                Point3D.Double p = tv.point();
                Color color = cell3DOptionPanel.isMonochrome() ? Color.gray : phiIntersectColor;
                Support3D.drawPoint(drawable, (float) p.x, (float) p.y, (float) p.z, color, 12f);
            }
            
            List<ThetaPatch> thetaPatches = prepatch.getThetaPatches();
            int count = 0;
			for (ThetaPatch tp : thetaPatches) {
				List<PhiCurve> phiCurves = tp.getPhiCurves();
				for (PhiCurve pc : phiCurves) {
					Drawing.curveDraw3D(drawable, pc, Color.red, 3f);
					count++;
				}
			}
			System.out.println("Phi curve count: " + count);
        }



		if (_annotations && cell3DOptionPanel.isThetaSplicings()) {
			Color[] color = cell3DOptionPanel.isMonochrome()
					? new Color[] { Color.lightGray, Color.gray, Color.darkGray, Color.lightGray, Color.black, Color.gray, Color.white }
					: new Color[] { Color.cyan, Color.PINK, Color.yellow, Color.green, Color.red, Color.blue , Color.magenta};
			List<BaseCurve> splitCurves = prepatch.getSplitCurves();
	//		System.out.println("splitCurves.size() = " + splitCurves.size());
			for (int i = 0; i < splitCurves.size(); i++) {
				Drawing.curveDraw3D(drawable, splitCurves.get(i), color[i % 7], 3f + 4*(i%2));
				
				//for debugging
			//	Drawing.curveDraw3D(drawable, splitCurves.get(i).reverse(), Color.magenta, 3f);
			}
		}


	}



	/**
	 * Computes the center of the cell given its eight corners.
	 *
	 * @param corners the eight corners, each as a double[3].
	 * @return a double array {cx, cy, cz}.
	 */
	private double[] computeCenter(double[][] corners) {
		double cx = 0, cy = 0, cz = 0;
		for (double[] corner : corners) {
			cx += corner[0];
			cy += corner[1];
			cz += corner[2];
		}
		int n = corners.length;
		return new double[] { cx / n, cy / n, cz / n };
	}

	/**
	 * Computes the bounding box (min and max for each coordinate) of the cell.
	 *
	 * @param corners the eight corners, each as a double[3].
	 * @return a double array {xmin, xmax, ymin, ymax, zmin, zmax}.
	 */
	private double[] computeBounds(double[][] corners) {
		double xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE, ymax = -Double.MAX_VALUE;
		double zmin = Double.MAX_VALUE, zmax = -Double.MAX_VALUE;
		for (double[] corner : corners) {
			xmin = Math.min(xmin, corner[0]);
			xmax = Math.max(xmax, corner[0]);
			ymin = Math.min(ymin, corner[1]);
			ymax = Math.max(ymax, corner[1]);
			zmin = Math.min(zmin, corner[2]);
			zmax = Math.max(zmax, corner[2]);
		}
		return new double[] { xmin, xmax, ymin, ymax, zmin, zmax };
	}

	/**
	 * Displays the specified cell in a dialog containing a Panel3D.
	 *
	 * @param cell the cell to be displayed.
	 */
	public static void displayCell(Cell cell) {

		double[][] corners = GridSupport.getCellCorners(cell.getCartesianGrid(), cell.nx, cell.ny, cell.nz);
		double[] bounds = computeStaticBounds(corners);
		double dz = bounds[5] - bounds[4];

		// Set view parameters.
		final float xdist = 0f;
		final float ydist = 0f;
		final float zdist = (float) (-3 * dz);
		final float thetaX = 45f;
		final float thetaY = 45f;
		final float thetaZ = 45f;
		float delta = (float) (cell.getCartesianGrid().getXGrid().getAverageSpacing() / 10);

		if (oneCellDialog == null) {
			oneCellPanel3D = new Panel3D(thetaX, thetaY, thetaZ, xdist, ydist, zdist) {
				@Override
				public void createInitialItems() {
					cell3D = new Cell3D(this, cell, true, true, true, true, 5f);
					addItem(cell3D);
				}

				@Override
				public Dimension getPreferredSize() {
					return new Dimension(800, 800);
				}
			};

			String title = "Cell " + cell.nx + ", " + cell.ny + ", " + cell.nz + " [" + cell.getIntersectionTypeString()
					+ "]";

			cell3DOptionPanel = new Cell3DOptionPanel(oneCellPanel3D);
			oneCellDialog = Cell3DSupport.cellDialog(title, oneCellPanel3D);

			oneCellDialog.add(cell3DOptionPanel, "East");


			PanelKeys.addKeyListener(oneCellPanel3D, delta, delta, delta);

		} else {
			cell3D.setCell(cell);
			oneCellDialog.setTitle("Cell " + cell.nx + ", " + cell.ny + ", " + cell.nz + " ["
					+ cell.getIntersectionTypeString() + "]");
			oneCellPanel3D.refresh();
		}

		oneCellDialog.setVisible(true);
	}

	/**
	 * Computes the bounding box from a set of corners (static version for use in
	 * displayCell).
	 *
	 * @param corners the eight corners.
	 * @return a double array {xmin, xmax, ymin, ymax, zmin, zmax}.
	 */
	private static double[] computeStaticBounds(double[][] corners) {
		double xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE, ymax = -Double.MAX_VALUE;
		double zmin = Double.MAX_VALUE, zmax = -Double.MAX_VALUE;
		for (double[] corner : corners) {
			xmin = Math.min(xmin, corner[0]);
			xmax = Math.max(xmax, corner[0]);
			ymin = Math.min(ymin, corner[1]);
			ymax = Math.max(ymax, corner[1]);
			zmin = Math.min(zmin, corner[2]);
			zmax = Math.max(zmax, corner[2]);
		}
		return new double[] { xmin, xmax, ymin, ymax, zmin, zmax };
	}

	/**
	 * Displays a list of cells in a dialog containing a Panel3D.
	 *
	 * @param cells
	 * @param grid
	 */
	public static void displayCellList(List<Cell> cells, MosaicGrid grid, int showType) {

		if (cellListDialog != null) {
			cellListDialog.dispose();
			cellListDialog = null;
		}

		CartesianGrid cartGrid = grid.getCartesianGrid();

		final float xymax = (float) Math.max(cartGrid.getXMax(), cartGrid.getYMax());
		final float zmax = (float) cartGrid.getZMax();
		float xdist = 0.1f * xymax;
		float ydist = .1f * xymax;
		float zdist = -3f * zmax;

		double radius = grid.getSphericalGrid().getRadius();

		float delta = 0.1f * (float) radius;

		cellListPanel3D = new Panel3D(45f, 45f, 45f, xdist, ydist, zdist) {
			@Override
			public void createInitialItems() {
				boolean first = true;

				for (Cell cell : cells) {
					int type = cell.getIntersectionType();
					boolean show = (showType == Cell.allTypes || type == showType
							|| (showType == Cell.polar && (cell.getPoleEnclosed() != 0)));
					if (show) {
						Cell3D cell3D = new Cell3D(this, cell, false, false, first, false, 3f);
						first = false;
						addItem(cell3D);
					}
				}
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(800, 800);
			}
		};
		PanelKeys.addKeyListener(cellListPanel3D, delta, delta, delta);

		String title = "Cell List [";
		if (showType == Cell.allTypes) {
			title += "All Types]";
		} else if (showType == Cell.polar) {
			title += "Polar Cells + Pole]";
		} else {
			title += Cell.intersectionTypes[showType] + "]";
		}

		cellListDialog = Cell3DSupport.cellDialog(title, cellListPanel3D);
		cellListDialog.setVisible(true);
	}
}
