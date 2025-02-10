
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
 * <p>
 * This class accepts a Cell (which you may have obtained from a ChimeraGrid)
 * and displays the cell with its 12 edges drawn. In addition, for each intersecting
 * edge (the Edge objects stored in the cell) a small red marker is drawn at the sphere
 * intersection. Also, for every cell corner that is inside the sphere (as indicated by the
 * cell's bit mask) a blue marker is drawn.
 * <p>
 * The cell is “centered” in the 3D panel by computing its eight corners,
 * averaging them to obtain the center, and then translating the drawing so that
 * the cell center is at the origin.
 */
public class ChimeraCell3D extends Item3D {

    // Static dialog-related fields for display; only one dialog instance is used.
    private static Panel3D panel3D;
    private static SimpleDialog dialog;
    private static ChimeraCell3D cell3D;

    private Cell _cell;
    // The eight cell corners (each is a double[3]: {x, y, z})
    private double[][] _corners;
    private double _radius;

    /**
     * Constructs a ChimeraCell3D.
     *
     * @param panel the Panel3D on which to draw this item.
     * @param cell  the Cell to be displayed.
     */
    public ChimeraCell3D(Panel3D panel, Cell cell) {
        super(panel);
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
     * This method computes the center of the cell and translates
     * the coordinate system so that the cell is drawn at the center of the panel.
     * Then, it draws the rectangular cell, the sphere (with a different color inside
     * the cell) and, for each intersecting edge, a small red marker at the sphere
     * intersection. Finally, it also places a marker on every cell corner that is inside
     * the sphere.
     *
     * @param drawable the OpenGL drawable.
     */
    @Override
    public void draw(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();

        Support3D.prepareForTransparent(drawable);

        // Compute cell center and bounds.
        double[] center = computeCenter(_corners);
        double[] bounds = computeBounds(_corners);  // [xmin, xmax, ymin, ymax, zmin, zmax]

        // Translate so that the cell center is at the origin.
        gl.glTranslatef((float) -center[0], (float) -center[1], (float) -center[2]);

        // Draw the cell as a rectangular solid.
        Color cellFaceColor = new Color(0, 0, 0, 28);
        Support3D.drawRectangularSolid(drawable, (float) center[0], (float) center[1], (float) center[2],
                (float) (bounds[1] - bounds[0]), (float) (bounds[3] - bounds[2]), (float) (bounds[5] - bounds[4]),
                cellFaceColor, 1f, true);

        // Draw the sphere with different colors outside and inside the cell.
        clipSphere(drawable, bounds);

        // Draw markers at intersections along the cell's edges.
        Edge[] edges = _cell.getEdges();

        if (edges != null) {
            for (Edge edge : edges) {
                Point3D.Double ip = edge.getIntersection();
                if (ip != null) {
                    Color markerColor = Color.red;
                    Support3D.drawPoint(drawable, (float) ip.x, (float) ip.y, (float) ip.z, markerColor, 10f, true);
                }
            }
        }

        // --- New Code: Draw markers on cell corners that are inside the sphere.
        // Use the cell's bit mask for inside corners.
        // The bit mask encodes (from bit 0 to bit 7) which of the 8 corners are inside.
        // Since we already translated so that the center is at (0,0,0),
        // we simply subtract the center from each corner to get the relative position.
        for (int i = 0; i < _corners.length; i++) {
            // Check if corner 'i' is inside according to the bit mask.
            if (( _cell.getInsideCorners() & (1 << i)) != 0) {
                double rx = _corners[i][0];
                double ry = _corners[i][1];
                double rz = _corners[i][2];
                
                Support3D.drawPoint(drawable, (float) rx, (float) ry, (float) rz, Color.blue, 10f);
            }
        }

        gl.glPopMatrix();
    }

    /**
     * Draws the sphere using clipping planes so that the portion inside the cell
     * is drawn with a different color.
     *
     * @param drawable the OpenGL drawable.
     * @param bounds   the cell bounds in the form [xmin, xmax, ymin, ymax, zmin, zmax].
     */
    private void clipSphere(GLAutoDrawable drawable, double[] bounds) {
        GL2 gl = drawable.getGL().getGL2();

        // First, draw the sphere's outer (wireframe) for context.
        Support3D.wireSphere(drawable, 0f, 0f, 0f, (float) _radius, 50, 50, Color.gray);

        // Save current state before enabling clipping.
        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_COLOR_BUFFER_BIT);

        // Define the six clipping planes corresponding to the cell faces.
        double[] eqXmin = { 1.0, 0.0, 0.0, -bounds[0] }; // x >= xmin
        double[] eqXmax = { -1.0, 0.0, 0.0, bounds[1] };   // x <= xmax
        double[] eqYmin = { 0.0, 1.0, 0.0, -bounds[2] };   // y >= ymin
        double[] eqYmax = { 0.0, -1.0, 0.0, bounds[3] };     // y <= ymax
        double[] eqZmin = { 0.0, 0.0, 1.0, -bounds[4] };     // z >= zmin
        double[] eqZmax = { 0.0, 0.0, -1.0, bounds[5] };     // z <= zmax

        gl.glClipPlane(GL2.GL_CLIP_PLANE0, eqXmin, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE1, eqXmax, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE2, eqYmin, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE3, eqYmax, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE4, eqZmin, 0);
        gl.glClipPlane(GL2.GL_CLIP_PLANE5, eqZmax, 0);

        gl.glEnable(GL2.GL_CLIP_PLANE0);
        gl.glEnable(GL2.GL_CLIP_PLANE1);
        gl.glEnable(GL2.GL_CLIP_PLANE2);
        gl.glEnable(GL2.GL_CLIP_PLANE3);
        gl.glEnable(GL2.GL_CLIP_PLANE4);
        gl.glEnable(GL2.GL_CLIP_PLANE5);

        // Draw the sphere inside the cell with the "inside" color.
        Color insideColor = new Color(0, 128, 0, 100);
        Support3D.solidSphereShell(drawable, 0f, 0f, 0f, (float) (0.9999 * _radius), (float) _radius, 200, 200, insideColor);

        // Disable the clipping planes and restore state.
        gl.glDisable(GL2.GL_CLIP_PLANE0);
        gl.glDisable(GL2.GL_CLIP_PLANE1);
        gl.glDisable(GL2.GL_CLIP_PLANE2);
        gl.glDisable(GL2.GL_CLIP_PLANE3);
        gl.glDisable(GL2.GL_CLIP_PLANE4);
        gl.glDisable(GL2.GL_CLIP_PLANE5);
        gl.glPopAttrib();
    }

    /**
     * Computes the center of the cell given its eight corners.
     *
     * @param corners the eight corners, each as a double[3].
     * @return a double array {cx, cy, cz}.
     */
    private double[] computeCenter(double[][] corners) {
        double cx = 0, cy = 0, cz = 0;
        for (int i = 0; i < corners.length; i++) {
            cx += corners[i][0];
            cy += corners[i][1];
            cz += corners[i][2];
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
        for (int i = 0; i < corners.length; i++) {
            xmin = Math.min(xmin, corners[i][0]);
            xmax = Math.max(xmax, corners[i][0]);
            ymin = Math.min(ymin, corners[i][1]);
            ymax = Math.max(ymax, corners[i][1]);
            zmin = Math.min(zmin, corners[i][2]);
            zmax = Math.max(zmax, corners[i][2]);
        }
        return new double[] { xmin, xmax, ymin, ymax, zmin, zmax };
    }

    /**
     * Displays the specified cell in a dialog containing a Panel3D.
     *
     * @param cell the cell to be displayed.
     */
    public static void displayCell(Cell cell) {
        System.out.println("Cell clicked");

        double[][] corners = GridSupport.getCellCorners(cell.getCartesianGrid(), cell.nx, cell.ny, cell.nz);
        double[] bounds = computeStaticBounds(corners);
        double dz = bounds[5] - bounds[4];

        double radius = cell.getRadius();
        final float radExt = (float) (1.1 * radius);

        // Set view parameters.
        final float xdist = 0f;
        final float ydist = 0f;
        final float zdist = (float) (-3 * dz);
        final float thetaX = 45f;
        final float thetaY = 45f;
        final float thetaZ = 45f;

        if (dialog == null) {
            panel3D = new Panel3D(thetaX, thetaY, thetaZ, xdist, ydist, zdist) {
                @Override
                public void createInitialItems() {
                    String[] labels = { "X", "Y", "Z" };
//                    Axes3D axes = new Axes3D(this, -radExt, radExt, -radExt, radExt, -radExt, radExt,
//                            labels, Color.black, 0.5f, 2, 2, 2, Color.black, Color.black,
//                            new Font("SansSerif", Font.PLAIN, 14), 1);
                    cell3D = new ChimeraCell3D(this, cell);
                    addItem(cell3D);
 //                   addItem(axes);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(800, 800);
                }
            };

            String title = "Cell " + cell.nx + ", " + cell.ny + ", " + cell.nz + " ["
                    + cell.getIntersectionTypeString() + "]";
            dialog = new SimpleDialog(title, false, "Close") {
                @Override
                public Component createCenterComponent() {
                    return panel3D;
                }
            };
        } else {
            cell3D.setCell(cell);
            dialog.setTitle("Cell " + cell.nx + ", " + cell.ny + ", " + cell.nz + " ["
                    + cell.getIntersectionTypeString() + "]");
             panel3D.refresh();
        }

        dialog.setVisible(true);
    }

    /**
     * Computes the bounding box from a set of corners (static version for use in displayCell).
     *
     * @param corners the eight corners.
     * @return a double array {xmin, xmax, ymin, ymax, zmin, zmax}.
     */
    private static double[] computeStaticBounds(double[][] corners) {
        double xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE;
        double ymin = Double.MAX_VALUE, ymax = -Double.MAX_VALUE;
        double zmin = Double.MAX_VALUE, zmax = -Double.MAX_VALUE;
        for (int i = 0; i < corners.length; i++) {
            xmin = Math.min(xmin, corners[i][0]);
            xmax = Math.max(xmax, corners[i][0]);
            ymin = Math.min(ymin, corners[i][1]);
            ymax = Math.max(ymax, corners[i][1]);
            zmin = Math.min(zmin, corners[i][2]);
            zmax = Math.max(zmax, corners[i][2]);
        }
        return new double[] { xmin, xmax, ymin, ymax, zmin, zmax };
    }
}
