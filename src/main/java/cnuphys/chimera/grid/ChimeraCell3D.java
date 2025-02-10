package cnuphys.chimera.grid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import item3D.Axes3D;
import item3D.Item3D;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.chimera.util.Point3D;

/**
 * A 3D item that displays a single Cell.
 * 
 * <p>This class accepts a Cell (which you may have obtained from a ChimeraGrid)
 * and displays the cell with its 12 edges drawn. In addition, for each intersecting
 * edge (the Edge objects stored in the cell) a small sphere marker is drawn at the
 * intersection point.
 * 
 * <p>The cell is “centered” in the 3D panel by computing its eight corners,
 * averaging them to obtain the center, and then translating the drawing so that
 * the cell center is at the origin.
 */
public class ChimeraCell3D extends Item3D {

    private Cell _cell;
    // The eight cell corners (each is a double[3]: {x, y, z})
    private double[][] _corners;

    /**
     * Constructs a ChimeraCell3D.
     * 
     * @param panel the Panel3D on which to draw this item.
     * @param cell  the Cell to be displayed.
     */
    public ChimeraCell3D(Panel3D panel, Cell cell) {
        super(panel);
        _cell = cell;
        // Compute the eight corners of the cell.
        // (Assumes that GridSupport.getCellCorners returns a double[8][3] array;
        // you may need to adjust if your GridSupport method differs.)
        _corners = GridSupport.getCellCorners(cell.getCartesianGrid(), cell.nx, cell.ny, cell.nz);
    }

    /**
     * Draws the cell.
     * 
     * <p>This method first computes the center of the cell and translates
     * the coordinate system so that the cell is drawn at the center of the panel.
     * Then, it draws the 12 edges of the hexahedral cell and, for each intersecting
     * edge, a small red marker at the sphere intersection.
     * 
     * @param drawable the OpenGL drawable.
     */
    @Override
    public void draw(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();

        // Compute cell center by averaging the 8 corner coordinates.
        double cx = 0, cy = 0, cz = 0;
        for (int i = 0; i < 8; i++) {
            cx += _corners[i][0];
            cy += _corners[i][1];
            cz += _corners[i][2];
        }
        cx /= 8.0;
        cy /= 8.0;
        cz /= 8.0;

        // Translate so that the cell center is at the origin.
        gl.glTranslatef((float) -cx, (float) -cy, (float) -cz);

        // Draw the cell's edges.
        // Here we define the 12 edges of a hexahedral cell using the standard corner indices:
        // Assume the following indexing for the 8 cell corners:
        //   0: (xmin, ymin, zmin)
        //   1: (xmax, ymin, zmin)
        //   2: (xmax, ymax, zmin)
        //   3: (xmin, ymax, zmin)
        //   4: (xmin, ymin, zmax)
        //   5: (xmax, ymin, zmax)
        //   6: (xmax, ymax, zmax)
        //   7: (xmin, ymax, zmax)
        int[][] edgeIndices = {
            {0, 1}, {1, 3}, {2, 3}, {2, 0}, // bottom face
            {4, 5}, {5, 7}, {6, 7}, {6, 4}, // top face
            {0, 4}, {1, 5}, {2, 6}, {3, 7}  // vertical edges
        };

        Color edgeColor = Color.blue;
        float lineWidth = 2f;

        for (int[] edge : edgeIndices) {
            float[] p1 = new float[] {
                (float) _corners[edge[0]][0],
                (float) _corners[edge[0]][1],
                (float) _corners[edge[0]][2]
            };
            float[] p2 = new float[] {
                (float) _corners[edge[1]][0],
                (float) _corners[edge[1]][1],
                (float) _corners[edge[1]][2]
            };
            Support3D.drawLine(drawable, p1, p2, edgeColor, lineWidth);
        }

        // Draw a marker (a small red sphere) at each intersection along the cell's edges.
        // (Assumes that Cell has a public method getEdges() returning an array of Edge objects.)
        Edge[] edges = _cell.getEdges();
        if (edges != null) {
            for (Edge edge : edges) {
                Point3D.Double ip = edge.getIntersection();
                if (ip != null) {
                    float markerRadius = 0.01f;
                    int slices = 10;
                    int stacks = 10;
                    Color markerColor = Color.red;
                    Support3D.solidSphere(drawable, (float) ip.x, (float) ip.y, (float) ip.z,
                            markerRadius, slices, stacks, markerColor);
                }
            }
        }

        gl.glPopMatrix();
    }
    
	/**
	 * Gets the cell associated with this item.
	 * 
	 * @return the cell.
	 */
	public static void displayCell(Cell cell) {
		System.out.println("Cell clicked");
        double[][] corners = GridSupport.getCellCorners(cell.getCartesianGrid(), cell.nx, cell.ny, cell.nz);

		double xmin = Double.MAX_VALUE;
		double xmax = -Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double ymax = -Double.MAX_VALUE;
		double zmin = Double.MAX_VALUE;
		double zmax = -Double.MAX_VALUE;

		for (int i = 0; i < 8; i++) {
			xmin = Math.min(xmin, corners[i][0]);
			xmax = Math.max(xmax, corners[i][0]);
			ymin = Math.min(ymin, corners[i][1]);
			ymax = Math.max(ymax, corners[i][1]);
			zmin = Math.min(zmin, corners[i][2]);
			zmax = Math.max(zmax, corners[i][2]);
		}
		
		
		double dx = xmax - xmin;
		double dy = ymax - ymin;
	    double dz = zmax - zmin;
        
		final float xdist = (float)(0);
		final float ydist = (float)(0);
		final float zdist = (float)(zmin - dz/8);
//		final float xdist = (float)(- dx);
//		final float ydist = (float)(- dy);
//		final float zdist = (float)(- dz);

		final float thetaX = 45f;
		final float thetaY = 45f;
		final float thetaZ = 45f;

        
        Panel3D panel3D = new Panel3D(thetaX, thetaY, thetaZ, xdist, ydist, zdist) {
        	@Override
        	public void createInitialItems() {
				addItem(new ChimeraCell3D(this, cell));
				
				String labels[] = { "X", "Y", "Z" };
				

				Axes3D axes = new Axes3D(this, -10f, 10f, -10f, 10f, -10f,
						10f, labels, Color.darkGray, 2f, 2, 2, 2, Color.black, Color.blue,
						new Font("SansSerif", Font.PLAIN, 14), 1);
				
				addItem(axes);

			}
        	
        	@Override
			public Dimension getPreferredSize() {
				return new Dimension(800, 800);
			}
        };
        
        String title = "Cell " + cell.nx + ", " + cell.ny + ", " + 
        cell.nz + " [" + cell.getIntersectionTypeString() + "]";
        SimpleDialog dialog = new SimpleDialog(title, false, "Close") {
        	@Override
			public Component createCenterComponent() {
				return panel3D;
			}
        };
        
        dialog.setVisible(true);


	}
}
